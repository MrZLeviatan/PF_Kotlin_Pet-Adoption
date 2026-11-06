package co.edu.uniquindio.petadoptionapp.domain.usecase.notification

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.error.NotificationException
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.NotificationRepository

/**
Caso de uso que borra una notificación propia de la bandeja.
Lo usa el ViewModel de la pantalla de notificaciones.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
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
            // Si no hay sesión, falla con NotAuthenticated.
            ?: return Result.failure(AuthException.NotAuthenticated)

        // Debe existir y ser de quien la pide.
        // Busca la notificación por su ID y asegura que pertenezca al usuario en sesión actual, retornando un fallo si no se encuentra o no le pertenece.
        notificationRepository.findById(notificationId)
            // Solo la conserva si pertenece a quien la pide.
            ?.takeIf { it.recipientId == session.id }
            // Si no existe o es de otra persona, falla con NotificationNotFound.
            ?: return Result.failure(NotificationException.NotificationNotFound)

        // Elimina la notificación del repositorio utilizando su ID y retorna el resultado.
        return notificationRepository.delete(notificationId)
    }
}
