package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository

/**
Caso de uso que cierra la sesión actual.
Al quedar sin cuenta, el perfil de la sesión ([UserRepository.currentUser]) pasa a `null`
solo; la navegación se encarga de volver al login y limpiar las pantallas abiertas.
Lo usan los ViewModels de perfil y del panel de moderación (botón "Cerrar sesión").
**/
// Declara el caso de uso de cierre de sesión.
class LogoutUseCase(
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository
) {
    // Función principal: delega el cierre de sesión en Firebase Auth a través del repositorio.
    suspend operator fun invoke() = authRepository.logout()
}
