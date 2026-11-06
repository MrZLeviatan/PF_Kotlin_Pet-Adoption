package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository

/**
Caso de uso que confirma que hay una sesión abierta de un usuario (no moderador).
Devuelve la cuenta de la sesión actual; nunca confía en un Account recibido por parámetro.
Caso de uso auxiliar: lo reutilizan casi todos los casos de uso de usuario (chats, comentarios, votos, reputación...).
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class RequireUserUseCase(
    // Inyecta el repositorio de autenticación para leer la sesión actual.
    private val authRepository: AuthRepository
) {
    // Define la función que devuelve la cuenta de la sesión o el error correspondiente.
    operator fun invoke(): Result<Account> {

        // Sin sesión abierta no se puede hacer nada.
        val session = authRepository.currentAccount.value
            // Si no hay sesión, falla con NotAuthenticated.
            ?: return Result.failure(AuthException.NotAuthenticated)

        // Solo las cuentas de usuario participan en la comunidad.
        if (session.role != UserRole.USER) return Result.failure(UserException.NotAllowed)

        // Devuelve la cuenta de la sesión validada.
        return Result.success(session)
    }
}
