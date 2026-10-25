package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository

// Define la clase del caso de uso para cambiar la contraseña inyectando el repositorio de autenticación.
class ChangePasswordUseCase(
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository
) {
    // Define la función asíncrona principal que recibe la contraseña actual y la nueva, devolviendo un resultado vacío.
    suspend operator fun invoke(currentPassword: String, newPassword: String): Result<Unit> {
        // Válida si la nueva contraseña cumple con los requisitos mínimos de seguridad.
        if (!AuthValidator.isPasswordValid(newPassword))
        // Retorna un fallo si la nueva contraseña es débil.
            return Result.failure(AuthException.WeakPassword)
        // Comprueba si la contraseña actual y la nueva son exactamente iguales.
        if (currentPassword == newPassword)
        // Retorna un fallo si la contraseña nueva es igual a la actual.
            return Result.failure(AuthException.SamePassword)

        // El repositorio reautentica con la actual y luego actualiza.
        // Llama al repositorio para realizar el cambio de contraseña.
        return authRepository.changePassword(currentPassword, newPassword)
    }
}