package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository

/**
Caso de uso que cierra la sesión actual.
Al quedar sin cuenta, el perfil de la sesión ([UserRepository.currentUser]) pasa a `null`
solo; la navegación se encarga de volver al login y limpiar las pantallas abiertas.
 */
class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.logout()
}
