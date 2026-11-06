package co.edu.uniquindio.petadoptionapp.data.repository.firebase

import android.content.Context
import androidx.core.content.edit
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.EMAILS
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.MODERATORS
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.TWO_FACTOR
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.USERS
import co.edu.uniquindio.petadoptionapp.data.repository.remote.EmailJsCodeSender
import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.model.auth.Credentials
import co.edu.uniquindio.petadoptionapp.domain.model.auth.TwoFactorChallenge
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.Moderator
import co.edu.uniquindio.petadoptionapp.domain.model.users.User
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.LoginResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
Autenticación con Firebase Auth (correo y contraseña) y verificación en dos pasos (A2F) propia.

- El rol y el alias no los guarda Auth: se leen del perfil (`users/{uid}` o `moderators/{uid}`).
- A2F: con email y contraseña correctos, si el perfil la tiene activa, se genera un código de 6 dígitos,
  se guarda su HASH en `twoFactorChallenges/{uid}` y se envía al correo con EmailJS. La sesión de la app
  ([currentAccount]) solo se abre cuando el código es correcto.
- Mientras falta el código, Firebase sí tiene la sesión (las reglas de Firestore la piden para guardar
  el código). Por eso se recuerda en el teléfono quién quedó a medias: al volver a abrir la app se cierra.

