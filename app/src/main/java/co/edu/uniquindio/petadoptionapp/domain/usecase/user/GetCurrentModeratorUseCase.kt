package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.model.users.Moderator
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Documenta el caso de uso que devuelve el perfil del moderador con sesión (para el saludo del inicio).
 */
class GetCurrentModeratorUseCase(
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase,
    // Inyecta el repositorio de usuarios para leer su perfil.
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Moderator> {
        val session = requireModerator().getOrElse { return Result.failure(it) }
        return userRepository.findModeratorById(session.id)?.let { Result.success(it) }
            ?: Result.failure(AuthException.NotModerator)
    }
}
