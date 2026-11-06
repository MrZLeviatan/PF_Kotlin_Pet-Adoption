package co.edu.uniquindio.petadoptionapp.data.repository.firebase

import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.ADOPTION_REQUESTS
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.CHATS
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.MESSAGES
import co.edu.uniquindio.petadoptionapp.domain.error.AdoptionException
import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.error.ChatException
import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequest
import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequestStatus
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Chat
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Message
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
Chats en `chats/{id}` (id determinista), sus mensajes en `chats/{id}/messages` y las solicitudes de
adopción en `adoptionRequests/{postId_requesterId}`.
Lo que toca varios documentos (mensaje + resumen del chat, solicitud + chat + mensaje) va en una transacción.
 */
@Singleton
class FirestoreChatRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : ChatRepository {

    private val chats = db.collection(CHATS)
    private val requests = db.collection(ADOPTION_REQUESTS)

    private fun messages(chatId: String) = chats.document(chatId).collection(MESSAGES)

    override suspend fun openChat(chat: Chat): Result<Chat> = runCatching {
        val ref = chats.document(chat.id)
        // Si ya existe (id fijo), se devuelve el existente en lugar de crear otro.
        db.runTransaction { tx ->
            val existing = tx.get(ref).toModel<Chat>()
            if (existing != null) existing else {
                tx.set(ref, chat.toFirestore())
                chat
            }
        }.await()
    }

    override suspend fun findChatById(id: String): Chat? =
        runCatching { chats.document(id).get().await().toModel<Chat>() }.getOrNull()

    override suspend fun getChatsByParticipant(userId: String, limit: Int, afterChatId: String?): Result<List<Chat>> = runCatching {
        val query = chats.whereArrayContains("participantIds", userId)
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
        val cursor = afterChatId?.let { id ->
            chats.document(id).get().await().takeIf { it.exists() } ?: return@runCatching emptyList()
        }
        // Los ocultos para esta persona se descartan en la app; se pide un poco más para llenar la página.
        (cursor?.let { query.startAfter(it) } ?: query)
            .limit(limit.toLong() * 2)
            .get().await()
            .toModels<Chat>()
            .filter { !it.isHiddenFor(userId) }
            .take(limit)
    }

    override suspend fun countChatsWithUnread(userId: String): Result<Int> = runCatching {
        chats.whereArrayContains("participantIds", userId).get().await()
            .toModels<Chat>()
            .count { it.unreadFor(userId) > 0 }
    }

    override suspend fun hideChatFor(chatId: String, userId: String): Result<Unit> = runCatching {
        val ref = chats.document(chatId)
        if (!ref.get().await().exists()) throw ChatException.ChatNotFound
        val now = System.currentTimeMillis()
        ref.update(mapOf(
            "deletedAt.$userId" to now,
            "unreadCounts.$userId" to 0,
            "lastReadAt.$userId" to now
        )).await()
        Unit
    }

    override suspend fun deleteChat(chatId: String): Result<Unit> = runCatching {
        val ref = chats.document(chatId)
        if (!ref.get().await().exists()) throw ChatException.ChatNotFound
        // Firestore no borra las subcolecciones solo: primero los mensajes, por lotes.
        messages(chatId).get().await().documents.chunked(450).forEach { chunk ->
            db.batch().apply { chunk.forEach { delete(it.reference) } }.commit().await()
        }
        ref.delete().await()
        Unit
    }

    override suspend fun sendMessage(chatId: String, message: Message): Result<Message> = runCatching {
        val chatRef = chats.document(chatId)
        val messageRef = messages(chatId).document()
        val saved = message.copy(id = messageRef.id)
        db.runTransaction { tx ->
            val chat = tx.get(chatRef).toModel<Chat>() ?: throw ChatException.ChatNotFound
            tx.set(messageRef, saved.toFirestore())
            tx.update(chatRef, summaryUpdate(chat, saved))
        }.await()
        saved
    }.unwrapTransactionError()

