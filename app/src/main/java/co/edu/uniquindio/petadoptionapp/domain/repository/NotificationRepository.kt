package co.edu.uniquindio.petadoptionapp.domain.repository

import co.edu.uniquindio.petadoptionapp.domain.model.notification.Notification

/**
Contrato de notificaciones guardadas en la colección `notifications` de Firestore.
Los permisos y la decisión de cuándo notificar viven en los casos de uso, aquí solo se ejecuta.
 */
interface NotificationRepository {

    // Crear

    /**
    Guarda una notificación nueva.

    - Qué envía ([notification]): La notificación con el `id` vacío y `read = false`.
    - Qué recibe: Un [Result<Unit>] que confirma si se guardó.
     */
    suspend fun create(notification: Notification): Result<Unit>


    // Consultar

    /**
    Obtiene una página de notificaciones de una persona, de la más reciente a la más antigua.

    - Qué envía: El [recipientId], el [limit] y, opcionalmente, el [afterNotificationId] del último ya mostrado.
    - Qué recibe: Un [Result] con la lista de notificaciones.
     */
    suspend fun getByRecipient(
        recipientId: String,
        limit: Int,
        afterNotificationId: String? = null
    ): Result<List<Notification>>

    /**
    Busca una notificación.

    - Qué envía ([id]): El identificador de la notificación.
    - Qué recibe: La [Notification] o `null` si no existe.
     */
    suspend fun findById(id: String): Notification?

    /**
    Cuenta las notificaciones no leídas de una persona.

    - Qué envía ([recipientId]): El identificador del destinatario.
    - Qué recibe: Un [Result] con la cantidad. Un conteo cuesta una fracción de descargar los documentos.
     */
    suspend fun countUnread(recipientId: String): Result<Int>


    // Modificar

    /**
    Cambia el estado leída/no leída de una notificación.

    - Qué envía: El [id] y el nuevo valor de [read].
    - Qué recibe: Un [Result<Unit>] que confirma si se guardó. Solo toca ese campo.
     */
    suspend fun setRead(id: String, read: Boolean): Result<Unit>

    /**
    Marca como leídas todas las no leídas de una persona.

    - Qué envía ([recipientId]): El identificador del destinatario.
    - Qué recibe: Un [Result<Unit>] que confirma si se aplicó. La implementación escribe en lotes (máximo 500 por lote).
     */
    suspend fun markAllAsRead(recipientId: String): Result<Unit>


    // Eliminar

    /**
    Borra una notificación de la bandeja.

    - Qué envía ([id]): El identificador de la notificación.
    - Qué recibe: Un [Result<Unit>] que confirma si se borró.
     */
    suspend fun delete(id: String): Result<Unit>
}