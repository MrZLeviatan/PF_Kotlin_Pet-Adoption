package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository

/**
Documenta el caso de uso que confirma que quien pide la acción es el titular del perfil.
Compara el id recibido con la cuenta de la sesión actual, nunca confía solo en el parámetro.
Los moderadores no editan ni eliminan su perfil desde la app, por eso solo acepta rol USER.
 */
class RequireOwnUserUseCase(
    // Inyecta el repositorio de autenticación para leer la sesión actual.
    private val authRepository: AuthRepository
) {
    // Define la función que recibe el id del perfil a tocar y devuelve la cuenta de la sesión.
    operator fun invoke(ownerId: String): Result<Account> {

        // Sin sesión abierta no se puede hacer nada.
        val session = authRepository.currentAccount.value
            ?: return Result.failure(AuthException.NotAuthenticated)

        // El id recibido debe ser el de la cuenta autenticada.
        if (session.id != ownerId) return Result.failure(UserException.NotAllowed)

        // Solo las cuentas de usuario tienen perfil editable.
        if (session.role != UserRole.USER) return Result.failure(UserException.NotAllowed)

        return Result.success(session)
    }
}