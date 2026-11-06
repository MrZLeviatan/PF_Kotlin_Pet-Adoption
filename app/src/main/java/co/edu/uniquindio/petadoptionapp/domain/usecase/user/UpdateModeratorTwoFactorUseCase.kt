package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Caso de uso que activa o desactiva la verificación en dos pasos del moderador con sesión.
Los moderadores no tienen ajustes de usuario (privacidad), así que el dato vive en su perfil de moderador.
Lo usa el ViewModel de los ajustes del moderador.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class UpdateModeratorTwoFactorUseCase(
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase,
    // Inyecta el repositorio de usuarios, donde viven los moderadores.
    private val userRepository: UserRepository
) {
    // Función principal: guarda si el moderador quiere A2F al iniciar sesión.
    suspend operator fun invoke(enabled: Boolean): Result<Unit> {
        // Exige un moderador activo; si no lo es, devuelve el error.
        val moderator = requireModerator().getOrElse { return Result.failure(it) }
        // Actualiza el dato en el perfil del moderador en Firestore.
        return userRepository.updateModeratorTwoFactor(moderator.id, enabled)
    }
}
