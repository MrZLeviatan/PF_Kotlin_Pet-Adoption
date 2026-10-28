package co.edu.uniquindio.petadoptionapp.domain.model.messaging

/**
Conversación entre dos personas, normalmente originada por una
publicación (por ejemplo, alguien interesado en adoptar una mascota).

Solo guarda un resumen del último mensaje para poder dibujar la lista de
chats sin descargar los mensajes. Los mensajes completos viven en la
subcolección `messages`.
 */
data class Chat(

    // Identificador del chat. Conviene generarlo con [buildId] para que dos personas no abran conversaciones duplicadas por la misma publicación.
    val id: String = "",
    // Ids de las cuentas participantes del chat.
    val participantIds: List<String> = emptyList(),
    // Publicación que originó el chat; nulo si no hay.
    val postId: String? = null,
    // Texto del último mensaje, para la vista previa; vacío si fue automático.
    val lastMessage: String = "",
    // Tipo del último mensaje, para mostrar el texto traducido si fue automático.
    val lastMessageType: MessageType = MessageType.TEXT,
    // Momento del último mensaje, en milisegundos (epoch), sirve para ordenar la lista.
    val lastMessageAt: Long = 0L,
    // Id de quien envió el último mensaje.
    val lastSenderId: String = "",
    // Mensajes sin leer por participante. La clave es el id de la cuenta; el valor, la cantidad pendiente.
    val unreadCounts: Map<String, Int> = emptyMap(),
    // Último momento en que cada participante abrió el chat (clave: id de la cuenta), en milisegundos (epoch).
    val lastReadAt: Map<String, Long> = emptyMap(),
    // Momento en que cada participante eliminó el chat solo para sí (clave: id de la cuenta), en milisegundos (epoch).
    // Como en WhatsApp: deja de ver los mensajes anteriores y el chat vuelve a aparecer si llega uno nuevo.
    val deletedAt: Map<String, Long> = emptyMap(),
    // Momento en que se creó el chat, en milisegundos (epoch).
    val createdAt: Long = System.currentTimeMillis()
) {

    // Dado mi id, devuelve el id de la otra persona del chat (o `null`).
    fun otherParticipantId(myId: String): String? =
        participantIds.firstOrNull { it != myId }

    // Desde cuándo ve mensajes la persona indicada (0 si nunca eliminó el chat para sí).
    fun visibleSince(userId: String): Long = deletedAt[userId] ?: 0L

    // ¿La persona eliminó el chat para sí y no ha llegado nada después? Entonces no sale en su lista.
    fun isHiddenFor(userId: String): Boolean = deletedAt[userId]?.let { it >= lastMessageAt } ?: false

    // Cantidad de mensajes sin leer que tiene la persona indicada.
    fun unreadFor(userId: String): Int = unreadCounts[userId] ?: 0

    // ¿La persona indicada ya vio este mensaje? Sirve para mostrar "visto".
    fun isMessageSeenBy(userId: String, message: Message): Boolean =
        message.sentAt <= (lastReadAt[userId] ?: 0L)

    companion object {
        /**
        Genera un id determinista a partir de los dos participantes y la
        publicación. Los ids se ordenan para que A+B y B+A produzcan el
        mismo resultado, evitando chats duplicados.
         */
        fun buildId(userA: String, userB: String, postId: String? = null): String {
            val (first, second) = listOf(userA, userB).sorted()
            return "${first}_${second}_${postId ?: "direct"}"
        }
    }
}