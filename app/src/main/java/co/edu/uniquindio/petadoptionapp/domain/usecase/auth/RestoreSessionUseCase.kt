package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.CheckProfileUseCase

/**
Caso de uso que, al abrir la app, recupera la sesión guardada en el teléfono por Firebase Auth.
Comprueba además que la cuenta siga teniendo un perfil válido; si no, cierra la sesión.
La pantalla de carga lo usa para ir directo al inicio (con sesión) o al login (sin sesión).
**/
// Declara el caso de uso de restauración de sesión.
class RestoreSessionUseCase(
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository,
    // Inyecta el caso de uso que verifica que la cuenta tenga un perfil válido.
    private val checkProfile: CheckProfileUseCase
) {
    // Devuelve la cuenta si la sesión sigue siendo válida; si no (perfil borrado, moderador inactivo), la cierra.
    suspend operator fun invoke(): Account? {
        // Recupera la cuenta guardada; si no hay sesión completa, devuelve null.
        val account = authRepository.restoreSession() ?: return null
        // Verifica que el perfil de la cuenta exista y esté en buen estado.
        return checkProfile(account).fold(
            // Si el perfil es válido, devuelve la cuenta.
            onSuccess = { account },
            // Si no, cierra la sesión y devuelve null.
            onFailure = {
                // Cierra la sesión para no dejar una cuenta inválida abierta.
                authRepository.logout()
                // Indica que no hay sesión utilizable.
                null
            }
        )
    }
}
