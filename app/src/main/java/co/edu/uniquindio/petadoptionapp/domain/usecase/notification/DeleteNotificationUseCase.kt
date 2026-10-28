package co.edu.uniquindio.petadoptionapp.domain.usecase.notification

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.error.NotificationException
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.NotificationRepository

/**
Documenta el caso de uso que borra una notificación propia de la bandeja.
 */
class DeleteNotificationUseCase(
    // Inyecta el repositorio de notificaciones.
    private val notificationRepository: NotificationRepository,
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository
) {
    // Define la función asíncrona principal que recibe el ID de la notificación y devuelve un resultado vacío.
    suspend operator fun invoke(notificationId: String): Result<Unit> {

        // Obtiene la cuenta actual de la sesión y retorna un fallo si el usuario no está autenticado.
        val session = authRepository.currentAccount.value
            ?: return Result.failure(AuthException.NotAuthenticated)

        // Debe existir y ser de quien la pide.
        // Busca la notificación por su ID y asegura que pertenezca al usuario en sesión actual, retornando un fallo si no se encuentra o no le pertenece.
        notificationRepository.findById(notificationId)
            ?.takeIf { it.recipientId == session.id }
            ?: return Result.failure(NotificationException.NotificationNotFound)

        // Elimina la notificación del repositorio utilizando su ID y retorna el resultado.
        return notificationRepository.delete(notificationId)
    }
}