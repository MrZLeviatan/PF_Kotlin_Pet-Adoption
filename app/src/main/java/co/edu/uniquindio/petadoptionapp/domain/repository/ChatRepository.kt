package co.edu.uniquindio.petadoptionapp.domain.repository


import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequest
import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequestStatus
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Chat
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Message
import kotlinx.coroutines.flow.Flow

/**
Contrato de chats (colección `chats`), mensajes (subcolección `messages`) y solicitudes de adopción (colección `adoptionRequests`).
Los permisos y las reglas de negocio viven en los casos de uso, aquí solo se ejecuta.
 */
interface ChatRepository {

    // Chats

    /**
    Abre un chat entre dos personas; si ya existe, devuelve el existente.

    - Qué envía ([chat]): El chat con su id determinista (`Chat.buildId`).
    - Qué recibe: Un [Result] con el chat guardado o el que ya existía.
    Como el id es fijo, dos aperturas simultáneas nunca crean chats duplicados.
     */
    suspend fun openChat(chat: Chat): Result<Chat>

    /**
    Busca un chat.

    - Qué envía ([id]): El identificador del chat.
    - Qué recibe: El [Chat] o `null` si no existe.
     */
    suspend fun findChatById(id: String): Chat?

    /**
    Obtiene una página de los chats de una persona, del más reciente al más antiguo.

    - Qué envía: El [userId], el [limit] y, opcionalmente, el [afterChatId] del último ya mostrado.
    - Qué recibe: Un [Result] con la lista de chats.
     */
    suspend fun getChatsByParticipant(userId: String, limit: Int, afterChatId: String? = null): Result<List<Chat>>

    /**
    Cuenta los chats que tienen mensajes sin leer para una persona.

    - Qué envía ([userId]): El identificador de la persona.
    - Qué recibe: Un [Result] con la cantidad (para el globito del ícono).
     */
    suspend fun countChatsWithUnread(userId: String): Result<Int>


    /**
    Elimina el chat solo para una persona (los demás lo siguen viendo).
    - Qué envía: El [chatId] y el [userId].
    - Qué recibe: Un [Result<Unit>] que confirma si se guardó.
    Guarda `deletedAt` de esa persona con el momento actual y pone en 0 sus no leídos; no borra mensajes.
     */
    suspend fun hideChatFor(chatId: String, userId: String): Result<Unit>

    /**
    Elimina el chat para todos: el documento y su subcolección de mensajes.
    - Qué envía ([chatId]): El identificador del chat.
    - Qué recibe: Un [Result<Unit>] que confirma si se borró.
     */
    suspend fun deleteChat(chatId: String): Result<Unit>

    // Mensajes

    /**
    Guarda un mensaje y actualiza el resumen del chat.

    - Qué envía: El [chatId] y el [message] ya validado, con el `id` vacío.
    - Qué recibe: Un [Result] con el mensaje guardado.
    En una transacción: crea el mensaje, actualiza `lastMessage`, `lastMessageType`, `lastMessageAt`, `lastSenderId`
    y suma 1 a `unreadCounts` de la otra persona.
     */
    suspend fun sendMessage(chatId: String, message: Message): Result<Message>

    /**
    Observa en vivo los últimos mensajes de un chat.

    - Qué envía: El [chatId] y el [limit] de mensajes.
    - Qué recibe: Un [Flow] con la lista de los últimos mensajes, del más antiguo al más nuevo.
    Cada mensaje nuevo que llega cuenta como una lectura de la cuota gratuita.
     */
    fun observeMessages(chatId: String, limit: Int): Flow<List<Message>>

    /**
    Obtiene una página de mensajes anteriores a un momento dado.

    - Qué envía: El [chatId], el momento [beforeSentAt] (del mensaje más antiguo ya mostrado) y el [limit].
    - Qué recibe: Un [Result] con los mensajes, del más antiguo al más nuevo.
     */
    suspend fun getMessagesBefore(chatId: String, beforeSentAt: Long, limit: Int): Result<List<Message>>

    /**
    Marca el chat como leído por una persona.

    - Qué envía: El [chatId] y el [userId].
    - Qué recibe: Un [Result<Unit>] que confirma si se guardó.
    Pone en 0 su contador de no leídos y guarda `lastReadAt`. Es una sola escritura, sin tocar cada mensaje.
     */
    suspend fun markAsRead(chatId: String, userId: String): Result<Unit>


    // Solicitudes de adopción

    /**
    Guarda la solicitud de adopción, abre el chat si no existía y deja el mensaje automático.

    - Qué envía: La [request], el [chat] (con su id determinista) y el [message] de tipo ADOPTION_INTEREST.
    - Qué recibe: Un [Result<Unit>] que confirma si todo se guardó.
    Todo o nada, en una transacción: solicitud, chat (se crea o se actualiza su resumen) y mensaje.
     */
    suspend fun sendAdoptionInterest(request: AdoptionRequest, chat: Chat, message: Message): Result<Unit>

    /**
    Busca la solicitud de una persona para una publicación.

    - Qué envía: El [postId] y el [requesterId].
    - Qué recibe: La [AdoptionRequest] o `null` si no existe (se lee por id fijo `postId_requesterId`).
     */
    suspend fun findAdoptionRequest(postId: String, requesterId: String): AdoptionRequest?

    /**
    Busca una solicitud por su id.

    - Qué envía ([id]): El identificador de la solicitud.
    - Qué recibe: La [AdoptionRequest] o `null` si no existe.
     */
    suspend fun findAdoptionRequestById(id: String): AdoptionRequest?

    /**
    Obtiene las solicitudes recibidas por una publicación, de la más nueva a la más antigua.

    - Qué envía: El [postId] y el [limit].
    - Qué recibe: Un [Result] con la lista de solicitudes.
     */
    suspend fun getRequestsByPost(postId: String, limit: Int): Result<List<AdoptionRequest>>

    /**
    Guarda la respuesta a una solicitud solo si su estado sigue siendo el esperado.

    - Qué envía: El [id], el estado que se leyó ([expected]) y la [request] con el estado nuevo.
    - Qué recibe: Un [Result<Unit>]; falla con AlreadyResponded si el estado ya cambió.
    La implementación lo hace en una transacción de Firestore (leer, comparar y escribir).
     */
    suspend fun updateRequestStatus(
        id: String,
        expected: AdoptionRequestStatus,
        request: AdoptionRequest
    ): Result<Unit>
}