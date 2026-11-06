package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.AccountDeletionFeedback
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Caso de uso que elimina la cuenta completa. Coordina tres repositorios y el orden importa:
1. Confirmar que la cuenta a borrar es la de la sesión actual y que la contraseña es correcta.
2. Cerrar las publicaciones de la cuenta.
3. Anonimizar el perfil de Firestore (aún hay sesión, así que hay permiso).
4. Borrar la cuenta de Auth (esto cierra la sesión).
Cada paso se puede repetir sin efectos dobles, así un fallo a medias se resuelve reintentando.
Lo usa el ViewModel de la pantalla de ajustes (opción "Eliminar cuenta").
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class DeleteAccountUseCase(
    // Inyecta el repositorio de autenticación (reautenticar y borrar la identidad).
    private val authRepository: AuthRepository,
    // Inyecta el repositorio de usuarios (anonimizar el perfil).
    private val userRepository: UserRepository,
    // Inyecta el repositorio de publicaciones para cerrarlas.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que quien pide es el titular.
    private val requireOwnUser: RequireOwnUserUseCase
) {
    // Función principal: elimina la cuenta indicada siguiendo los cuatro pasos.
    suspend operator fun invoke(
        // Id de la cuenta a eliminar (debe ser la de la sesión).
        accountId: String,
        // Contraseña actual, para confirmar la identidad.
        password: String,
        // Motivo opcional de la baja, para estadísticas.
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
        // Si el primer intento falló, lo intenta una segunda vez.
        if (result.isFailure) result = authRepository.deleteAccount()

        // Si sigue fallando, cierra la sesión y avisa que quedó pendiente.
        if (result.isFailure) {
            // Cierra la sesión para no dejarla a medias.
            authRepository.logout()
            // Avisa que el borrado quedó incompleto (se resuelve reintentando).
            return Result.failure(UserException.DeletionIncomplete)
        }
        // Todo salió bien.
        return Result.success(Unit)
    }
}
