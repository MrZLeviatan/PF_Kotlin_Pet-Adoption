package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.CheckProfileUseCase

/**
Documenta el caso de uso que, al abrir la app, recupera la sesión guardada en el teléfono.
La pantalla de carga lo usa para ir directo al inicio (con sesión) o al login (sin sesión).
 */
class RestoreSessionUseCase(
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository,
    // Inyecta el caso de uso que verifica que la cuenta tenga un perfil válido.
    private val checkProfile: CheckProfileUseCase
) {
    // Devuelve la cuenta si la sesión sigue siendo válida; si no (perfil borrado, moderador inactivo), la cierra.
    suspend operator fun invoke(): Account? {
        val account = authRepository.restoreSession() ?: return null
        return checkProfile(account).fold(
            onSuccess = { account },
            onFailure = {
                authRepository.logout()
                null
            }
        )
    }
}
