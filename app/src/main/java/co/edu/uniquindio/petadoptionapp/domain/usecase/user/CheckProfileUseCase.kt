package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Documenta la función que evalúa el perfil y retorna un error si no es válido o nulo si todo está bien.
Define una función de extensión interna y asíncrona sobre UserRepository para validar perfiles.
*/
internal suspend fun UserRepository.checkProfile(account: Account): AuthException? =
    // Evalúa el rol asignado a la cuenta.
    when (account.role) {
        // Ejecuta este bloque si el rol es de moderador.
        UserRole.MODERATOR -> {
            // Busca al moderador en la base de datos usando su ID.
            val moderator = findModeratorById(account.id)
            // Evalúa las condiciones de validez del moderador.
            when {
                // Retorna un error si no se encuentra el perfil del moderador.
                moderator == null -> AuthException.ProfileNotFound
                // Retorna un error si el moderador no tiene permisos activos para moderar.
                !moderator.canModerate() -> AuthException.InactiveModerator
                // Retorna nulo si el moderador cumple con todas las condiciones.
                else -> null
            }
        }
        // Ejecuta este bloque si el rol es de usuario estándar.
        UserRole.USER ->
            // Busca el usuario por ID y retorna ProfileNotFound si no existe, o nulo si existe.
            if (findById(account.id) == null) AuthException.ProfileNotFound else null
    }