Implementa AuthRepository del dominio y se enlaza en RepositoryModule. Es la fuente de verdad de la
sesión de la app: expone `currentAccount` (StateFlow) que observan FirestoreUserRepository y los
ViewModels. Lo usan los casos de uso de login, registro, A2F, recuperación y cambio de contraseña
y eliminación de cuenta. Delega el envío del código A2F en EmailJsCodeSender.
**/
// Una sola instancia: la sesión de la app debe ser única.
@Singleton
// Hilt inyecta Firebase Auth, Firestore, el enviador de correos y el Context de la aplicación.
class FirebaseAuthRepository @Inject constructor(
    // Firebase Authentication: maneja las credenciales de correo y contraseña.
    private val auth: FirebaseAuth,
    // Firestore: perfiles, índice de correos y retos A2F.
    private val db: FirebaseFirestore,
    // Servicio que envía el código A2F por correo.
    private val emailSender: EmailJsCodeSender,
    // Context de la aplicación, solo para abrir las SharedPreferences.
    @ApplicationContext context: Context,
) : AuthRepository {

    // Estado interno con la cuenta de la sesión de la app (null si no hay sesión completa).
    private val _currentAccount = MutableStateFlow<Account?>(null)
    // Vista de solo lectura de la sesión que observa el resto de la app.
    override val currentAccount: StateFlow<Account?> = _currentAccount.asStateFlow()

    // Guarda el uid que inició sesión pero aún no escribe el código A2F (sobrevive a cerrar la app).
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Alias de quien espera el código, para saludarlo en el correo sin volver a leer su perfil.
    private var pendingUserName: String = ""

    // Generador aleatorio criptográficamente seguro para los códigos A2F.
    private val random = SecureRandom()

    // Lo que se necesita del perfil para abrir la sesión.
    private data class Profile(val account: Account, val twoFactorEnabled: Boolean)

    // Lee el perfil de usuario o de moderador. Sin perfil, devuelve una cuenta básica (CheckProfile la rechaza).
    private suspend fun loadProfile(uid: String, email: String?): Profile {
        // Primero busca un perfil de usuario común; si existe, devuelve su cuenta con rol USER y su ajuste de A2F.
        db.collection(USERS).document(uid).get().await().toModel<User>()?.let {
            return Profile(it.account.copy(id = uid, role = UserRole.USER), it.settings.twoFactorEnabled)
        }
        // Si no, busca un perfil de moderador; si existe, devuelve su cuenta con rol MODERATOR.
        db.collection(MODERATORS).document(uid).get().await().toModel<Moderator>()?.let {
            return Profile(it.account.copy(id = uid, role = UserRole.MODERATOR), it.twoFactorEnabled)
        }
        // Sin perfil: cuenta mínima con id y correo, sin A2F.
        return Profile(Account(id = uid, email = email.orEmpty()), twoFactorEnabled = false)
    }


    // Sesión

    // Restaura la sesión guardada por Firebase al abrir la app (lo usa el splash).
    override suspend fun restoreSession(): Account? {
        // Si Firebase no tiene usuario guardado, no hay sesión.
        val firebaseUser = auth.currentUser ?: return null
        // Quedó a mitad de la A2F: no cuenta como sesión.
        if (prefs.getString(KEY_PENDING_TWO_FACTOR, null) == firebaseUser.uid) {
            // Cierra la sesión de Firebase para que no quede a medias.
            logout()
            // Informa que no hay sesión.
            return null
        }
        // Sin internet, Firestore responde con su copia local si ya había leído el perfil antes.
        val profile = runCatching { loadProfile(firebaseUser.uid, firebaseUser.email) }.getOrNull() ?: return null
        // Publica la cuenta como sesión activa de la app.
        _currentAccount.value = profile.account
        // Devuelve la cuenta restaurada.
        return profile.account
    }

    // Cierra la sesión por completo.
    override suspend fun logout() {
        // Cierra la sesión en Firebase Auth.
        auth.signOut()
        // Olvida el uid pendiente de A2F guardado en el teléfono.
        prefs.edit { remove(KEY_PENDING_TWO_FACTOR) }
        // Olvida el alias pendiente.
        pendingUserName = ""
        // Deja la app sin sesión.
        _currentAccount.value = null
    }


    // Login

    // Inicia sesión con correo y contraseña; puede terminar en éxito o pedir el código A2F.
    override suspend fun login(credentials: Credentials): Result<LoginResult> = runCatching {
        // Autentica con Firebase (el correo sin espacios); si no devuelve usuario, falla como no autenticado.
        val firebaseUser = auth.signInWithEmailAndPassword(credentials.email.trim(), credentials.password)
            .await().user ?: throw AuthException.NotAuthenticated

        // Si no se puede leer el perfil (sin internet), no se deja la sesión de Firebase a medias.
        val profile = try {
            // Intenta leer el perfil del usuario autenticado.
            loadProfile(firebaseUser.uid, firebaseUser.email)
        } catch (e: Exception) {
            // Si falla, cierra la sesión de Firebase.
            auth.signOut()
            // Y relanza el error original.
            throw e
        }

        // Si el perfil tiene A2F activa, la sesión de la app aún no se abre.
        if (profile.twoFactorEnabled) {
            // Recuerda en el teléfono que este uid quedó esperando el código.
            prefs.edit { putString(KEY_PENDING_TWO_FACTOR, firebaseUser.uid) }
            // Guarda el alias para saludarlo en el correo.
            pendingUserName = profile.account.userName
            // Devuelve que se requiere el código, con el id de la cuenta.
            LoginResult.TwoFactorRequired(firebaseUser.uid)
        } else {
            // Sin A2F: abre la sesión de la app de inmediato.
            _currentAccount.value = profile.account
            // Devuelve el éxito con la cuenta.
            LoginResult.Success(profile.account)
        }
    // Traduce los errores de Firebase Auth a errores del dominio.
    }.mapAuthError()


    // A2F

    // Genera un código A2F, guarda su hash en Firestore y lo envía por correo.
    override suspend fun requestTwoFactorCode(accountId: String): Result<Unit> {
        // Solo vale si el usuario de Firebase es el mismo que pide el código; si no, falla como no autenticado.
        val firebaseUser = auth.currentUser?.takeIf { it.uid == accountId }
            ?: return Result.failure(AuthException.NotAuthenticated)
        // Si EmailJS no está configurado, el código no puede salir: falla de inmediato.
        if (!emailSender.isConfigured) return Result.failure(AuthException.CodeNotSent)

        // Genera un código de CODE_LENGTH dígitos aleatorios.
        val code = (1..TwoFactorChallenge.CODE_LENGTH).joinToString("") { random.nextInt(10).toString() }
        // Momento actual en milisegundos.
        val now = System.currentTimeMillis()
        // Un código nuevo reemplaza al anterior (y reinicia los intentos).
        val challenge = TwoFactorChallenge(
            // Dueño del reto.
            userId = accountId,
            // Hash del código (nunca el código en claro).
            codeHash = hash(accountId, code),
            // Vencimiento: ahora más el tiempo de vida del código.
            expiresAt = now + TwoFactorChallenge.TTL_MILLIS,
            // Momento de creación.
            createdAt = now
        )
        // Guarda el reto en `twoFactorChallenges/{uid}`, reemplazando cualquier reto anterior.
        runCatching { db.collection(TWO_FACTOR).document(accountId).set(challenge.toFirestore()).await() }
            // Si no se pudo guardar, informa que el código no se envió.
            .onFailure { return Result.failure(AuthException.CodeNotSent) }

        // Envía el código por correo y devuelve el resultado del envío.
        return emailSender.sendTwoFactorCode(
            // Correo de la cuenta de Firebase.
            toEmail = firebaseUser.email.orEmpty(),
            // Alias guardado durante el login.
            userName = pendingUserName,
            // Código en claro (solo viaja en el correo).
            code = code,
            // Vigencia expresada en minutos.
            minutes = TwoFactorChallenge.TTL_MILLIS / 60_000
        // Cualquier fallo del envío se traduce a CodeNotSent.
        ).recoverCatching { throw AuthException.CodeNotSent }
    }

    // Verifica el código A2F escrito; si es correcto, abre la sesión de la app.
    override suspend fun verifyTwoFactorCode(accountId: String, code: String): Result<Account> {
        // Solo vale para el usuario de Firebase que tiene la sesión a medias.
        val firebaseUser = auth.currentUser?.takeIf { it.uid == accountId }
            ?: return Result.failure(AuthException.NotAuthenticated)
        // Referencia al reto A2F del usuario.
        val ref = db.collection(TWO_FACTOR).document(accountId)

        // Ejecuta la verificación atrapando cualquier error en un Result.
        return runCatching {
            // Leer, comparar y contar el intento en una transacción: dos toques rápidos no se saltan el límite.
            val error: AuthException? = db.runTransaction { tx ->
                // Lee el reto actual.
                val challenge = tx.get(ref).toModel<TwoFactorChallenge>()
                // Evalúa el reto y decide el resultado (null = código correcto).
                when {
                    // Sin reto, ya usado o vencido: el código expiró.
                    challenge == null || challenge.consumed || challenge.hasExpired() -> AuthException.CodeExpired
                    // Demasiados intentos fallidos: se bloquea la verificación.
                    challenge.hasReachedMaxAttempts() -> AuthException.TooManyAttempts
                    // Código incorrecto: suma un intento y lo informa.
                    challenge.codeHash != hash(accountId, code) -> {
                        // Guarda el nuevo número de intentos.
                        tx.update(ref, "attempts", challenge.attempts + 1)
                        // Devuelve el error de código incorrecto.
                        AuthException.WrongCode
                    }
                    // Código correcto.
                    else -> {
                        // Un solo uso.
                        tx.update(ref, "consumed", true)
                        null
                    }
                }
            // Espera a que la transacción termine.
            }.await()
            // Si la transacción devolvió un error, se lanza fuera de ella (para no deshacer la escritura del intento).
            if (error != null) throw error

            // Carga el perfil para abrir la sesión.
            val profile = loadProfile(firebaseUser.uid, firebaseUser.email)
            // Ya no hay A2F pendiente: lo borra del teléfono.
            prefs.edit { remove(KEY_PENDING_TWO_FACTOR) }
            // Olvida el alias pendiente.
            pendingUserName = ""
            // Abre la sesión de la app.
            _currentAccount.value = profile.account
            // Devuelve la cuenta autenticada.
            profile.account
        }
    }

    // SHA-256 del uid y el código: en Firestore nunca queda el código en claro.
    private fun hash(uid: String, code: String): String =
        // Obtiene el algoritmo SHA-256.
        MessageDigest.getInstance("SHA-256")
            // Calcula el hash de "uid:código".
            .digest("$uid:$code".toByteArray())
            // Convierte los bytes a texto hexadecimal.
            .joinToString("") { "%02x".format(it) }


    // Crear cuenta

    // Crea la cuenta en Firebase Auth y devuelve su uid.
    override suspend fun register(credentials: Credentials): Result<String> = runCatching {
        // Correo sin espacios en los extremos.
        val email = credentials.email.trim()
        // Crea el usuario en Firebase; si no lo devuelve, falla como no autenticado.
        val firebaseUser = auth.createUserWithEmailAndPassword(email, credentials.password).await().user
            ?: throw AuthException.NotAuthenticated
        // Firebase abre la sesión al registrar; el alias llega después con updateSessionAccount.
        _currentAccount.value = Account(id = firebaseUser.uid, email = email, role = UserRole.USER)
        // Devuelve el uid, que será el id del perfil en Firestore.
        firebaseUser.uid
    // Traduce los errores de Firebase Auth (correo en uso, contraseña débil...).
    }.mapAuthError()

    // Con la protección contra enumeración de Firebase Auth no se puede preguntar por correos: se usa el índice `emails`.
    override suspend fun isEmailRegistered(email: String): Boolean =
        // Consulta si existe el documento índice del correo (en minúsculas).
        db.collection(EMAILS).document(email.trim().lowercase()).get().await().exists()

    // Completa la cuenta de la sesión con el alias y el rol una vez creado el perfil.
    override suspend fun updateSessionAccount(account: Account): Result<Unit> {
        // Cuenta actual de la sesión.
        val current = _currentAccount.value
        // Solo se puede actualizar la cuenta de la propia sesión.
        if (current == null || current.id != account.id) return Result.failure(AuthException.NotAuthenticated)
        // Actualiza alias y rol conservando el resto.
        _currentAccount.value = current.copy(userName = account.userName, role = account.role)
        // Resultado exitoso.
        return Result.success(Unit)
    }


    // Contraseña

    // Envía el correo de recuperación de contraseña.
    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        try {
            // Pide a Firebase que envíe el correo con el enlace para restablecer la contraseña.
            auth.sendPasswordResetEmail(email.trim()).await()
        } catch (_: FirebaseAuthInvalidUserException) {
            // No se revela si el correo existe: la pantalla dice lo mismo en ambos casos.
        }
        // Resultado exitoso (aunque el correo no exista).
        Unit
    }

    // Cambia la contraseña después de confirmar la actual.
    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        // Firebase exige un inicio de sesión reciente: reautentica con la contraseña actual o falla.
        reauthenticate(currentPassword).getOrThrow()
        // Usuario de Firebase de la sesión.
        val firebaseUser = auth.currentUser ?: throw AuthException.NotAuthenticated
        // Actualiza la contraseña en Firebase.
        firebaseUser.updatePassword(newPassword).await()
        // Resultado exitoso.
        Unit
    // Traduce los errores de Firebase Auth (por ejemplo, contraseña actual incorrecta o débil).
    }.mapAuthError()


    // Eliminar cuenta

    // Confirma la identidad con la contraseña (necesario antes de operaciones sensibles).
    override suspend fun reauthenticate(password: String): Result<Unit> = runCatching {
        // Usuario de Firebase de la sesión.
        val firebaseUser = auth.currentUser ?: throw AuthException.NotAuthenticated
        // Credencial de correo y contraseña.
        val credential = EmailAuthProvider.getCredential(firebaseUser.email.orEmpty(), password)
        // Reautentica en Firebase.
        firebaseUser.reauthenticate(credential).await()
        // Resultado exitoso.
        Unit
    // Traduce los errores de Firebase Auth.
    }.mapAuthError()

    // Elimina la identidad del usuario en Firebase Auth (el perfil se anonimiza aparte).
    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        // Usuario de Firebase de la sesión.
        val firebaseUser = auth.currentUser ?: throw AuthException.NotAuthenticated
        // Borra la cuenta en Firebase Auth.
        firebaseUser.delete().await()
        // Limpia la sesión local.
        logout()
    // Traduce los errores de Firebase Auth.
    }.mapAuthError()


    // Traduce los errores de Firebase Auth a los del dominio; los demás (por ejemplo, sin internet) siguen igual.
    private fun <T> Result<T>.mapAuthError(): Result<T> = recoverCatching { error ->
        // Lanza el error del dominio que corresponda.
        throw when (error) {
            // Correo ya registrado.
            is FirebaseAuthUserCollisionException -> AuthException.EmailAlreadyInUse
            // Contraseña demasiado débil.
            is FirebaseAuthWeakPasswordException -> AuthException.WeakPassword
            // Mismo error si el correo no existe o la contraseña está mal (no revela qué correos existen).
            is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException -> AuthException.WrongPassword
            // Otros errores (por ejemplo, de red) se dejan tal cual.
            else -> error
        }
    }

    // Constantes privadas de la clase.
    private companion object {
        // Nombre del archivo de SharedPreferences de la sesión.
        const val PREFS_NAME = "auth_session"
        // Clave donde se guarda el uid con A2F pendiente.
        const val KEY_PENDING_TWO_FACTOR = "pending_two_factor_uid"
    }
}
