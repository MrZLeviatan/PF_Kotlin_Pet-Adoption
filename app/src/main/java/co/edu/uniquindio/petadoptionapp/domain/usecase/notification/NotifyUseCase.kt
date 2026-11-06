package co.edu.uniquindio.petadoptionapp.domain.usecase.notification

import co.edu.uniquindio.petadoptionapp.domain.model.notification.Notification
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.repository.NotificationRepository

/**
Caso de uso interno que crea una notificación para otra persona.
Lo llaman los demás casos de uso (comentar, votar, moderar) después de guardar su acción principal.
Es de "mejor esfuerzo": si falla, la acción principal no se deshace, porque sin Cloud Functions no hay transacción entre ambas.
Guarda la notificación en Firestore mediante NotificationRepository.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class NotifyUseCase(
    // Inyecta el repositorio de notificaciones.
    private val notificationRepository: NotificationRepository
) {
    // Define la función asíncrona que recibe los datos del evento y devuelve un resultado vacío.
    suspend operator fun invoke(
        // Persona que recibe la notificación.
        recipientId: String,
        // Evento que la origina.
        type: NotificationType,
        // Quien provocó el evento (null si es el sistema).
        actorId: String? = null,
        // Alias de quien provocó el evento.
        actorName: String? = null,
        // Publicación relacionada, si aplica.
        postId: String? = null,
        // Comentario relacionado, si aplica.
        commentId: String? = null,
        // Dato adicional según el tipo (motivo, insignia, nivel...).
        extra: String? = null,
        // Chat relacionado, si aplica.
        chatId: String? = null
    ): Result<Unit> {

        // Nadie se notifica a sí mismo (si el actor es el destinatario, no se hace nada).
        if (actorId != null && actorId == recipientId) return Result.success(Unit)

        // Sin destinatario no hay a quién avisar.
        if (recipientId.isBlank()) return Result.success(Unit)

        // Crea la notificación sin leer, con el momento actual.
        return notificationRepository.create(
            // Construye el modelo con los datos del evento (el id lo asigna Firestore).
            Notification(
                // Destinatario.
                recipientId = recipientId,
                // Tipo de evento.
                type = type,
                // Actor del evento.
                actorId = actorId,
                // Alias del actor.
                actorName = actorName,
                // Publicación relacionada.
                postId = postId,
                // Comentario relacionado.
                commentId = commentId,
                // Dato adicional.
                extra = extra,
                // Chat relacionado.
                chatId = chatId,
                // Nace sin leer.
                read = false,
                // Momento actual.
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
