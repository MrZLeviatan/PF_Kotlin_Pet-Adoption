package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.AccountDeletionFeedback
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Documenta el caso de uso que elimina la cuenta completa. Coordina tres repositorios y el orden importa:
1. Confirmar que la cuenta a borrar es la de la sesión actual y que la contraseña es correcta.
2. Cerrar las publicaciones de la cuenta.
3. Anonimizar el perfil de Firestore (aún hay sesión, así que hay permiso).
4. Borrar la cuenta de Auth (esto cierra la sesión).
Cada paso se puede repetir sin efectos dobles, así un fallo a medias se resuelve reintentando.
 */
class DeleteAccountUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    // Inyecta el repositorio de publicaciones para cerrarlas.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que quien pide es el titular.
    private val requireOwnUser: RequireOwnUserUseCase
) {
    suspend operator fun invoke(
        accountId: String,
        password: String,
        feedback: AccountDeletionFeedback? = null
    ): Result<Unit> {

        // Paso 1: solo se puede eliminar la cuenta de la sesión actual.
        requireOwnUser(accountId).getOrElse { return Result.failure(it) }

        // Verifica la identidad con la contraseña.
        authRepository.reauthenticate(password).getOrElse { return Result.failure(it) }

        // Paso 2: deja de mostrar las publicaciones de la cuenta.
        postRepository.deleteAllByAuthor(accountId).getOrElse { return Result.failure(it) }

        // Paso 3: anonimiza el perfil.
        userRepository.anonymize(accountId, feedback).getOrElse { return Result.failure(it) }

        // Paso 4: borra la cuenta de login; si falla, reintenta una vez.
        var result = authRepository.deleteAccount()
        if (result.isFailure) result = authRepository.deleteAccount()

        // Si sigue fallando, cierra la sesión y avisa que quedó pendiente.
        if (result.isFailure) {
            authRepository.logout()
            return Result.failure(UserException.DeletionIncomplete)
        }
        return Result.success(Unit)
    }
}