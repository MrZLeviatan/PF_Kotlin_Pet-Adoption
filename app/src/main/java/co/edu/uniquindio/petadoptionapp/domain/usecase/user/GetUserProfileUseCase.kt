package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.PublicProfile
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Devuelve lo que una persona puede ver del perfil de otra.
- Qué recibe: El [userId] del perfil buscado y el [viewerId] de quien lo consulta.
- Qué devuelve: Un [PublicProfile]. Si el perfil es privado, nombre, teléfono,
reputación y publicaciones llegan en `null` (lo resuelve `User.toPublicProfile`).
Un moderador lo ve completo, aunque sea privado (lo necesita para revisar).
Caso de uso de la pantalla de perfil ajeno: lo usa su ViewModel.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class GetUserProfileUseCase(
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository
) {
    // Define la función asíncrona principal que recibe el ID de usuario y el ID del espectador, devolviendo un resultado con el perfil público.
    suspend operator fun invoke(userId: String, viewerId: String): Result<PublicProfile> {

        // Busca el perfil en el repositorio usando el ID y retorna un fallo si no existe.
        val user = userRepository.findById(userId)
            // Si no existe, falla con UserNotFound.
            ?: return Result.failure(UserException.UserNotFound)
        // Eliminada: se avisa distinto a "no existe" (ej. al abrirla desde un comentario viejo).
        if (user.deleted) return Result.failure(UserException.UserDeleted)

        // Comprueba si quien mira es un moderador (tiene perfil en la colección de moderadores).
        val viewerIsModerator = userRepository.findModeratorById(viewerId) != null
        // Convierte el usuario a perfil público aplicando la privacidad según el espectador y lo retorna como éxito.
        return Result.success(user.toPublicProfile(viewerId, fullAccess = viewerIsModerator))
    }
}
