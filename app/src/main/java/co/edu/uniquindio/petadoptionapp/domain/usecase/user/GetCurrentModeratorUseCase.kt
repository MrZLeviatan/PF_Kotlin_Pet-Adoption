package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.model.users.Moderator
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Caso de uso que devuelve el perfil del moderador con sesión (para el saludo del inicio).
Lo usa el ViewModel del inicio / perfil del moderador.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class GetCurrentModeratorUseCase(
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase,
    // Inyecta el repositorio de usuarios para leer su perfil.
    private val userRepository: UserRepository
) {
    // Función principal: devuelve el perfil del moderador con sesión.
    suspend operator fun invoke(): Result<Moderator> {
        // Exige un moderador activo; si no lo es, devuelve el error.
        val session = requireModerator().getOrElse { return Result.failure(it) }
        // Busca su perfil y lo devuelve con éxito si existe.
        return userRepository.findModeratorById(session.id)?.let { Result.success(it) }
            // Si no se encuentra, falla con NotModerator.
            ?: Result.failure(AuthException.NotModerator)
    }
}
