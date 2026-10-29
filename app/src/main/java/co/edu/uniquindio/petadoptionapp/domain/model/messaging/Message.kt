package co.edu.uniquindio.petadoptionapp.domain.model.messaging

/**
Mensaje individual dentro de un [Chat].

Se guarda en la subcolección `chats/{chatId}/messages`, por eso no
necesita un campo `chatId`: la ruta del documento ya lo indica.
Si el mensaje fue leído se deduce de [Chat.lastReadAt], no se guarda aquí.
 */
data class Message(
    // Identificador del mensaje; lo genera Firestore al guardar.
    val id: String = "",
    // Id de la cuenta que lo envió.
    val senderId: String = "",
    // Tipo de mensaje.
    val type: MessageType = MessageType.TEXT,
    // Contenido del mensaje; vacío en mensajes automáticos (la UI pone el texto desde strings.xml).
    // En una foto es el pie de foto (puede ir vacío).
    val text: String = "",
    // Dirección de la foto si el mensaje es de tipo IMAGE; null en los demás.
    val imageUrl: String? = null,
    // Momento del envío, en milisegundos (epoch). Se usa para ordenar.
    val sentAt: Long = System.currentTimeMillis()
)