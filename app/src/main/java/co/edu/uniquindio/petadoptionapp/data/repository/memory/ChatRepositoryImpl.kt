package co.edu.uniquindio.petadoptionapp.data.repository.memory

import co.edu.uniquindio.petadoptionapp.domain.error.AdoptionException
import co.edu.uniquindio.petadoptionapp.domain.error.ChatException
import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequest
import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequestStatus
import co.edu.uniquindio.petadoptionapp.data.repository.send.SeedData
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Chat
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Message
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
Documenta la implementación en memoria del repositorio de chats, mensajes y solicitudes de adopción.
Simula las colecciones `chats`, `chats/{id}/messages` y `adoptionRequests`.
Las operaciones que tocan varios documentos se hacen bajo un candado, imitando una transacción (todo o nada).
 */
@Singleton
class ChatRepositoryImpl @Inject constructor() : ChatRepository {

    // Guarda los chats, con el id determinista como clave.
    private val chats = MutableStateFlow(SeedData.chats.associateBy { it.id })

    // Guarda los mensajes de cada chat, del más antiguo al más nuevo.
    private val messages = MutableStateFlow(SeedData.chatMessages)

    // Guarda las solicitudes de adopción, con el id fijo "postId_requesterId" como clave.
    private val requests = MutableStateFlow(SeedData.adoptionRequests.associateBy { it.id })

    // Candado que vuelve atómicas las operaciones sobre varios estados.
    private val lock = Mutex()

    override suspend fun openChat(chat: Chat): Result<Chat> = lock.withLock {
        // Si ya existe (id fijo), se devuelve el existente en lugar de crear otro.
        val existing = chats.value[chat.id]
        if (existing != null) return Result.success(existing)
        chats.update { it + (chat.id to chat) }
        Result.success(chat)
    }

    override suspend fun findChatById(id: String): Chat? = chats.value[id]

    override suspend fun getChatsByParticipant(userId: String, limit: Int, afterChatId: String?): Result<List<Chat>> {
        val list = chats.value.values
            .filter { userId in it.participantIds && !it.isHiddenFor(userId) }
            .sortedByDescending { it.lastMessageAt }
        val start = if (afterChatId == null) 0 else {
            val index = list.indexOfFirst { it.id == afterChatId }
            if (index < 0) return Result.success(emptyList()) else index + 1
        }
        return Result.success(list.drop(start).take(limit))
    }

    override suspend fun countChatsWithUnread(userId: String): Result<Int> =
        Result.success(chats.value.values.count { userId in it.participantIds && it.unreadFor(userId) > 0 })

    override suspend fun hideChatFor(chatId: String, userId: String): Result<Unit> = lock.withLock {
        val chat = chats.value[chatId] ?: return Result.failure(ChatException.ChatNotFound)
        val now = System.currentTimeMillis()
        chats.update {
            it + (chatId to chat.copy(
                deletedAt = chat.deletedAt + (userId to now),
                unreadCounts = chat.unreadCounts + (userId to 0),
                lastReadAt = chat.lastReadAt + (userId to now)
            ))
        }
        Result.success(Unit)
    }

    override suspend fun deleteChat(chatId: String): Result<Unit> = lock.withLock {
        if (chats.value[chatId] == null) return Result.failure(ChatException.ChatNotFound)
        chats.update { it - chatId }
        messages.update { it - chatId }
        Result.success(Unit)
    }

    override suspend fun sendMessage(chatId: String, message: Message): Result<Message> = lock.withLock {
        val chat = chats.value[chatId] ?: return Result.failure(ChatException.ChatNotFound)
        val saved = message.copy(id = UUID.randomUUID().toString())
        appendMessage(chat, saved)
        Result.success(saved)
    }

    // Guarda el mensaje y actualiza el resumen del chat: último mensaje y no leídos de los demás participantes.
    private fun appendMessage(chat: Chat, message: Message) {
        messages.update { it + (chat.id to (it[chat.id].orEmpty() + message)) }
        val unread = chat.participantIds.associateWith { id ->
            if (id == message.senderId) chat.unreadFor(id) else chat.unreadFor(id) + 1
        }
        chats.update {
            it + (chat.id to chat.copy(
                lastMessage = message.text,
                lastMessageType = message.type,
                lastMessageAt = message.sentAt,
                lastSenderId = message.senderId,
                unreadCounts = unread
            ))
        }
    }

    override fun observeMessages(chatId: String, limit: Int): Flow<List<Message>> =
        messages.map { it[chatId].orEmpty().takeLast(limit) }.distinctUntilChanged()

    override suspend fun getMessagesBefore(chatId: String, beforeSentAt: Long, limit: Int): Result<List<Message>> =
        Result.success(messages.value[chatId].orEmpty().filter { it.sentAt < beforeSentAt }.takeLast(limit))

    override suspend fun markAsRead(chatId: String, userId: String): Result<Unit> = lock.withLock {
        val chat = chats.value[chatId] ?: return Result.failure(ChatException.ChatNotFound)
        // Una sola escritura: contador en 0 y momento de lectura (sin tocar cada mensaje).
        chats.update {
            it + (chatId to chat.copy(
                unreadCounts = chat.unreadCounts + (userId to 0),
                lastReadAt = chat.lastReadAt + (userId to System.currentTimeMillis())
            ))
        }
        Result.success(Unit)
    }

    override suspend fun sendAdoptionInterest(
        request: AdoptionRequest,
        chat: Chat,
        message: Message
    ): Result<Unit> = lock.withLock {
        // Solicitud, chat y mensaje se guardan juntos o ninguno.
        requests.update { it + (request.id to request) }

        val saved = message.copy(id = UUID.randomUUID().toString())
        val existing = chats.value[chat.id]
        if (existing == null) {
            // Chat nuevo: ya trae su resumen y el no leído del autor.
            chats.update { it + (chat.id to chat) }
            messages.update { it + (chat.id to listOf(saved)) }
        } else {
            // Chat existente: solo se agrega el mensaje y se actualiza el resumen.
            appendMessage(existing, saved)
        }
        Result.success(Unit)
    }

    override suspend fun findAdoptionRequest(postId: String, requesterId: String): AdoptionRequest? =
        requests.value[AdoptionRequest.buildId(postId, requesterId)]

    override suspend fun findAdoptionRequestById(id: String): AdoptionRequest? = requests.value[id]

    override suspend fun getRequestsByPost(postId: String, limit: Int): Result<List<AdoptionRequest>> =
        Result.success(
            requests.value.values.filter { it.postId == postId }.sortedByDescending { it.createdAt }.take(limit)
        )

    override suspend fun updateRequestStatus(
        id: String,
        expected: AdoptionRequestStatus,
        request: AdoptionRequest
    ): Result<Unit> = lock.withLock {
        val current = requests.value[id] ?: return Result.failure(AdoptionException.RequestNotFound)
        // Solo se escribe si el estado sigue siendo el que se leyó.
        if (current.status != expected) return Result.failure(AdoptionException.AlreadyResponded)
        requests.update { it + (id to request) }
        Result.success(Unit)
    }
}