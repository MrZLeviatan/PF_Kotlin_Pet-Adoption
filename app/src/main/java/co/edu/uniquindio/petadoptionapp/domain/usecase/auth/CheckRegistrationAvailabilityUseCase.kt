package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/** Qué datos del primer paso del registro ya usa otra cuenta. */
data class RegistrationAvailability(val emailTaken: Boolean, val phoneTaken: Boolean) {
    val isAvailable: Boolean get() = !emailTaken && !phoneTaken
}

/**
Documenta el caso de uso que revisa, antes de pasar al segundo paso del registro, si el correo o el
teléfono ya los usa otra cuenta. Así la persona lo corrige al momento y no al final del formulario.
RegisterUseCase lo vuelve a revisar al crear la cuenta (pudo registrarse alguien entre tanto).
 */
class CheckRegistrationAvailabilityUseCase(
    // Inyecta el repositorio de autenticación (los correos los conoce Firebase Auth).
    private val authRepository: AuthRepository,
    // Inyecta el repositorio de usuarios (el teléfono es parte del perfil).
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, phone: String): Result<RegistrationAvailability> = runCatching {
        RegistrationAvailability(
            emailTaken = authRepository.isEmailRegistered(email.trim().lowercase()),
            phoneTaken = userRepository.existsByPhone(phone.trim()),
        )
    }
}
