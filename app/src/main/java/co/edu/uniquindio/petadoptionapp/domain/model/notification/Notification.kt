package co.edu.uniquindio.petadoptionapp.domain.model.notification


/**
 Notificación interna de la aplicación, dirigida a una persona.

 Se guarda en la colección `notifications` y se consulta al abrir la app
 o al entrar al apartado de notificaciones (no usa Firebase Cloud
 Messaging). No almacena el mensaje ya redactado: guarda el [type] y los
 datos necesarios, y la interfaz compone el texto con `strings.xml`.

 Para navegar, la UI usa [postId] (y [commentId] si existe) y abre el
 detalle de la publicación relacionada.
 */
data class Notification(
    val id: String = "",
    // Id de la cuenta que recibe la notificación.
    val recipientId: String = "",
    // Tipo de notificación.
    val type: NotificationType = NotificationType.NEW_COMMENT,
    // Id de quien la generó; nulo si es el sistema.
    val actorId: String? = null,
    // Alias de quien la generó; nulo si es el sistema.
    val actorName: String? = null,
    // Id de la publicación relacionada, si aplica.
    val postId: String? = null,
    // Id del comentario relacionado, si aplica.
    val commentId: String? = null,
    // Dato adicional según el tipo: motivo del rechazo, id de la insignia o nombre del nivel.
    val extra: String? = null,
    // `true` si la persona ya la abrió.
    val read: Boolean = false,
    // Momento del evento, en milisegundos (epoch).
    val createdAt: Long = System.currentTimeMillis()
) {

    // ¿Se puede navegar a una publicación desde esta notificación?
    fun hasDestination(): Boolean = !postId.isNullOrBlank()

    // Devuelve una copia marcada como leída.
    fun markAsRead(): Notification = copy(read = true)

    //  Devuelve una copia marcada como no leída.
    fun markAsUnread(): Notification = copy(read = false)
}