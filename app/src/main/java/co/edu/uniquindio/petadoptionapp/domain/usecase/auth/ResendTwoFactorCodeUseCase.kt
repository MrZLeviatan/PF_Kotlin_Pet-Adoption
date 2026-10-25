package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository

/**
 Documenta el caso de uso para solicitar un nuevo código cuando el anterior venció o no llegó.
 - Pide un código nuevo cuando el anterior venció o no llegó.
 - La espera mínima entre reenvíos la controla la pantalla (temporizador) y el servidor (límite real).
 */
class ResendTwoFactorCodeUseCase(
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository
) {
    // Define la función asíncrona principal que recibe el ID de cuenta y devuelve un resultado vacío.
    suspend operator fun invoke(accountId: String): Result<Unit> =
        // Solicita al repositorio que envíe un nuevo código de doble factor usando el ID.
        authRepository.requestTwoFactorCode(accountId)
}