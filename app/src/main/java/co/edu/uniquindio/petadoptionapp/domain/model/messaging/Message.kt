package co.edu.uniquindio.petadoptionapp.domain.model.messaging

/**
 Mensaje individual dentro de un [Chat].

 Se guarda en la subcolección `chats/{chatId}/messages`, por eso no
 necesita un campo `chatId`: la ruta del documento ya lo indica.
 */
data class Message(
    val id: String = "",
    // Id de la cuenta que lo envío.
    val senderId: String = "",
    // Tipo de mensaje.
    val type: MessageType = MessageType.TEXT,
    // Contenido del mensaje.
    val text: String = "",
    // Momento del envío, en milisegundos (epoch). Se usa para ordenar.
    val sentAt: Long = System.currentTimeMillis(),
    // `true` cuando el destinatario ya lo vio. No se usa el prefijo `is` porque Firestore elimina eso.
    val read: Boolean = false
)