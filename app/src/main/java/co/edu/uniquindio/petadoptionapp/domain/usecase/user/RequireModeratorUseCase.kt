package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Documenta el caso de uso que confirma que quien pide la acción es un moderador activo.
Lee la sesión actual y consulta su perfil de moderador; nunca confía en un Account recibido por parámetro.
 */
class RequireModeratorUseCase(
    // Inyecta el repositorio de autenticación para leer la sesión actual.
    private val authRepository: AuthRepository,
    // Inyecta el repositorio de usuarios para consultar el perfil del moderador.
    private val userRepository: UserRepository
) {
    // Define la función que devuelve la cuenta del moderador o el error correspondiente.
    suspend operator fun invoke(): Result<Account> {

        // Sin sesión abierta no se puede hacer nada.
        val session = authRepository.currentAccount.value
            ?: return Result.failure(AuthException.NotAuthenticated)

        // La sesión debe ser de rol moderador.
        if (session.role != UserRole.MODERATOR) return Result.failure(AuthException.NotModerator)

        // Su perfil debe existir y estar activo.
        val moderator = userRepository.findModeratorById(session.id)
        if (moderator == null || !moderator.canModerate()) return Result.failure(AuthException.NotModerator)

        return Result.success(session)
    }
}