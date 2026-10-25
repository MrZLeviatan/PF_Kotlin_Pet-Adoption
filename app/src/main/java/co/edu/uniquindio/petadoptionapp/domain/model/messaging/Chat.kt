package co.edu.uniquindio.petadoptionapp.domain.model.messaging

/**
 Conversación entre dos personas, normalmente originada por una
 publicación (por ejemplo, alguien interesado en adoptar una mascota).

 Solo guarda un resumen del último mensaje para poder dibujar la lista de
 chats sin descargar los mensajes. Los mensajes completos viven en la
 sub-colección `messages`.
 */
data class Chat(

    // Identificador del chat. Conviene generarlo con [buildId] para que dos personas no abran conversaciones duplicadas por la misma publicación.
    val id: String = "",
    // Ids de las cuentas participantes del chat.
    val participantIds: List<String> = emptyList(),
    // Publicación que originó el chat; nulo si no hay.
    val postId: String? = null,
    // Texto del último mensaje, para la vista previa.
    val lastMessage: String = "",
    // Momento del último mensaje, en milisegundos (epoch), sirve para ordenar la lista.
    val lastMessageAt: Long = System.currentTimeMillis(),
    // Id de quien envió el último mensaje.
    val lastSenderId: String = "",
    // Mensajes sin leer por participantes. La clave es el ID de la cuenta; el valor, la cantidad pendiente.
    val unreadCounts: Map<String, Int> = emptyMap(),
    // Momento en que se creó el chat, en milisegundos (epoch).
    val createdAt: Long = System.currentTimeMillis()
) {

    // Dado mi id, devuelve el id de la otra persona del chat (o `null`).
    fun otherParticipantId(myId: String): String? =
        participantIds.firstOrNull { it != myId }

    // Cantidad de mensajes sin leer que tiene la persona indicada.
    fun unreadFor(userId: String): Int = unreadCounts[userId] ?: 0

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