    // Resumen del chat tras un mensaje: último mensaje y +1 no leído para los demás participantes.
    private fun summaryUpdate(chat: Chat, message: Message): Map<String, Any?> = buildMap {
        put("lastMessage", message.text)
        put("lastMessageType", message.type.name)
        put("lastMessageAt", message.sentAt)
        put("lastSenderId", message.senderId)
        chat.participantIds.filter { it != message.senderId }.forEach {
            put("unreadCounts.$it", FieldValue.increment(1))
        }
    }

    override fun observeMessages(chatId: String, limit: Int): Flow<List<Message>> = callbackFlow {
        // Los últimos [limit], en vivo; llegan del más nuevo al más antiguo y se voltean.
        val registration = messages(chatId)
            .orderBy("sentAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                snapshot?.let { trySend(it.toModels<Message>().reversed()) }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getMessagesBefore(chatId: String, beforeSentAt: Long, limit: Int): Result<List<Message>> = runCatching {
        messages(chatId)
            .whereLessThan("sentAt", beforeSentAt)
            .orderBy("sentAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get().await()
            .toModels<Message>()
            .reversed()
    }

    override suspend fun markAsRead(chatId: String, userId: String): Result<Unit> = runCatching {
        val ref = chats.document(chatId)
        if (!ref.get().await().exists()) throw ChatException.ChatNotFound
        // Una sola escritura: contador en 0 y momento de lectura (sin tocar cada mensaje).
        ref.update(mapOf(
            "unreadCounts.$userId" to 0,
            "lastReadAt.$userId" to System.currentTimeMillis()
        )).await()
        Unit
    }

    override suspend fun sendAdoptionInterest(
        request: AdoptionRequest,
        chat: Chat,
        message: Message
    ): Result<Unit> = runCatching {
        val chatRef = chats.document(chat.id)
        val messageRef = messages(chat.id).document()
        val saved = message.copy(id = messageRef.id)
        // Solicitud, chat y mensaje se guardan juntos o ninguno.
        db.runTransaction { tx ->
            val existing = tx.get(chatRef).toModel<Chat>()
            tx.set(requests.document(request.id), request.toFirestore())
            if (existing == null) {
                // Chat nuevo: ya trae su resumen y el no leído del autor.
                tx.set(chatRef, chat.toFirestore())
            } else {
                tx.update(chatRef, summaryUpdate(existing, saved))
            }
            tx.set(messageRef, saved.toFirestore())
        }.await()
        Unit
    }

    override suspend fun findAdoptionRequest(postId: String, requesterId: String): AdoptionRequest? =
        findAdoptionRequestById(AdoptionRequest.buildId(postId, requesterId))

    override suspend fun findAdoptionRequestById(id: String): AdoptionRequest? =
        runCatching { requests.document(id).get().await().toModel<AdoptionRequest>() }.getOrNull()

    override suspend fun getRequestsByPost(postId: String, limit: Int): Result<List<AdoptionRequest>> = runCatching {
        // Las reglas solo dejan listar las solicitudes de las publicaciones propias: la consulta lo debe decir.
        val uid = auth.currentUser?.uid ?: throw AuthException.NotAuthenticated
        requests.whereEqualTo("postId", postId)
            .whereEqualTo("postAuthorId", uid)
            .get().await()
            .toModels<AdoptionRequest>()
            .sortedByDescending { it.createdAt }
            .take(limit)
    }

    override suspend fun updateRequestStatus(
        id: String,
        expected: AdoptionRequestStatus,
        request: AdoptionRequest
    ): Result<Unit> = runCatching {
        val ref = requests.document(id)
        // Solo se escribe si el estado sigue siendo el que se leyó.
        db.runTransaction { tx ->
            val current = tx.get(ref).toModel<AdoptionRequest>() ?: throw AdoptionException.RequestNotFound
            if (current.status != expected) throw AdoptionException.AlreadyResponded
            tx.set(ref, request.toFirestore())
        }.await()
        Unit
    }.unwrapTransactionError()
}
