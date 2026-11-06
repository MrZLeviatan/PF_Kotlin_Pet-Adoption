package co.edu.uniquindio.petadoptionapp.data.repository.firebase

import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.DELETION_FEEDBACK
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.EMAILS
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.MODERATORS
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.PHONES
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.USERS
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.USER_NAMES
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.model.users.Moderator
import co.edu.uniquindio.petadoptionapp.domain.model.users.User
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.AccountDeletionFeedback
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSettings
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSummary
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
Perfiles en Firestore: `users/{uid}` y `moderators/{uid}`.

Los valores únicos (correo, alias y teléfono) tienen además su propio documento en `emails`, `userNames`
y `phones`, con el uid del dueño. Así se revisa si ya existen sin dejar leer los perfiles a cualquiera,
y las reglas impiden que dos cuentas guarden el mismo valor (solo se pueden crear, no sobrescribir).

Implementa UserRepository del dominio y se enlaza en RepositoryModule. Además de las lecturas y
escrituras de perfiles, mantiene `currentUser`: un StateFlow con el perfil de la sesión que se
escucha en vivo y que observan los ViewModels (perfil, métricas, configuración). Depende de
AuthRepository para saber qué cuenta tiene la sesión. Los casos de uso de registro, edición de
perfil, búsqueda de usuarios y eliminación de cuenta (anonimización) lo usan.
**/
// Una sola instancia: el listener del perfil de la sesión se registra una sola vez.
@Singleton
// Hilt inyecta Firestore y el repositorio de autenticación.
class FirestoreUserRepository @Inject constructor(
    // Base de datos Firestore.
    private val db: FirebaseFirestore,
    // Repositorio de autenticación: informa qué cuenta tiene la sesión activa.
    private val authRepository: AuthRepository,
) : UserRepository {

    // Referencia a la colección de perfiles de usuario.
    private val users = db.collection(USERS)
    // Alcance propio de corrutinas para escuchar la sesión mientras viva la app; SupervisorJob aísla los fallos.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Estado interno con el perfil del usuario de la sesión (null si no hay sesión o es moderador).
    private val _currentUser = MutableStateFlow<User?>(null)
    // Vista de solo lectura del perfil de la sesión que observan las pantallas.
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Escucha en vivo el perfil de quien tiene la sesión: puntos, insignias y ajustes se actualizan solos.
    init {
        // Habilita flatMapLatest, que aún es una API experimental de corrutinas.
        @OptIn(ExperimentalCoroutinesApi::class)
        // Lanza la observación en segundo plano.
        scope.launch {
            // Parte de la cuenta autenticada actual (cambia al iniciar o cerrar sesión).
            authRepository.currentAccount
                // Obtiene el id solo si la cuenta es de un usuario común (los moderadores tienen otro perfil).
                .map { account -> account?.takeIf { it.role == UserRole.USER }?.id }
                // Ignora emisiones repetidas del mismo id para no reiniciar el listener sin necesidad.
                .distinctUntilChanged()
                // Cambia al listener del nuevo perfil (cancelando el anterior); sin sesión emite null.
                .flatMapLatest { id -> if (id == null) flowOf(null) else observeUser(id) }
                // Publica cada versión del perfil en el estado.
                .collect { _currentUser.value = it }
        }
    }

    // Convierte el listener de Firestore de un perfil en un Flow.
    private fun observeUser(id: String): Flow<User?> = callbackFlow {
        // Registra el listener en vivo sobre el documento del usuario.
        val registration = users.document(id).addSnapshotListener { snapshot, _ ->
            // Con error (por ejemplo, al cerrar sesión) se conserva el último valor.
            if (snapshot != null) trySend(snapshot.toModel<User>())
        }
        // Al dejar de observar, quita el listener para no gastar lecturas.
        awaitClose { registration.remove() }
    }

    // Convierte el usuario en documento agregando su alias en minúsculas.
    // El alias en minúsculas permite buscar por prefijo sin importar mayúsculas.
    private fun User.toDocument(): Map<String, Any?> =
        toFirestore() + (USER_NAME_LOWER to account.userName.lowercase())

    // Guarda un perfil nuevo junto con sus índices de valores únicos.
    override suspend fun save(user: User): Result<Unit> = runCatching {
        // Uid de la cuenta, que es el id del documento del perfil.
        val uid = user.account.id
        // Contenido de los documentos índice: solo el uid del dueño.
        val owner = mapOf("uid" to uid)
        // Lote: el perfil y sus índices se escriben juntos o no se escribe nada.
        db.batch().apply {
            // Guarda el perfil.
            set(users.document(uid), user.toDocument())
            // Reserva el alias (en minúsculas) para este uid.
            set(db.collection(USER_NAMES).document(user.account.userName.lowercase()), owner)
            // Reserva el teléfono para este uid.
            set(db.collection(PHONES).document(user.phone), owner)
            // Reserva el correo (en minúsculas) para este uid.
            set(db.collection(EMAILS).document(user.account.email.lowercase()), owner)
        // Confirma el lote.
        }.commit().await()
        // Resultado exitoso.
        Unit
    }

    // Busca un perfil de usuario por id.
    override suspend fun findById(id: String): User? {
        // Lee el documento; null si no existe o falla la lectura.
        val user = runCatching { users.document(id).get().await().toModel<User>() }.getOrNull()
        // Si es el perfil de la sesión, queda listo antes de que llegue el primer aviso del listener.
        if (user != null && authRepository.currentAccount.value?.id == id) _currentUser.value = user
        // Devuelve el perfil encontrado (o null).
        return user
    }

    // Busca un perfil de moderador por id; null si no existe o falla la lectura.
    override suspend fun findModeratorById(id: String): Moderator? =
        runCatching { db.collection(MODERATORS).document(id).get().await().toModel<Moderator>() }.getOrNull()

    // Activa o desactiva la verificación en dos pasos de un moderador.
    override suspend fun updateModeratorTwoFactor(id: String, enabled: Boolean): Result<Unit> = runCatching {
        // Referencia al perfil del moderador.
        val ref = db.collection(MODERATORS).document(id)
        // Si no existe, informa el error del dominio.
        if (!ref.get().await().exists()) throw UserException.ProfileNotFound
        // Actualiza solo la bandera de A2F y la fecha de modificación.
        ref.update(mapOf("twoFactorEnabled" to enabled, "updatedAt" to System.currentTimeMillis())).await()
        // Resultado exitoso.
        Unit
    }

    // Busca usuarios cuyo alias empiece por el texto escrito.
    override suspend fun searchByUserName(query: String, limit: Int): Result<List<UserSummary>> = runCatching {
        // Normaliza el texto: sin espacios en los extremos y en minúsculas.
        val q = query.trim().lowercase()
        // Prefijo: todo lo que esté entre "q" y "q" seguido del último carácter posible.
        // Primer límite: desde "q" en adelante (orden alfabético).
        users.whereGreaterThanOrEqualTo(USER_NAME_LOWER, q)
            // Hasta antes de "q" seguido del carácter Unicode más alto (U+F8FF), lo que equivale a "empieza por q".
            .whereLessThan(USER_NAME_LOWER, q + '')
            // Ordena por alias en minúsculas (obligatorio al usar rangos sobre ese campo).
            .orderBy(USER_NAME_LOWER)
            // Margen por las cuentas eliminadas que se descartan.
            .limit(limit.toLong() + 5)
            // Ejecuta la consulta.
            .get().await()
            // Convierte los documentos en usuarios.
            .toModels<User>()
            // Descarta las cuentas eliminadas.
            .filter { !it.deleted }
            // Recorta al límite pedido.
            .take(limit)
            // Devuelve solo el resumen público (id, alias y foto).
            .map { UserSummary(it.account.id, it.account.userName, it.photoUrl) }
    }

    // Indica si un alias ya está en uso, consultando su documento índice.
    override suspend fun existsByUserName(userName: String): Boolean =
        db.collection(USER_NAMES).document(userName.trim().lowercase()).get().await().exists()

    // Indica si un teléfono ya pertenece a otra cuenta distinta de excludeId.
    override suspend fun existsByPhone(phone: String, excludeId: String): Boolean {
        // Un teléfono vacío (cuenta anonimizada) nunca cuenta como duplicado.
        if (phone.isBlank()) return false
        // Lee el documento índice del teléfono.
        val owner = db.collection(PHONES).document(phone.trim()).get().await()
        // Es duplicado si existe y su dueño no es la cuenta excluida (la propia al editar).
        return owner.exists() && owner.getString("uid") != excludeId
    }

    // Actualiza los datos editables del perfil.
    override suspend fun updateProfile(
        // Id del usuario.
        id: String,
        // Nombre completo.
        name: String,
        // Teléfono.
        phone: String,
        // URL de la foto (null para quitarla).
        photoUrl: String?,
        // Ubicación.
        location: Location
    ): Result<Unit> = runCatching {
        // Referencia al perfil.
        val ref = users.document(id)
        // Transacción: perfil e índice del teléfono cambian juntos.
        db.runTransaction { tx ->
            // Lee el perfil actual; si no existe, falla.
            val current = tx.get(ref).toModel<User>() ?: throw UserException.ProfileNotFound
            // Si cambió el teléfono, el índice pasa al número nuevo.
            if (current.phone != phone) {
                // Libera el índice del teléfono anterior si tenía uno.
                if (current.phone.isNotBlank()) tx.delete(db.collection(PHONES).document(current.phone))
                // Reserva el nuevo teléfono para este uid.
                tx.set(db.collection(PHONES).document(phone), mapOf("uid" to id))
            }
            // Solo toca estos campos: no pisa puntos ni ajustes.
            // Actualiza nombre, teléfono, foto, ubicación y fecha de modificación.
            tx.update(ref, mapOf(
                "name" to name,
                "phone" to phone,
                "photoUrl" to photoUrl,
                "location" to location.toFirestore(),
                "updatedAt" to System.currentTimeMillis()
            ))
        // Espera a que la transacción termine.
        }.await()
        // Resultado exitoso.
        Unit
    // Extrae el error de dominio original si la transacción lo envolvió.
    }.unwrapTransactionError()

    // Guarda los ajustes (privacidad, notificaciones, etc.) del usuario.
    override suspend fun updateSettings(id: String, settings: UserSettings): Result<Unit> = runCatching {
        // Referencia al perfil.
        val ref = users.document(id)
        // Si no existe, informa el error del dominio.
        if (!ref.get().await().exists()) throw UserException.ProfileNotFound
        // Reemplaza el objeto de ajustes y actualiza la fecha de modificación.
        ref.update(mapOf("settings" to settings.toFirestore(), "updatedAt" to System.currentTimeMillis())).await()
        // Resultado exitoso.
        Unit
    }

    // Anonimiza la cuenta al eliminarla: borra los datos personales pero conserva el documento.
    override suspend fun anonymize(id: String, feedback: AccountDeletionFeedback?): Result<Unit> = runCatching {
        // Referencia al perfil.
        val ref = users.document(id)
        // Lee el perfil actual para saber qué índices liberar; si no existe, falla.
        val user = ref.get().await().toModel<User>() ?: throw UserException.ProfileNotFound
        // Lote: todos los cambios se aplican juntos.
        db.batch().apply {
            // Conserva id y reputación; vacía los datos personales.
            update(ref, mapOf(
                "account.email" to "",
                "account.userName" to ANONYMOUS_NAME,
                USER_NAME_LOWER to "",
                "name" to "",
                "phone" to "",
                "photoUrl" to null,
                "location" to Location().toFirestore(),
                "settings" to UserSettings().toFirestore(),
                "deleted" to true,
                "deletedAt" to System.currentTimeMillis()
            ))
            // Libera el alias, el teléfono y el correo para que otra persona los pueda usar.
            delete(db.collection(USER_NAMES).document(user.account.userName.lowercase()))
            // Libera el teléfono si tenía uno.
            if (user.phone.isNotBlank()) delete(db.collection(PHONES).document(user.phone))
            // Libera el correo si tenía uno.
            if (user.account.email.isNotBlank()) delete(db.collection(EMAILS).document(user.account.email.lowercase()))
            // La opinión se guarda sin el id de la persona (es anónima).
            feedback?.let { set(db.collection(DELETION_FEEDBACK).document(), it.toFirestore()) }
        // Confirma el lote.
        }.commit().await()
        // Resultado exitoso.
        Unit
    }

    // Constantes privadas de la clase.
    private companion object {
        // Alias que reemplaza al real en una cuenta eliminada (la UI lo traduce con strings.xml).
        const val ANONYMOUS_NAME = "deleted_user"
        // Campo con el alias en minúsculas, usado para búsquedas sin distinguir mayúsculas.
        const val USER_NAME_LOWER = "userNameLower"
    }
}
