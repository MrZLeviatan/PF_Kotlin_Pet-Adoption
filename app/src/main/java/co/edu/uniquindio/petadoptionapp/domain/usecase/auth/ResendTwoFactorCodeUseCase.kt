package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository

/**
Caso de uso para solicitar un nuevo código A2F cuando el anterior venció o no llegó.
- Pide al repositorio que genere y envíe un código nuevo al correo de la cuenta.
- La espera mínima entre reenvíos la controla la pantalla (temporizador) y el servidor (límite real).
Lo usa el ViewModel de la pantalla de verificación en dos pasos (botón "Reenviar código").
**/
// Declara el caso de uso de reenvío del código.
class ResendTwoFactorCodeUseCase(
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository
) {
    // Define la función asíncrona principal que recibe el ID de cuenta y devuelve un resultado vacío.
    suspend operator fun invoke(accountId: String): Result<Unit> =
        // Solicita al repositorio que envíe un nuevo código de doble factor usando el ID.
        authRepository.requestTwoFactorCode(accountId)
}