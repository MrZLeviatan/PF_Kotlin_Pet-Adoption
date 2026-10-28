package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.UserValidator

/**
Válida y guarda nombre, teléfono, foto y ubicación del perfil.
El alias no se edita aquí: es parte de la identidad (`Account`).
 */
class UpdateProfileUseCase(
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository,
    // Inyecta el caso de uso que confirma que quien pide es el titular.
    private val requireOwnUser: RequireOwnUserUseCase
) {
    // Define la función asíncrona principal que recibe los datos de actualización y devuelve un resultado vacío.
    suspend operator fun invoke(
        // Recibe el identificador único del usuario.
        userId: String,
        // Recibe el nombre del usuario.
        name: String,
        // Recibe el número de teléfono del usuario.
        phone: String,
        // Recibe la URL opcional de la foto de perfil.
        photoUrl: String?,
        // Recibe la ubicación geográfica del usuario.
        location: Location
    ): Result<Unit> {

        // 1. Solo el titular de la sesión puede editar este perfil.
        requireOwnUser(userId).getOrElse { return Result.failure(it) }

        // 2. Formato de los campos, sin internet.
        val cleanName = name.trim()
        val cleanPhone = phone.trim()
        // Valida si el formato del nombre es correcto y retorna un fallo si no lo es.
        if (!UserValidator.isNameValid(cleanName)) return Result.failure(UserException.InvalidName)
        // Valida si el formato del teléfono es correcto y retorna un fallo si no lo es.
        if (!UserValidator.isPhoneValid(cleanPhone)) return Result.failure(UserException.InvalidPhone)
        // Válida si la ubicación contiene coordenadas válidas y retorna un fallo si no las tiene.
        if (!location.hasCoordinates()) return Result.failure(UserException.InvalidLocation)

        // 3. El perfil debe existir y estar activo.
        val user = userRepository.findById(userId)
        if (user == null || user.deleted) return Result.failure(UserException.ProfileNotFound)

        // 4. El teléfono no puede ser de otra persona (se ignora el propio).
        if (userRepository.existsByPhone(cleanPhone, excludeId = userId))
            return Result.failure(UserException.PhoneTaken)

        // 5. Guarda solo estos campos.
        return userRepository.updateProfile(userId, cleanName, cleanPhone, photoUrl, location)
    }
}
