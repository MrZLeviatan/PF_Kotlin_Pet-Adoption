package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.AccountDeletionFeedback
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Elimina la cuenta completa. Coordina dos repositorios y el orden importa:
1. Confirmar la contraseña (si falla, no se borra nada).
2. Borrar el perfil de Firestore (aún hay sesión, así que hay permiso).
3. Borrar la cuenta de Auth (esto cierra la sesión).
 */
class DeleteAccountUseCase(
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository,
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository
) {
    // Define la función asíncrona principal que recibe el ID de cuenta, la contraseña y la retroalimentación opcional, devolviendo un resultado vacío.
    suspend operator fun invoke(accountId: String, password: String, feedback: AccountDeletionFeedback? = null): Result<Unit> {

        // Paso 1: verifica la identidad.
        // Reautentica al usuario con la contraseña y retorna un fallo si el proceso falla.
        authRepository.reauthenticate(password).getOrElse { return Result.failure(it) }

        // Paso 2: anonimiza el perfil (aún hay sesión, así que hay permiso).
        // Anonimiza el perfil de usuario en el repositorio y retorna un fallo si el proceso falla.
        userRepository.anonymize(accountId, feedback).getOrElse { return Result.failure(it) }

        // Paso 3: borra la cuenta de login (esto cierra la sesión).
        // Elimina la cuenta de autenticación y retorna el resultado final.
        return authRepository.deleteAccount()
    }
}