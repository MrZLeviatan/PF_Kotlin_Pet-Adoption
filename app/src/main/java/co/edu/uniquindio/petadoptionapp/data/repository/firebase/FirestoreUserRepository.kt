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
 */
@Singleton
class FirestoreUserRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val authRepository: AuthRepository,
) : UserRepository {

    private val users = db.collection(USERS)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Escucha en vivo el perfil de quien tiene la sesión: puntos, insignias y ajustes se actualizan solos.
    init {
        @OptIn(ExperimentalCoroutinesApi::class)
        scope.launch {
            authRepository.currentAccount
                .map { account -> account?.takeIf { it.role == UserRole.USER }?.id }
                .distinctUntilChanged()
                .flatMapLatest { id -> if (id == null) flowOf(null) else observeUser(id) }
                .collect { _currentUser.value = it }
        }
    }

    private fun observeUser(id: String): Flow<User?> = callbackFlow {
        val registration = users.document(id).addSnapshotListener { snapshot, _ ->
            // Con error (por ejemplo, al cerrar sesión) se conserva el último valor.
            if (snapshot != null) trySend(snapshot.toModel<User>())
        }
        awaitClose { registration.remove() }
    }

    // El alias en minúsculas permite buscar por prefijo sin importar mayúsculas.
    private fun User.toDocument(): Map<String, Any?> =
        toFirestore() + (USER_NAME_LOWER to account.userName.lowercase())

    override suspend fun save(user: User): Result<Unit> = runCatching {
        val uid = user.account.id
        val owner = mapOf("uid" to uid)
        db.batch().apply {
            set(users.document(uid), user.toDocument())
            set(db.collection(USER_NAMES).document(user.account.userName.lowercase()), owner)
            set(db.collection(PHONES).document(user.phone), owner)
            set(db.collection(EMAILS).document(user.account.email.lowercase()), owner)
        }.commit().await()
        Unit
    }

    override suspend fun findById(id: String): User? {
        val user = runCatching { users.document(id).get().await().toModel<User>() }.getOrNull()
        // Si es el perfil de la sesión, queda listo antes de que llegue el primer aviso del listener.
        if (user != null && authRepository.currentAccount.value?.id == id) _currentUser.value = user
        return user
    }

    override suspend fun findModeratorById(id: String): Moderator? =
        runCatching { db.collection(MODERATORS).document(id).get().await().toModel<Moderator>() }.getOrNull()

    override suspend fun updateModeratorTwoFactor(id: String, enabled: Boolean): Result<Unit> = runCatching {
        val ref = db.collection(MODERATORS).document(id)
        if (!ref.get().await().exists()) throw UserException.ProfileNotFound
        ref.update(mapOf("twoFactorEnabled" to enabled, "updatedAt" to System.currentTimeMillis())).await()
        Unit
    }

    override suspend fun searchByUserName(query: String, limit: Int): Result<List<UserSummary>> = runCatching {
        val q = query.trim().lowercase()
        // Prefijo: todo lo que esté entre "q" y "q" seguido del último carácter posible.
        users.whereGreaterThanOrEqualTo(USER_NAME_LOWER, q)
            .whereLessThan(USER_NAME_LOWER, q + '')
            .orderBy(USER_NAME_LOWER)
            .limit(limit.toLong() + 5)  // Margen por las cuentas eliminadas que se descartan.
            .get().await()
            .toModels<User>()
            .filter { !it.deleted }
            .take(limit)
            .map { UserSummary(it.account.id, it.account.userName, it.photoUrl) }
    }

    override suspend fun existsByUserName(userName: String): Boolean =
        db.collection(USER_NAMES).document(userName.trim().lowercase()).get().await().exists()

    override suspend fun existsByPhone(phone: String, excludeId: String): Boolean {
        // Un teléfono vacío (cuenta anonimizada) nunca cuenta como duplicado.
        if (phone.isBlank()) return false
        val owner = db.collection(PHONES).document(phone.trim()).get().await()
        return owner.exists() && owner.getString("uid") != excludeId
    }

    override suspend fun updateProfile(
        id: String,
        name: String,
        phone: String,
        photoUrl: String?,
        location: Location
    ): Result<Unit> = runCatching {
        val ref = users.document(id)
        db.runTransaction { tx ->
            val current = tx.get(ref).toModel<User>() ?: throw UserException.ProfileNotFound
            // Si cambió el teléfono, el índice pasa al número nuevo.
            if (current.phone != phone) {
                if (current.phone.isNotBlank()) tx.delete(db.collection(PHONES).document(current.phone))
                tx.set(db.collection(PHONES).document(phone), mapOf("uid" to id))
            }
            // Solo toca estos campos: no pisa puntos ni ajustes.
            tx.update(ref, mapOf(
                "name" to name,
                "phone" to phone,
                "photoUrl" to photoUrl,
                "location" to location.toFirestore(),
                "updatedAt" to System.currentTimeMillis()
            ))
        }.await()
        Unit
    }.unwrapTransactionError()

    override suspend fun updateSettings(id: String, settings: UserSettings): Result<Unit> = runCatching {
        val ref = users.document(id)
        if (!ref.get().await().exists()) throw UserException.ProfileNotFound
        ref.update(mapOf("settings" to settings.toFirestore(), "updatedAt" to System.currentTimeMillis())).await()
        Unit
    }

    override suspend fun anonymize(id: String, feedback: AccountDeletionFeedback?): Result<Unit> = runCatching {
        val ref = users.document(id)
        val user = ref.get().await().toModel<User>() ?: throw UserException.ProfileNotFound
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
            if (user.phone.isNotBlank()) delete(db.collection(PHONES).document(user.phone))
            if (user.account.email.isNotBlank()) delete(db.collection(EMAILS).document(user.account.email.lowercase()))
            // La opinión se guarda sin el id de la persona (es anónima).
            feedback?.let { set(db.collection(DELETION_FEEDBACK).document(), it.toFirestore()) }
        }.commit().await()
        Unit
    }

    private companion object {
        // Alias que reemplaza al real en una cuenta eliminada (la UI lo traduce con strings.xml).
        const val ANONYMOUS_NAME = "deleted_user"
        const val USER_NAME_LOWER = "userNameLower"
    }
}
