package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.model.auth.Credentials
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.LoginResult
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.CheckProfileUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.AuthValidator

// Define la clase del caso de uso para gestionar el inicio de sesión.
class LoginUseCase(

    // Recibe la dependencia del repositorio de autenticación.
    private val authRepository: AuthRepository,
    // Recibe el caso de uso que verifica que la cuenta tenga un perfil válido.
    private val checkProfile: CheckProfileUseCase

) {
    // Función asíncrona principal que se ejecuta al invocar el caso de uso.
    suspend operator fun invoke(credentials: Credentials): Result<LoginResult> {

        // Valida si el formato del correo electrónico es correcto.
        if (!AuthValidator.isEmailValid(credentials.email)) {
            // Retorna un fallo si el correo no es válido.
            return Result.failure(AuthException.InvalidEmail)
        }

        // Ejecuta el login en el repositorio con las credenciales.
        val result = authRepository.login(credentials)
            // Obtiene el resultado o retorna el error si ocurre un fallo.
            .getOrElse { return Result.failure(it) }

        // Evalúa el resultado obtenido del inicio de sesión.
        return when (result) {

            // Ejecuta este bloque si se requiere autenticación de dos factores.
            is LoginResult.TwoFactorRequired ->
                // Solicita el código de doble factor enviando el ID de cuenta.
                authRepository.requestTwoFactorCode(result.accountId)
                    // Mapea el resultado para devolverlo.
                    .map { result }

            // Ejecuta este bloque si el inicio de sesión fue exitoso.
            is LoginResult.Success ->
                // Verifica la existencia y estado del perfil del usuario.
                checkProfile(result.account).fold(
                    // Si el perfil es válido, retorna el éxito con el resultado del login.
                    onSuccess = { Result.success(result) },
                    // Si falla, cierra la sesión para evitar dejarla abierta a medias.
                    onFailure = {
                        authRepository.logout()
                        // Retorna el error encontrado como un fallo.
                        Result.failure(it)
                    }
                )
        }
    }
}