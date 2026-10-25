package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSettings
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Guarda las preferencias de privacidad (perfil público o privado) y seguridad (A2F).
Es corto, pero mantiene la regla: el ViewModel solo habla con casos de uso.
 */
class UpdateSettingsUseCase(
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository,
    // Inyecta el caso de uso que confirma que quien pide es el titular.
    private val requireOwnUser: RequireOwnUserUseCase

) {
    // Define la función asíncrona principal que recibe el ID de usuario y la nueva configuración, devolviendo un resultado vacío.
    suspend operator fun invoke(userId: String, settings: UserSettings): Result<Unit> {

        // Solo el titular de la sesión puede cambiar sus ajustes.
        requireOwnUser(userId).getOrElse { return Result.failure(it) }

        // El perfil debe existir y estar activo.
        val user = userRepository.findById(userId)
        if (user == null || user.deleted) return Result.failure(UserException.ProfileNotFound)

        return userRepository.updateSettings(userId, settings)

    }
}