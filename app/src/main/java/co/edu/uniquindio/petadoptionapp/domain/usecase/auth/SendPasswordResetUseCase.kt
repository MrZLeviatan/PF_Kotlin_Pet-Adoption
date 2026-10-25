package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository

/**
Documenta el caso de uso para solicitar el correo de recuperación de contraseña.
"Olvidé mi contraseña": válida el email y pide el correo de recuperación.
 */
class SendPasswordResetUseCase(
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository
) {
    // Define la función asíncrona principal que recibe el correo y devuelve un resultado vacío.
    suspend operator fun invoke(email: String): Result<Unit> {
        // Válida si el formato del correo electrónico es correcto antes de llamar al servidor.
        if (!AuthValidator.isEmailValid(email))
        // Retorna un fallo si el correo no es válido.
            return Result.failure(AuthException.InvalidEmail)

        // Envía la solicitud de restablecimiento de contraseña al repositorio con el correo limpio de espacios.
        return authRepository.sendPasswordReset(email.trim())
    }
}