package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Caso de uso que revisa, antes de pasar al segundo paso del registro, si el correo o el
teléfono ya los usa otra cuenta. Así la persona lo corrige al momento y no al final del formulario.
RegisterUseCase lo vuelve a revisar al crear la cuenta (pudo registrarse alguien entre tanto).
También define [RegistrationAvailability], el resultado que recibe el ViewModel del registro.
**/
// Qué datos del primer paso del registro ya usa otra cuenta.
data class RegistrationAvailability(val emailTaken: Boolean, val phoneTaken: Boolean) {
    // Está disponible solo si ni el correo ni el teléfono están ocupados.
    val isAvailable: Boolean get() = !emailTaken && !phoneTaken
}

// Declara el caso de uso con los repositorios que necesita.
class CheckRegistrationAvailabilityUseCase(
    // Inyecta el repositorio de autenticación (los correos los conoce Firebase Auth).
    private val authRepository: AuthRepository,
    // Inyecta el repositorio de usuarios (el teléfono es parte del perfil).
    private val userRepository: UserRepository
) {
    // Función principal: consulta ambos datos y envuelve cualquier excepción en un Result.failure.
    suspend operator fun invoke(email: String, phone: String): Result<RegistrationAvailability> = runCatching {
        // Construye el resultado con las dos comprobaciones.
        RegistrationAvailability(
            // Normaliza el correo (sin espacios, en minúsculas) y pregunta a Auth si ya existe.
            emailTaken = authRepository.isEmailRegistered(email.trim().lowercase()),
            // Quita espacios al teléfono y pregunta a Firestore si otro perfil lo usa.
            phoneTaken = userRepository.existsByPhone(phone.trim()),
        )
    }
}
