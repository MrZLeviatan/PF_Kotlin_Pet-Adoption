package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSettings
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Guarda las preferencias de privacidad (perfil público o privado) y seguridad (A2F).
Es corto, pero mantiene la regla: el ViewModel solo habla con casos de uso.
 */
class UpdateSettingsUseCase(
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository
) {
    // Define la función asíncrona principal que recibe el ID de usuario y la nueva configuración, devolviendo un resultado vacío.
    suspend operator fun invoke(userId: String, settings: UserSettings): Result<Unit> =
        // Llama al repositorio para actualizar la configuración asociada al ID de usuario.
        userRepository.updateSettings(userId, settings)
}