package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Documenta el caso de uso que activa o desactiva la verificación en dos pasos del moderador con sesión.
Los moderadores no tienen ajustes de usuario (privacidad), así que el dato vive en su perfil de moderador.
 */
class UpdateModeratorTwoFactorUseCase(
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase,
    // Inyecta el repositorio de usuarios, donde viven los moderadores.
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> {
        val moderator = requireModerator().getOrElse { return Result.failure(it) }
        return userRepository.updateModeratorTwoFactor(moderator.id, enabled)
    }
}
