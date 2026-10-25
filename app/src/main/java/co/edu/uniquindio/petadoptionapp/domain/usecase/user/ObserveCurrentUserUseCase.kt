package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.model.users.User
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.StateFlow

/**
Expone el perfil de quien está dentro de la app.
Lo usan Inicio, Perfil y Dashboard sin consultar Firestore cada vez.
 */
class ObserveCurrentUserUseCase(
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository
) {
    // Define la función operadora que permite invocar la clase directamente para retornar el flujo del usuario actual.
    operator fun invoke(): StateFlow<User?> = userRepository.currentUser
}