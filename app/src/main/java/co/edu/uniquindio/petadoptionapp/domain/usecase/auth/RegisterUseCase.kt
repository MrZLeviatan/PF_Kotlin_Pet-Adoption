package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.auth.Credentials
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.User
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.AuthValidator
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.UserValidator

/**
Caso de uso que crea una nueva cuenta de usuario y su perfil coordinando ambos repositorios.
Es el único Use Case de auth que coordina dos repositorios: AuthRepository crea la identidad y UserRepository guarda el perfil.
Valida los datos sin red, comprueba duplicados (alias y teléfono), crea la cuenta en Firebase Auth y guarda el perfil en Firestore;
si el perfil no se puede guardar, borra la cuenta recién creada. Lo usa el ViewModel de la pantalla de registro.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class RegisterUseCase(
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository,
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository
) {
    // Define la función asíncrona principal que recibe credenciales, nombre de usuario, nombre, teléfono y ubicación, devolviendo un resultado con la cuenta.
    suspend operator fun invoke(
        // Correo y contraseña escritos por la persona.
        credentials: Credentials,
        // Alias público deseado.
        userName: String,
        // Nombre real.
        name: String,
        // Teléfono de contacto.
        phone: String,
        // Ubicación elegida en el mapa.
        location: Location
    ): Result<Account> {


        // Normaliza una sola vez; este valor se usa en todo el flujo.
        val email = credentials.email.trim().lowercase()
        // Quita espacios al alias.
        val cleanUserName = userName.trim()
        // Quita espacios al nombre.
        val cleanName = name.trim()
        // Quita espacios al teléfono.
        val cleanPhone = phone.trim()
        // Copia las credenciales con el correo ya normalizado.
        val cleanCredentials = credentials.copy(email = email)

        // 1. Valida el formato de todos los campos (sin internet).

        // Falla si el correo no tiene formato válido.
        if (!AuthValidator.isEmailValid(email)) return Result.failure(AuthException.InvalidEmail)
        // Falla si la contraseña es débil.
        if (!AuthValidator.isPasswordValid(credentials.password)) return Result.failure(AuthException.WeakPassword)
        // Falla si el nombre es inválido.
        if (!UserValidator.isNameValid(cleanName)) return Result.failure(UserException.InvalidName)
        // Falla si el alias es inválido.
        if (!UserValidator.isUserNameValid(cleanUserName)) return Result.failure(UserException.InvalidUserName)
        // Falla si el teléfono es inválido.
        if (!UserValidator.isPhoneValid(cleanPhone)) return Result.failure(UserException.InvalidPhone)

        // 2. La ubicación debe tener coordenadas reales.
        if (!location.hasCoordinates())
            // Falla con InvalidLocation si no las tiene.
            return Result.failure(UserException.InvalidLocation)

        // 3. Duplicados que Firebase Auth no conoce.

        // Falla si el alias ya lo usa otra persona.
        if (userRepository.existsByUserName(cleanUserName)) return Result.failure(UserException.UserNameTaken)
        // Falla si el teléfono ya lo usa otra cuenta.
        if (userRepository.existsByPhone(cleanPhone)) return Result.failure(UserException.PhoneTaken)


        // 4. Firebase crea la cuenta (rechaza si el email ya existe).

        // Registra las credenciales en el repositorio de autenticación para obtener el identificador único.
        val uid = authRepository.register(cleanCredentials)
            // Obtiene el identificador único o retorna el error si falla el registro.
            .getOrElse { return Result.failure(it) }


        // 5. Identidad y perfil con ese uid, incluida la ubicación.

        // Crea el objeto de cuenta con los datos básicos y el rol de usuario estándar.
        val account = Account(
            // Id igual al uid de Firebase Auth.
            id = uid,
            // Correo normalizado.
            email = email,
            // Alias limpio.
            userName = cleanUserName,
            // Rol de usuario estándar.
            role = UserRole.USER
        )
        // Crea el objeto de perfil de usuario asociando la cuenta, el nombre y el teléfono.
        val user = User(account = account, name = cleanName, phone = cleanPhone, location = location)

        // 6. Guardar el perfil; si falla, cerrar sesión para no dejarla a medias.

        // Guarda el perfil en Firestore y decide según el resultado.
        return userRepository.save(user).fold(
            // La sesión queda con el alias (si no, saldría vacío en comentarios, chats y notificaciones).
            onSuccess = {
                // Actualiza la cuenta de la sesión con el alias y el rol.
                authRepository.updateSessionAccount(account)
                // Devuelve la cuenta creada.
                Result.success(account)
            },
            // Ejecuta este bloque si falla el almacenamiento del perfil.
            onFailure = { error ->
                // Elimina la cuenta recién creada; si eso falla, al menos cierra la sesión.
                authRepository.deleteAccount().onFailure { authRepository.logout() }
                // Retorna el error obtenido como un fallo.
                Result.failure(error)
            }
        )
    }
}
