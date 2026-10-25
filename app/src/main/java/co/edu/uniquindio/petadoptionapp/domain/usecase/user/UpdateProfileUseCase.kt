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
    private val userRepository: UserRepository
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

        // Valida si el formato del nombre es correcto y retorna un fallo si no lo es.
        if (!UserValidator.isNameValid(name)) return Result.failure(UserException.InvalidName)
        // Valida si el formato del teléfono es correcto y retorna un fallo si no lo es.
        if (!UserValidator.isPhoneValid(phone)) return Result.failure(UserException.InvalidPhone)
        // Válida si la ubicación contiene coordenadas válidas y retorna un fallo si no las tiene.
        if (!location.hasCoordinates()) return Result.failure(UserException.InvalidLocation)

        // Verifica si el número de teléfono ya está registrado por otro usuario excluyendo el ID actual.
        if (userRepository.existsByPhone(phone.trim(), excludeId = userId))
        // Retorna un fallo si el teléfono ya se encuentra ocupado.
            return Result.failure(UserException.PhoneTaken)

        // Actualiza los campos específicos del perfil en el repositorio con los valores limpios.
        return userRepository.updateProfile(userId, name.trim(), phone.trim(), photoUrl, location)
    }
}
