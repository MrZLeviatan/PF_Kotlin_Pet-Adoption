package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.model.auth.Credentials
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.User
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Documenta el caso de uso que crea una nueva cuenta de usuario y su perfil coordinando ambos repositorios.
Es el único Use Case de auth que coordina dos repositorios: AuthRepository crea la identidad y UserRepository guarda el perfil.
 */
class RegisterUseCase(
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository,
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository
) {
    // Define la función asíncrona principal que recibe credenciales, nombre de usuario, nombre, teléfono y ubicación, devolviendo un resultado con la cuenta.
    suspend operator fun invoke(
        credentials: Credentials,
        userName: String,
        name: String,
        phone: String,
        location: Location
    ): Result<Account> {

        // 1. Válida el formato de todos los campos (Sin internet)

        if (!AuthValidator.isEmailValid(credentials.email))
            return Result.failure(AuthException.InvalidEmail)
        if (!AuthValidator.isPasswordValid(credentials.password))
            return Result.failure(AuthException.WeakPassword)
        if (!AuthValidator.isUserNameValid(userName))
            return Result.failure(AuthException.InvalidUserName)
        if (!AuthValidator.isPhoneValid(phone))
            return Result.failure(AuthException.InvalidPhone)

        // 2. La ubicación debe tener coordenadas reales
        if (!location.hasCoordinates())
            return Result.failure(AuthException.InvalidLocation)

        // 3. Duplicados que Firebase Auth no conoce.

        if (userRepository.existsByUserName(userName.trim()))
            return Result.failure(AuthException.UserNameTaken)
        if (userRepository.existsByPhone(phone.trim()))
            return Result.failure(AuthException.PhoneTaken)

        // 4. Firebase crea la cuenta (rechaza si el email ya existe).

        // Registra las credenciales en el repositorio de autenticación para obtener el identificador único.
        val uid = authRepository.register(credentials)
            // Obtiene el identificador único o retorna el error si falla el registro.
            .getOrElse { return Result.failure(it) }


        // 5. Identidad y perfil con ese uid, incluida la ubicación.

        // Crea el objeto de cuenta con los datos básicos y el rol de usuario estándar.
        val account = Account(
            id = uid,
            email = credentials.email.trim(),
            userName = userName.trim(),
            role = UserRole.USER
        )
        // Crea el objeto de perfil de usuario asociando la cuenta, el nombre y el teléfono.
        val user = User(account = account, name = name.trim(), phone = phone.trim(), location = location)

        // 6. Guardar el perfil; si falla, cerrar sesión para no dejarla a medias.

        return userRepository.save(user).fold(
            // Retorna el éxito con la cuenta creada si se guarda correctamente.
            onSuccess = { Result.success(account) },
            // Ejecuta este bloque si falla el almacenamiento del perfil.
            onFailure = {
                // Cierra la sesión activa para evitar dejarla a medias.
                authRepository.logout()
                // Retorna el error obtenido como un fallo.
                Result.failure(it)
            }
        )
    }
}