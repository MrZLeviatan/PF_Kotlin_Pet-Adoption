package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Caso de uso que confirma que quien pide la acción es un moderador activo.
Lee la sesión actual y consulta su perfil de moderador; nunca confía en un Account recibido por parámetro.
Caso de uso auxiliar: lo reutilizan los casos de uso de moderación, catálogos y borrado de comentarios.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
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
            // Si no hay sesión, falla con NotAuthenticated.
            ?: return Result.failure(AuthException.NotAuthenticated)

        // La sesión debe ser de rol moderador.
        if (session.role != UserRole.MODERATOR) return Result.failure(AuthException.NotModerator)

        // Su perfil debe existir y estar activo.
        val moderator = userRepository.findModeratorById(session.id)
        // Falla si no existe o está inactivo.
        if (moderator == null || !moderator.canModerate()) return Result.failure(AuthException.NotModerator)

        // Devuelve la cuenta del moderador validado.
        return Result.success(session)
    }
}
