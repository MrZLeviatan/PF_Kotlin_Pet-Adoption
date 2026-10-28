package co.edu.uniquindio.petadoptionapp.domain.repository

import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.model.users.Moderator
import co.edu.uniquindio.petadoptionapp.domain.model.users.User
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.AccountDeletionFeedback
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSettings
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSummary
import kotlinx.coroutines.flow.StateFlow

/**
Contrato de perfiles ([User] y [Moderator]) guardados en Firestore.
Las validaciones de formato y reglas de flujo viven en los casos de uso, aquí solo se ejecuta.
 */
interface UserRepository {

    // Perfil actual

    /**
    Flujo reactivo del perfil de quien tiene la sesión abierta.

    - Qué emite: Un [StateFlow] con el [User] activo, o `null` si no hay sesión o la cuenta es de moderador.
    Evita consultar Firestore en cada pantalla (ahorra lecturas de la cuota gratuita).
     */
    val currentUser: StateFlow<User?>


    // Crear y consultar

    /**
    Guarda el perfil de un usuario nuevo.

    - Qué envía ([user]): El perfil completo, con el mismo id de su cuenta de Auth.
    - Qué recibe: Un [Result<Unit>] que confirma si se guardó.
    Se llama una sola vez, justo después del registro.
     */
    suspend fun save(user: User): Result<Unit>

    /**
    Busca el perfil de una persona.

    - Qué envía ([id]): El identificador de la cuenta (`Account.id`).
    - Qué recibe: El [User] o `null` si no existe. No encontrarlo es un caso normal, no un error.
     */
    suspend fun findById(id: String): User?

    /**
    Busca el perfil de un moderador (solo lectura: los moderadores se pre-cargan en la base de datos).

    - Qué envía ([id]): El identificador de la cuenta.
    - Qué recibe: Él [Moderator] o `null` si no existe.
     */
    suspend fun findModeratorById(id: String): Moderator?


    /**
    Busca perfiles cuyo alias empiece por el texto escrito.

    - Qué envía: El [query] (lo que escribió la persona) y el [limit] de resultados.
    - Qué recibe: Un [Result] con la lista de [UserSummary] que coinciden.
    Excluye cuentas eliminadas (anonimizadas).
     */
    suspend fun searchByUserName(query: String, limit: Int): Result<List<UserSummary>>


    // Duplicados

    /**
    Verifica si un alias ya está en uso.

    - Qué envía ([userName]): El alias a consultar.
    - Qué recibe: `true` si ya pertenece a alguien.
     */
    suspend fun existsByUserName(userName: String): Boolean

    /**
    Verifica si un teléfono ya está asociado a una cuenta.

    - Qué envía: Él [phone] a consultar y, opcionalmente, un [excludeId] para ignorar a esa persona (sirve cuando alguien edita su propio teléfono).
    - Qué recibe: `true` si pertenece a otra persona.
     */
    suspend fun existsByPhone(phone: String, excludeId: String = ""): Boolean


    // Modificar

    /**
    Actualiza los datos personales del perfil.

    - Qué envía: El [id] de la cuenta, el [name], el [phone], la [photoUrl] (ya subida a Cloudinary) y la [location].
    - Qué recibe: Un [Result<Unit>] que confirma si se guardó.
    Solo toca esos campos: no pisa los puntos ni los ajustes, que pueden cambiar por otras acciones.
     */
    suspend fun updateProfile(
        id: String,
        name: String,
        phone: String,
        photoUrl: String?,
        location: Location
    ): Result<Unit>

    /**
    Actualiza las preferencias de privacidad y seguridad.

    - Qué envía: El [id] de la cuenta y los [settings] nuevos.
    - Qué recibe: Un [Result<Unit>] que confirma si se guardó.
    Solo toca los ajustes.
     */
    suspend fun updateSettings(id: String, settings: UserSettings): Result<Unit>


    // Eliminar

    /**
    Anonimiza el perfil: borra los datos personales y lo marca como eliminado.
    Conserva id y reputación; vacía nombre, teléfono, foto y ubicación; el alias pasa a ser anónimo. La cuenta de Auth se borra aparte con AuthRepository

    - Qué envía: El [id] de la cuenta y un [feedback] opcional.
    - Qué recibe: Un [Result<Unit>] que confirma si se aplicó.
     */
    suspend fun anonymize(id: String, feedback: AccountDeletionFeedback?): Result<Unit>
}