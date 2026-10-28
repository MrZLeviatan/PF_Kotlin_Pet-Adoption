package co.edu.uniquindio.petadoptionapp.domain.usecase.notification

import co.edu.uniquindio.petadoptionapp.domain.model.notification.Notification
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.repository.NotificationRepository

/**
Documenta el caso de uso interno que crea una notificación para otra persona.
Lo llaman los demás casos de uso (comentar, votar, moderar) después de guardar su acción principal.
Es de "mejor esfuerzo": si falla, la acción principal no se deshace, porque sin Cloud Functions no hay transacción entre ambas.
 */
class NotifyUseCase(
    // Inyecta el repositorio de notificaciones.
    private val notificationRepository: NotificationRepository
) {
    // Define la función asíncrona que recibe los datos del evento y devuelve un resultado vacío.
    suspend operator fun invoke(
        recipientId: String,
        type: NotificationType,
        actorId: String? = null,
        actorName: String? = null,
        postId: String? = null,
        commentId: String? = null,
        extra: String? = null,
        chatId: String? = null
    ): Result<Unit> {

        // Nadie se notifica a sí mismo (si el actor es el destinatario, no se hace nada).
        if (actorId != null && actorId == recipientId) return Result.success(Unit)

        // Sin destinatario no hay a quién avisar.
        if (recipientId.isBlank()) return Result.success(Unit)

        // Crea la notificación sin leer, con el momento actual.
        return notificationRepository.create(
            Notification(
                recipientId = recipientId,
                type = type,
                actorId = actorId,
                actorName = actorName,
                postId = postId,
                commentId = commentId,
                extra = extra,
                chatId = chatId,
                read = false,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}