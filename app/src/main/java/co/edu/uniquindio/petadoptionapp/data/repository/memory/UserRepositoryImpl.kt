package co.edu.uniquindio.petadoptionapp.data.repository.memory

import co.edu.uniquindio.petadoptionapp.data.repository.send.SeedData
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Reputation
import co.edu.uniquindio.petadoptionapp.domain.model.users.Moderator
import co.edu.uniquindio.petadoptionapp.domain.model.users.User
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.AccountDeletionFeedback
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSettings
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSummary
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
Documenta la implementación en memoria del repositorio de perfiles.
Simula Firestore: los perfiles viven en un mapa temporal y se pierden al cerrar la app.
Solo ejecuta: las validaciones y los permisos viven en los casos de uso.
 */
// Anota la clase como un singleton para que Hilt gestione una única instancia compartida en toda la aplicación.
@Singleton
class UserRepositoryImpl @Inject constructor(
    // Inyecta el repositorio de autenticación para seguir la sesión actual.
    authRepository: AuthRepository
) : UserRepository {

    // Guarda los perfiles de usuario en un flujo de estado, con el id como clave.
    private val users = MutableStateFlow<Map<String, User>>(emptyMap())

    // Guarda los moderadores precargados indexados por el ID de su cuenta (solo cambia su verificación en dos pasos).
    private val moderators = MutableStateFlow(SeedData.moderators.associateBy { it.account.id })

    // Define el ámbito de corrutinas con un trabajo supervisor y despachador predeterminado donde vive el flujo derivado de la sesión.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Combina la sesión con los perfiles: si la cuenta es de usuario, emite su perfil; si no, null.
    // Mantiene el flujo sincronizado de forma ávida con el ciclo de vida del ámbito definido.
    override val currentUser: StateFlow<User?> =
        combine(authRepository.currentAccount, users) { account, profiles ->
            if (account?.role == UserRole.USER) profiles[account.id] else null
        }.stateIn(scope, SharingStarted.Eagerly, null)

    // Carga los usuarios de prueba al crear la instancia mapeándolos por su ID.
    init {
        users.value = SeedData.users.associateBy { it.account.id }
    }

    // Define la función asíncrona para guardar o actualizar un perfil de usuario en el mapa de estado, retornando un resultado vacío.
    override suspend fun save(user: User): Result<Unit> {
        // Actualiza el mapa de usuarios agregando o reemplazando el perfil correspondiente a su ID.
        users.update { it + (user.account.id to user) }
        return Result.success(Unit)
    }

    // Busca y retorna un perfil de usuario por su ID, o nulo si no existe en el mapa actual.
    override suspend fun findById(id: String): User? = users.value[id]

    // Busca y retorna un moderador precargado por su ID, o nulo si no se encuentra.
    override suspend fun findModeratorById(id: String): Moderator? = moderators.value[id]

    override suspend fun updateModeratorTwoFactor(id: String, enabled: Boolean): Result<Unit> {
        if (moderators.value[id] == null) return Result.failure(UserException.ProfileNotFound)
        moderators.update { map -> map[id]?.let { map + (id to it.copy(twoFactorEnabled = enabled, updatedAt = System.currentTimeMillis())) } ?: map }
        return Result.success(Unit)
    }

    // Valida si ya existe un nombre de usuario registrado y no eliminado, ignorando mayúsculas, minúsculas y espacios.
    override suspend fun existsByUserName(userName: String): Boolean =
        users.value.values.any {
            !it.deleted && it.account.userName.equals(userName.trim(), ignoreCase = true)
        }

    // Comprueba si un número de teléfono ya está registrado por otro usuario diferente al excluido y que no tenga cuenta eliminada.
    override suspend fun existsByPhone(phone: String, excludeId: String): Boolean =
        // Un teléfono vacío (cuenta anonimizada) nunca cuenta como duplicado.
        phone.isNotBlank() && users.value.values.any {
            !it.deleted && it.account.id != excludeId && it.phone == phone.trim()
        }

    // Define la función asíncrona para actualizar los datos principales del perfil de un usuario.
    override suspend fun updateProfile(
        id: String,
        name: String,
        phone: String,
        photoUrl: String?,
        location: Location
    ): Result<Unit> {
        // Valida que el perfil del usuario exista en el mapa actual o retorna un fallo si no se encuentra.
        if (users.value[id] == null) return Result.failure(UserException.ProfileNotFound)
        // Solo toca estos campos: no pisa puntos ni ajustes, actualizando la marca de tiempo de modificación.
        users.update { map ->
            map[id]?.let { map + (id to it.copy(
                name = name, phone = phone, photoUrl = photoUrl,
                location = location, updatedAt = System.currentTimeMillis()
            )) } ?: map
        }
        return Result.success(Unit)
    }

    // Define la función asíncrona para actualizar las preferencias y ajustes de configuración de un usuario.
    override suspend fun updateSettings(id: String, settings: UserSettings): Result<Unit> {
        // Valida que el perfil exista o retorna un fallo en caso contrario.
        if (users.value[id] == null) return Result.failure(UserException.ProfileNotFound)
        // Actualiza los ajustes del usuario y la marca de tiempo actual en el mapa de estado.
        users.update { map ->
            map[id]?.let { map + (id to it.copy(settings = settings, updatedAt = System.currentTimeMillis())) } ?: map
        }
        return Result.success(Unit)
    }

    // Define la función asíncrona para anonimizar la cuenta de un usuario al solicitar su eliminación.
    override suspend fun anonymize(id: String, feedback: AccountDeletionFeedback?): Result<Unit> {
        // Valida que el usuario exista o retorna un fallo si no se encuentra el perfil.
        if (users.value[id] == null) return Result.failure(UserException.ProfileNotFound)
        // Conserva id y reputación; vacía los datos personales y marca la cuenta como eliminada junto con su marca de tiempo.
        users.update { map ->
            map[id]?.let { user ->
                map + (id to user.copy(
                    account = user.account.copy(email = "", userName = ANONYMOUS_NAME),
                    name = "", phone = "", photoUrl = null,
                    location = Location(), settings = UserSettings(),
                    deleted = true, deletedAt = System.currentTimeMillis()
                ))
            } ?: map
        }
        // El feedback es anónimo (sin userId); en esta fase solo se descarta.
        return Result.success(Unit)
    }

    // Define la función asíncrona para buscar resúmenes de usuarios por coincidencia de prefijo en su nombre de usuario.
    override suspend fun searchByUserName(query: String, limit: Int): Result<List<UserSummary>> {
        val q = query.trim().lowercase()
        // Solo busca por prefijo, igual que lo hará Firestore en la capa gratuita.
        val results = users.value.values
            .filter { !it.deleted && it.account.userName.lowercase().startsWith(q) }
            .take(limit)
            .map { UserSummary(it.account.id, it.account.userName, it.photoUrl) }
        return Result.success(results)
    }

    // Declara un objeto de acompañamiento privado para constantes internas de la implementación del repositorio.
    private companion object {
        // Alias que reemplaza al real en una cuenta eliminada (la UI lo traduce con strings.xml).
        const val ANONYMOUS_NAME = "deleted_user"
    }


    // Modifica solo la reputación de un perfil activo y devuelve (antes, después); null si no existe o está eliminado.
    fun updateReputation(id: String, transform: (Reputation) -> Reputation): Pair<Reputation, Reputation>? {
        var result: Pair<Reputation, Reputation>? = null
        // El bloque puede repetirse si hay concurrencia; el valor final de `result` es el de la ejecución que se aplicó.
        users.update { map ->
            val user = map[id]
            if (user == null || user.deleted) {
                result = null
                map
            } else {
                val updated = transform(user.reputation)
                result = user.reputation to updated
                map + (id to user.copy(reputation = updated))
            }
        }
        return result
    }
}