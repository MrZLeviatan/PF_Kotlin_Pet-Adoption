package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Verifica que una cuenta autenticada tenga un perfil válido en la base de datos.

- Qué recibe ([account]): La cuenta que acaba de iniciar sesión.
- Qué devuelve: Éxito si el perfil existe (y el moderador está activo), o el error correspondiente.
 */
class CheckProfileUseCase(
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository
) {
    // Define la función asíncrona principal que recibe la cuenta y devuelve un resultado con unidad.
    suspend operator fun invoke(account: Account): Result<Unit> =
        // Evalúa el rol asignado a la cuenta.
        when (account.role) {

            // Ejecuta este bloque si el rol es de moderador.
            UserRole.MODERATOR -> {
                // Busca al moderador en el repositorio usando su ID.
                val moderator = userRepository.findModeratorById(account.id)
                // Evalúa las condiciones de validez del moderador.
                when {
                    // Retorna un fallo si no se encuentra el perfil del moderador.
                    moderator == null -> Result.failure(UserException.ProfileNotFound)
                    // Retorna un fallo si el moderador no tiene permisos activos para moderar.
                    !moderator.canModerate() -> Result.failure(AuthException.InactiveModerator)
                    // Retorna éxito si el moderador cumple con todas las condiciones.
                    else -> Result.success(Unit)
                }
            }

            // Ejecuta este bloque si el rol es de usuario estándar.
            UserRole.USER -> {
                // Busca el perfil del usuario por ID.
                val user = userRepository.findById(account.id)
                // Retorna un fallo si no existe o si la cuenta ya fue eliminada (anonimizada).
                if (user == null || user.deleted) Result.failure(UserException.ProfileNotFound)
                // Retorna éxito si el perfil existe y está activo.
                else Result.success(Unit)
            }
        }
}