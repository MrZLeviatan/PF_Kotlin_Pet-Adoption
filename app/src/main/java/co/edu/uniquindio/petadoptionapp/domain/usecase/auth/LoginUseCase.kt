package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.model.auth.Credentials
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.LoginResult
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.checkProfile

// Define la clase del caso de uso para gestionar el inicio de sesión.
class LoginUseCase(

    // Recibe la dependencia del repository de autenticación.
    private val authRepository: AuthRepository,
    // Recibe la dependencia del repositorio de usuarios.
    private val userRepository: UserRepository

) {
    // Función asíncrona principal que se ejecuta al invocar el caso de uso.
    suspend operator fun invoke(credentials: Credentials): Result<LoginResult> {

        // Válida si el formato del correo electrónico es correcto.
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
            is LoginResult.Success -> {
                // Verifica la existencia y estado del perfil del usuario.
                val error = userRepository.checkProfile(result.account)
                // Comprueba si el perfil no presenta errores.
                if (error == null) {
                    // Retorna el éxito con el resultado del login.
                    Result.success(result)
                } else {
                    // Cierra la sesión para evitar dejarla abierta a medias.
                    authRepository.logout()
                    // Retorna el error encontrado como un fallo.
                    Result.failure(error)
                }
            }
        }
    }
}