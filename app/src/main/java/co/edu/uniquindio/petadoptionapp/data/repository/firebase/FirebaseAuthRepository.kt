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
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val emailSender: EmailJsCodeSender,
    @ApplicationContext context: Context,
) : AuthRepository {

    private val _currentAccount = MutableStateFlow<Account?>(null)
    override val currentAccount: StateFlow<Account?> = _currentAccount.asStateFlow()

    // Guarda el uid que inició sesión pero aún no escribe el código A2F (sobrevive a cerrar la app).
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Alias de quien espera el código, para saludarlo en el correo sin volver a leer su perfil.
    private var pendingUserName: String = ""

    private val random = SecureRandom()

    // Lo que se necesita del perfil para abrir la sesión.
    private data class Profile(val account: Account, val twoFactorEnabled: Boolean)

    // Lee el perfil de usuario o de moderador. Sin perfil, devuelve una cuenta básica (CheckProfile la rechaza).
    private suspend fun loadProfile(uid: String, email: String?): Profile {
        db.collection(USERS).document(uid).get().await().toModel<User>()?.let {
            return Profile(it.account.copy(id = uid, role = UserRole.USER), it.settings.twoFactorEnabled)
        }
        db.collection(MODERATORS).document(uid).get().await().toModel<Moderator>()?.let {
            return Profile(it.account.copy(id = uid, role = UserRole.MODERATOR), it.twoFactorEnabled)
        }
        return Profile(Account(id = uid, email = email.orEmpty()), twoFactorEnabled = false)
    }


    // Sesión

    override suspend fun restoreSession(): Account? {
        val firebaseUser = auth.currentUser ?: return null
        // Quedó a mitad de la A2F: no cuenta como sesión.
        if (prefs.getString(KEY_PENDING_TWO_FACTOR, null) == firebaseUser.uid) {
            logout()
            return null
        }
        // Sin internet, Firestore responde con su copia local si ya había leído el perfil antes.
        val profile = runCatching { loadProfile(firebaseUser.uid, firebaseUser.email) }.getOrNull() ?: return null
        _currentAccount.value = profile.account
        return profile.account
    }

    override suspend fun logout() {
        auth.signOut()
        prefs.edit { remove(KEY_PENDING_TWO_FACTOR) }
        pendingUserName = ""
        _currentAccount.value = null
    }


    // Login

    override suspend fun login(credentials: Credentials): Result<LoginResult> = runCatching {
        val firebaseUser = auth.signInWithEmailAndPassword(credentials.email.trim(), credentials.password)
            .await().user ?: throw AuthException.NotAuthenticated

        // Si no se puede leer el perfil (sin internet), no se deja la sesión de Firebase a medias.
        val profile = try {
            loadProfile(firebaseUser.uid, firebaseUser.email)
        } catch (e: Exception) {
            auth.signOut()
            throw e
        }

        if (profile.twoFactorEnabled) {
            prefs.edit { putString(KEY_PENDING_TWO_FACTOR, firebaseUser.uid) }
            pendingUserName = profile.account.userName
            LoginResult.TwoFactorRequired(firebaseUser.uid)
        } else {
            _currentAccount.value = profile.account
            LoginResult.Success(profile.account)
        }
    }.mapAuthError()


    // A2F

    override suspend fun requestTwoFactorCode(accountId: String): Result<Unit> {
        val firebaseUser = auth.currentUser?.takeIf { it.uid == accountId }
            ?: return Result.failure(AuthException.NotAuthenticated)
        if (!emailSender.isConfigured) return Result.failure(AuthException.CodeNotSent)

        val code = (1..TwoFactorChallenge.CODE_LENGTH).joinToString("") { random.nextInt(10).toString() }
        val now = System.currentTimeMillis()
        // Un código nuevo reemplaza al anterior (y reinicia los intentos).
        val challenge = TwoFactorChallenge(
            userId = accountId,
            codeHash = hash(accountId, code),
            expiresAt = now + TwoFactorChallenge.TTL_MILLIS,
            createdAt = now
        )
        runCatching { db.collection(TWO_FACTOR).document(accountId).set(challenge.toFirestore()).await() }
            .onFailure { return Result.failure(AuthException.CodeNotSent) }

        return emailSender.sendTwoFactorCode(
            toEmail = firebaseUser.email.orEmpty(),
            userName = pendingUserName,
            code = code,
            minutes = TwoFactorChallenge.TTL_MILLIS / 60_000
        ).recoverCatching { throw AuthException.CodeNotSent }
    }

    override suspend fun verifyTwoFactorCode(accountId: String, code: String): Result<Account> {
        val firebaseUser = auth.currentUser?.takeIf { it.uid == accountId }
            ?: return Result.failure(AuthException.NotAuthenticated)
        val ref = db.collection(TWO_FACTOR).document(accountId)

        return runCatching {
            // Leer, comparar y contar el intento en una transacción: dos toques rápidos no se saltan el límite.
            val error: AuthException? = db.runTransaction { tx ->
                val challenge = tx.get(ref).toModel<TwoFactorChallenge>()
                when {
                    challenge == null || challenge.consumed || challenge.hasExpired() -> AuthException.CodeExpired
                    challenge.hasReachedMaxAttempts() -> AuthException.TooManyAttempts
                    challenge.codeHash != hash(accountId, code) -> {
                        tx.update(ref, "attempts", challenge.attempts + 1)
                        AuthException.WrongCode
                    }
                    else -> {
                        // Un solo uso.
                        tx.update(ref, "consumed", true)
                        null
                    }
                }
            }.await()
            if (error != null) throw error

            val profile = loadProfile(firebaseUser.uid, firebaseUser.email)
            prefs.edit { remove(KEY_PENDING_TWO_FACTOR) }
            pendingUserName = ""
            _currentAccount.value = profile.account
            profile.account
        }
    }

    // SHA-256 del uid y el código: en Firestore nunca queda el código en claro.
    private fun hash(uid: String, code: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest("$uid:$code".toByteArray())
            .joinToString("") { "%02x".format(it) }


    // Crear cuenta

    override suspend fun register(credentials: Credentials): Result<String> = runCatching {
        val email = credentials.email.trim()
        val firebaseUser = auth.createUserWithEmailAndPassword(email, credentials.password).await().user
            ?: throw AuthException.NotAuthenticated
        // Firebase abre la sesión al registrar; el alias llega después con updateSessionAccount.
        _currentAccount.value = Account(id = firebaseUser.uid, email = email, role = UserRole.USER)
        firebaseUser.uid
    }.mapAuthError()

    // Con la protección contra enumeración de Firebase Auth no se puede preguntar por correos: se usa el índice `emails`.
    override suspend fun isEmailRegistered(email: String): Boolean =
        db.collection(EMAILS).document(email.trim().lowercase()).get().await().exists()

    override suspend fun updateSessionAccount(account: Account): Result<Unit> {
        val current = _currentAccount.value
        if (current == null || current.id != account.id) return Result.failure(AuthException.NotAuthenticated)
        _currentAccount.value = current.copy(userName = account.userName, role = account.role)
        return Result.success(Unit)
    }


    // Contraseña

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        try {
            auth.sendPasswordResetEmail(email.trim()).await()
        } catch (_: FirebaseAuthInvalidUserException) {
            // No se revela si el correo existe: la pantalla dice lo mismo en ambos casos.
        }
        Unit
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        reauthenticate(currentPassword).getOrThrow()
        val firebaseUser = auth.currentUser ?: throw AuthException.NotAuthenticated
        firebaseUser.updatePassword(newPassword).await()
        Unit
    }.mapAuthError()


    // Eliminar cuenta

    override suspend fun reauthenticate(password: String): Result<Unit> = runCatching {
        val firebaseUser = auth.currentUser ?: throw AuthException.NotAuthenticated
        val credential = EmailAuthProvider.getCredential(firebaseUser.email.orEmpty(), password)
        firebaseUser.reauthenticate(credential).await()
        Unit
    }.mapAuthError()

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        val firebaseUser = auth.currentUser ?: throw AuthException.NotAuthenticated
        firebaseUser.delete().await()
        logout()
    }.mapAuthError()


    // Traduce los errores de Firebase Auth a los del dominio; los demás (por ejemplo, sin internet) siguen igual.
    private fun <T> Result<T>.mapAuthError(): Result<T> = recoverCatching { error ->
        throw when (error) {
            is FirebaseAuthUserCollisionException -> AuthException.EmailAlreadyInUse
            is FirebaseAuthWeakPasswordException -> AuthException.WeakPassword
            // Mismo error si el correo no existe o la contraseña está mal (no revela qué correos existen).
            is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException -> AuthException.WrongPassword
            else -> error
        }
    }

    private companion object {
        const val PREFS_NAME = "auth_session"
        const val KEY_PENDING_TWO_FACTOR = "pending_two_factor_uid"
    }
}
