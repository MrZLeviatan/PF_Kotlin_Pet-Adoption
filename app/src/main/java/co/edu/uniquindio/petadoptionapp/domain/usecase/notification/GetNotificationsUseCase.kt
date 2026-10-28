package co.edu.uniquindio.petadoptionapp.domain.usecase.notification

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.model.notification.Notification
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.NotificationRepository

/**
Documenta el caso de uso que entrega una página de las notificaciones de la persona con sesión.
 */
class GetNotificationsUseCase(
    // Inyecta el repositorio de notificaciones.
    private val notificationRepository: NotificationRepository,
    // Inyecta el repositorio de autenticación para leer la sesión actual.
    private val authRepository: AuthRepository
) {
    // Define un objeto de acompañamiento que establece la cantidad máxima de notificaciones por página en 20.
    companion object { const val PAGE_SIZE = 20 }

    // Define la función asíncrona principal que recibe un identificador opcional de notificación para la paginación y devuelve un resultado con la lista de notificaciones.
    suspend operator fun invoke(afterNotificationId: String? = null): Result<List<Notification>> {

        // Las notificaciones son de quien tiene la sesión; nunca se recibe el id por parámetro.
        // Obtiene la cuenta actual de la sesión y retorna un fallo si el usuario no está autenticado.
        val session = authRepository.currentAccount.value
            ?: return Result.failure(AuthException.NotAuthenticated)

        // Solicita al repositorio las notificaciones destinadas al usuario utilizando su ID, el tamaño de página y el paginador, retornando el resultado.
        return notificationRepository.getByRecipient(session.id, PAGE_SIZE, afterNotificationId)
    }
}