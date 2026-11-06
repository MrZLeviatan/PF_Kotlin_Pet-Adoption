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

Implementa ChatRepository del dominio y se enlaza en RepositoryModule. Lo usan los casos de uso de
mensajería (abrir chat, enviar mensaje, marcar leído, ocultar/borrar) y de adopción ("Me interesa
adoptar", aceptar o rechazar solicitudes). La pantalla de conversación observa los mensajes en vivo
con observeMessages y carga los anteriores con getMessagesBefore.
**/
// Una sola instancia compartida en toda la app.
@Singleton
// Hilt inyecta Firestore y FirebaseAuth desde FirebaseModule.
class FirestoreChatRepository @Inject constructor(
    // Base de datos Firestore.
    private val db: FirebaseFirestore,
    // Autenticación, para conocer el uid de la sesión en consultas que lo exigen las reglas.
    private val auth: FirebaseAuth,
) : ChatRepository {

    // Referencia a la colección de chats.
    private val chats = db.collection(CHATS)
    // Referencia a la colección de solicitudes de adopción.
    private val requests = db.collection(ADOPTION_REQUESTS)

    // Devuelve la subcolección de mensajes de un chat.
    private fun messages(chatId: String) = chats.document(chatId).collection(MESSAGES)

    // Abre un chat: lo crea si no existe o devuelve el que ya existía.
    override suspend fun openChat(chat: Chat): Result<Chat> = runCatching {
        // Referencia con el id determinista del chat.
        val ref = chats.document(chat.id)
        // Si ya existe (id fijo), se devuelve el existente en lugar de crear otro.
        db.runTransaction { tx ->
            // Lee el chat existente, si lo hay.
            val existing = tx.get(ref).toModel<Chat>()
            // Si existe lo devuelve; si no, lo crea.
            if (existing != null) existing else {
                // Guarda el chat nuevo.
                tx.set(ref, chat.toFirestore())
                // Devuelve el chat recién creado.
                chat
            }
        // Espera a que la transacción termine.
        }.await()
    }

    // Busca un chat por id; null si no existe o falla la lectura.
    override suspend fun findChatById(id: String): Chat? =
        runCatching { chats.document(id).get().await().toModel<Chat>() }.getOrNull()

    // Lista paginada de chats en los que participa el usuario, del más reciente al más antiguo.
    override suspend fun getChatsByParticipant(userId: String, limit: Int, afterChatId: String?): Result<List<Chat>> = runCatching {
        // Chats cuyo arreglo de participantes contiene al usuario.
        val query = chats.whereArrayContains("participantIds", userId)
            // Ordenados por la fecha del último mensaje.
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
        // Documento cursor para continuar la paginación; si ya no existe, no hay más resultados.
        val cursor = afterChatId?.let { id ->
            chats.document(id).get().await().takeIf { it.exists() } ?: return@runCatching emptyList()
        }
        // Los ocultos para esta persona se descartan en la app; se pide un poco más para llenar la página.
        (cursor?.let { query.startAfter(it) } ?: query)
            // Pide el doble del límite para compensar los ocultos.
            .limit(limit.toLong() * 2)
            // Ejecuta la consulta.
            .get().await()
            // Convierte los documentos en chats.
            .toModels<Chat>()
            // Descarta los chats que el usuario ocultó.
            .filter { !it.isHiddenFor(userId) }
            // Recorta al tamaño de página pedido.
            .take(limit)
    }

    // Cuenta cuántos chats del usuario tienen mensajes sin leer (para el globo de la pestaña Mensajes).
    override suspend fun countChatsWithUnread(userId: String): Result<Int> = runCatching {
        // Lee todos los chats del usuario.
        chats.whereArrayContains("participantIds", userId).get().await()
            // Los convierte en modelos.
            .toModels<Chat>()
            // Cuenta los que tienen al menos un mensaje no leído para este usuario.
            .count { it.unreadFor(userId) > 0 }
    }

    // Oculta un chat solo para un usuario (el otro participante lo sigue viendo).
    override suspend fun hideChatFor(chatId: String, userId: String): Result<Unit> = runCatching {
        // Referencia al chat.
        val ref = chats.document(chatId)
        // Si no existe, informa el error del dominio.
        if (!ref.get().await().exists()) throw ChatException.ChatNotFound
        // Momento actual en milisegundos.
        val now = System.currentTimeMillis()
        // Actualiza solo las entradas de este usuario dentro de los mapas del chat.
        ref.update(mapOf(
            // Marca desde cuándo está oculto para el usuario.
            "deletedAt.$userId" to now,
            // Pone sus no leídos en cero.
            "unreadCounts.$userId" to 0,
            // Registra que leyó todo hasta ahora.
            "lastReadAt.$userId" to now
        )).await()
        // Resultado exitoso.
        Unit
    }

    // Borra un chat por completo, con todos sus mensajes.
    override suspend fun deleteChat(chatId: String): Result<Unit> = runCatching {
        // Referencia al chat.
        val ref = chats.document(chatId)
        // Si no existe, informa el error del dominio.
        if (!ref.get().await().exists()) throw ChatException.ChatNotFound
        // Firestore no borra las subcolecciones solo: primero los mensajes, por lotes de 450 (límite de 500 por lote).
        messages(chatId).get().await().documents.chunked(450).forEach { chunk ->
            // Crea un lote con un borrado por mensaje y lo confirma.
            db.batch().apply { chunk.forEach { delete(it.reference) } }.commit().await()
        }
        // Finalmente borra el documento del chat.
        ref.delete().await()
        // Resultado exitoso.
        Unit
    }

    // Envía un mensaje y actualiza el resumen del chat; devuelve el mensaje con su id.
    override suspend fun sendMessage(chatId: String, message: Message): Result<Message> = runCatching {
        // Referencia al chat.
        val chatRef = chats.document(chatId)
        // Nueva referencia con id automático para el mensaje.
        val messageRef = messages(chatId).document()
        // Copia del mensaje con su id.
        val saved = message.copy(id = messageRef.id)
        // Transacción: mensaje y resumen del chat se guardan juntos.
        db.runTransaction { tx ->
            // Lee el chat; si no existe, falla.
            val chat = tx.get(chatRef).toModel<Chat>() ?: throw ChatException.ChatNotFound
            // Guarda el mensaje.
            tx.set(messageRef, saved.toFirestore())
            // Actualiza el último mensaje y los no leídos del chat.
            tx.update(chatRef, summaryUpdate(chat, saved))
        // Espera a que la transacción termine.
        }.await()
        // Devuelve el mensaje guardado.
        saved
    // Extrae el error de dominio original si la transacción lo envolvió.
    }.unwrapTransactionError()

    // Resumen del chat tras un mensaje: último mensaje y +1 no leído para los demás participantes.
    private fun summaryUpdate(chat: Chat, message: Message): Map<String, Any?> = buildMap {
        // Texto del último mensaje (para la vista previa de la lista de chats).
        put("lastMessage", message.text)
        // Tipo del último mensaje (texto, imagen...).
        put("lastMessageType", message.type.name)
        // Fecha del último mensaje, usada para ordenar los chats.
        put("lastMessageAt", message.sentAt)
        // Quién envió el último mensaje.
        put("lastSenderId", message.senderId)
        // Para cada participante distinto del remitente, suma 1 a sus no leídos de forma atómica.
        chat.participantIds.filter { it != message.senderId }.forEach {
            put("unreadCounts.$it", FieldValue.increment(1))
        }
    }

    // Flujo en vivo con los últimos mensajes de un chat.
    override fun observeMessages(chatId: String, limit: Int): Flow<List<Message>> = callbackFlow {
        // Los últimos [limit], en vivo; llegan del más nuevo al más antiguo y se voltean.
        val registration = messages(chatId)
            // Del más nuevo al más antiguo, para que el límite tome los más recientes.
            .orderBy("sentAt", Query.Direction.DESCENDING)
            // Limita la cantidad de mensajes escuchados.
            .limit(limit.toLong())
            // Registra el listener en vivo.
            .addSnapshotListener { snapshot, error ->
                // Si hubo un error (por ejemplo, permisos), cierra el flujo con ese error.
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                // Emite los mensajes en orden cronológico (del más antiguo al más nuevo).
                snapshot?.let { trySend(it.toModels<Message>().reversed()) }
            }
        // Cuando el colector deja de escuchar, elimina el listener para no gastar lecturas.
        awaitClose { registration.remove() }
    }

    // Carga mensajes anteriores a una fecha (scroll hacia arriba en la conversación).
    override suspend fun getMessagesBefore(chatId: String, beforeSentAt: Long, limit: Int): Result<List<Message>> = runCatching {
        // Consulta la subcolección de mensajes.
        messages(chatId)
            // Solo los enviados antes de la fecha dada.
            .whereLessThan("sentAt", beforeSentAt)
            // Del más nuevo al más antiguo, para tomar los inmediatamente anteriores.
            .orderBy("sentAt", Query.Direction.DESCENDING)
            // Limita el tamaño de la página.
            .limit(limit.toLong())
            // Ejecuta la consulta.
            .get().await()
            // Convierte los documentos en mensajes.
            .toModels<Message>()
            // Los voltea a orden cronológico.
            .reversed()
    }

    // Marca el chat como leído por el usuario.
    override suspend fun markAsRead(chatId: String, userId: String): Result<Unit> = runCatching {
        // Referencia al chat.
        val ref = chats.document(chatId)
        // Si no existe, informa el error del dominio.
        if (!ref.get().await().exists()) throw ChatException.ChatNotFound
        // Una sola escritura: contador en 0 y momento de lectura (sin tocar cada mensaje).
        ref.update(mapOf(
            // No leídos del usuario en cero.
            "unreadCounts.$userId" to 0,
            // Momento de la última lectura.
            "lastReadAt.$userId" to System.currentTimeMillis()
        )).await()
        // Resultado exitoso.
        Unit
    }

    // Registra el interés de adopción: guarda la solicitud, abre o actualiza el chat y envía el primer mensaje.
    override suspend fun sendAdoptionInterest(
        // Solicitud de adopción a guardar.
        request: AdoptionRequest,
        // Chat entre el interesado y el autor de la publicación.
        chat: Chat,
        // Mensaje inicial del interesado.
        message: Message
    ): Result<Unit> = runCatching {
        // Referencia al chat.
        val chatRef = chats.document(chat.id)
        // Nueva referencia con id automático para el mensaje.
        val messageRef = messages(chat.id).document()
        // Copia del mensaje con su id.
        val saved = message.copy(id = messageRef.id)
        // Solicitud, chat y mensaje se guardan juntos o ninguno.
        db.runTransaction { tx ->
            // Lee el chat por si ya existía una conversación previa.
            val existing = tx.get(chatRef).toModel<Chat>()
            // Guarda la solicitud de adopción.
            tx.set(requests.document(request.id), request.toFirestore())
            // Decide si crear el chat o solo actualizar su resumen.
            if (existing == null) {
                // Chat nuevo: ya trae su resumen y el no leído del autor.
                tx.set(chatRef, chat.toFirestore())
            } else {
                // Chat existente: actualiza último mensaje y no leídos.
                tx.update(chatRef, summaryUpdate(existing, saved))
            }
            // Guarda el mensaje inicial.
            tx.set(messageRef, saved.toFirestore())
        // Espera a que la transacción termine.
        }.await()
        // Resultado exitoso.
        Unit
    }

    // Busca la solicitud de un usuario para una publicación, construyendo su id determinista.
    override suspend fun findAdoptionRequest(postId: String, requesterId: String): AdoptionRequest? =
        findAdoptionRequestById(AdoptionRequest.buildId(postId, requesterId))

    // Busca una solicitud por id; null si no existe o falla la lectura.
    override suspend fun findAdoptionRequestById(id: String): AdoptionRequest? =
        runCatching { requests.document(id).get().await().toModel<AdoptionRequest>() }.getOrNull()

    // Lista las solicitudes de adopción de una publicación propia.
    override suspend fun getRequestsByPost(postId: String, limit: Int): Result<List<AdoptionRequest>> = runCatching {
        // Las reglas solo dejan listar las solicitudes de las publicaciones propias: la consulta lo debe decir.
        val uid = auth.currentUser?.uid ?: throw AuthException.NotAuthenticated
        // Solicitudes de la publicación.
        requests.whereEqualTo("postId", postId)
            // Y cuyo autor es el usuario de la sesión (requisito de las reglas de seguridad).
            .whereEqualTo("postAuthorId", uid)
            // Ejecuta la consulta.
            .get().await()
            // Convierte los documentos en solicitudes.
            .toModels<AdoptionRequest>()
            // Ordena en memoria de la más reciente a la más antigua (evita requerir un índice compuesto).
            .sortedByDescending { it.createdAt }
            // Recorta al límite pedido.
            .take(limit)
    }

    // Cambia el estado de una solicitud solo si nadie la respondió antes.
    override suspend fun updateRequestStatus(
        // Id de la solicitud.
        id: String,
        // Estado que se espera encontrar (el que se leyó al mostrarla).
        expected: AdoptionRequestStatus,
        // Solicitud con los datos nuevos.
        request: AdoptionRequest
    ): Result<Unit> = runCatching {
        // Referencia a la solicitud.
        val ref = requests.document(id)
        // Solo se escribe si el estado sigue siendo el que se leyó.
        db.runTransaction { tx ->
            // Lee el estado actual; si no existe, falla.
            val current = tx.get(ref).toModel<AdoptionRequest>() ?: throw AdoptionException.RequestNotFound
            // Si el estado cambió entretanto, la solicitud ya fue respondida.
            if (current.status != expected) throw AdoptionException.AlreadyResponded
            // Guarda la solicitud actualizada.
            tx.set(ref, request.toFirestore())
        // Espera a que la transacción termine.
        }.await()
        // Resultado exitoso.
        Unit
    // Extrae el error de dominio original si la transacción lo envolvió.
    }.unwrapTransactionError()
}
