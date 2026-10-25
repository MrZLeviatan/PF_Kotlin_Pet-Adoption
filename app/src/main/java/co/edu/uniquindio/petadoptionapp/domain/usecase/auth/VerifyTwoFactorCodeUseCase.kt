package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.checkProfile

/**
Documenta el caso de uso que gestiona el segundo paso del login mediante la verificación del código de 6 dígitos.
Define la clase del caso de uso para verificar el código de doble factor.
 */
class VerifyTwoFactorCodeUseCase(
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository,
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository
) {
    // Define la función asíncrona principal que recibe el ID de cuenta y el código, devolviendo un resultado con la cuenta.
    suspend operator fun invoke(accountId: String, code: String): Result<Account> {

        // Válida si el código ingresado cumple con el formato correcto.
        if (!AuthValidator.isCodeValid(code)) {
            // Retorna un fallo si el formato del código no es válido.
            return Result.failure(AuthException.InvalidCode)
        }

        // Envía el código limpio y el ID de cuenta al repositorio para que el servidor lo verifique.
        val account = authRepository.verifyTwoFactorCode(accountId, code.trim())
            // Obtiene la cuenta resultante o retorna el error si la verificación falla.
            .getOrElse { return Result.failure(it) }

        // Verifica si el perfil del usuario existe y se encuentra activo.
        val error = userRepository.checkProfile(account)
        // Comprueba si la validación del perfil no arrojó ningún error.
        return if (error == null) {
            // Retorna el éxito con la cuenta verificada.
            Result.success(account)
        } else {
            // Cierra la sesión activa en el repositorio para evitar dejarla a medias.
            authRepository.logout()   // no dejamos la sesión a medias
            // Retorna el error encontrado como un fallo.
            Result.failure(error)
        }
    }
}