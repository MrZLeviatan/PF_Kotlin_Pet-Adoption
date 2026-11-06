package co.edu.uniquindio.petadoptionapp.domain.usecase.chat.adoption

import co.edu.uniquindio.petadoptionapp.domain.error.AdoptionException
import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequest
import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequestStatus
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Chat
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Message
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.MessageType
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.NotifyUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.AwardPointsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.ChatValidator

/**
Caso de uso del botón "Me interesa adoptar".
Crea la solicitud, abre el chat con el autor y deja un mensaje automático, todo en una sola transacción de Firestore.
Solo aplica a publicaciones visibles de categorías que admiten adopción, y nunca a la propia.
Al terminar notifica al autor. Lo usa el ViewModel del detalle de publicación.
**/
// Define la clase del caso de uso para enviar una solicitud de interés de adopción inyectando los repositorios de chats, publicaciones, catálogos y el caso de uso de requerir usuario.
class SendAdoptionInterestUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el repositorio de catálogos para saber si la categoría admite adopción.
    private val catalogRepository: CatalogRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase,
    // Inyecta el caso de uso que crea notificaciones.
    private val notify: NotifyUseCase

) {
    // Define la función asíncrona principal que recibe el ID de la publicación y un mensaje opcional, devolviendo un resultado con la solicitud de adopción creada.
    suspend operator fun invoke(postId: String, message: String = ""): Result<AdoptionRequest> {

        // 1. Solo un usuario con sesión puede solicitar.
        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // 2. Formato del mensaje opcional, sin internet.
        // Recorta el texto del mensaje y valida que cumpla con el formato permitido para solicitudes de adopción, retornando un fallo si no es válido.
        val cleanMessage = message.trim()
        // Comprueba que el mensaje no supere el máximo permitido.
        if (!ChatValidator.isAdoptionMessageValid(cleanMessage))
            // Falla con InvalidMessage si no es válido.
            return Result.failure(AdoptionException.InvalidMessage)

        // 3. La publicación debe existir, estar verificada y seguir en proceso.
        // Busca la publicación por su ID y retorna un fallo si no existe.
        val post = postRepository.findById(postId)
            // Si no existe, falla con PostNotFound.
            ?: return Result.failure(PostException.PostNotFound)
        // Comprueba si la publicación se muestra en el feed público y retorna un fallo si no está activa.
        if (!post.showsInPublicFeed()) return Result.failure(PostException.PostNotActive)

        // 4. No se solicita la propia publicación.
        // Valida que el autor de la publicación no sea el mismo usuario en sesión y retorna un fallo si intenta solicitar su propia publicación.
        if (post.authorId == session.id) return Result.failure(AdoptionException.CannotRequestOwnPost)

        // 5. La categoría debe admitir adopción (lo define el catálogo, no el código).
        // Busca la categoría de la publicación en el catálogo y valida que exista y permita solicitudes de adopción, retornando un fallo en caso contrario.
        val category = catalogRepository.findCategoryById(post.categoryId)
        // Falla si la categoría no existe o no admite adopción.
        if (category == null || !category.allowsAdoptionRequests)
            // Devuelve NotAdoptable.
            return Result.failure(AdoptionException.NotAdoptable)

        // 6. Si ya hay una solicitud vigente (o respondida), no se repite; solo se reenvía si la cancelaron.
        // Busca si ya existe una solicitud previa para esta publicación por parte del usuario y retorna un fallo si se encuentra una solicitud activa o respondida.
        val existing = chatRepository.findAdoptionRequest(postId, session.id)
        // Solo se permite repetir si la anterior fue cancelada.
        if (existing != null && existing.status != AdoptionRequestStatus.CANCELLED)
            // Devuelve AlreadyRequested.
            return Result.failure(AdoptionException.AlreadyRequested)

        // 7. Arma la solicitud, el chat (id fijo) y el mensaje automático.
        // Obtiene la marca de tiempo actual del sistema y construye el ID determinista del chat.
        val now = System.currentTimeMillis()
        // Id determinista del chat entre autor y solicitante para esta publicación.
        val chatId = Chat.buildId(post.authorId, session.id, postId)
        // Construye la instancia de la solicitud de adopción con identificadores, autor, solicitante, mensaje y marcas de tiempo.
        val request = AdoptionRequest(
            // Id determinista: una solicitud por persona y publicación.
            id = AdoptionRequest.buildId(postId, session.id),
            // Publicación solicitada.
            postId = postId,
            // Autor que recibe la solicitud.
            postAuthorId = post.authorId,
            // Persona que solicita.
            requesterId = session.id,
            // Alias del solicitante, copiado para listar rápido.
            requesterName = session.userName,
            // Mensaje opcional ya limpio.
            message = cleanMessage,
            // Chat asociado a la solicitud.
            chatId = chatId,
            // Momento de creación.
            createdAt = now
        )
        // Construye la instancia del chat inicializado con los participantes, referencia a la publicación, tipo de último mensaje, contadores de no leídos y marcas de tiempo.
        val chat = Chat(
            // Id determinista calculado arriba.
            id = chatId,
            // Participantes: autor y solicitante.
            participantIds = listOf(post.authorId, session.id),
            // Publicación que origina el chat.
            postId = postId,
            // El último mensaje es el automático de interés.
            lastMessageType = MessageType.ADOPTION_INTEREST,
            // Momento del último mensaje.
            lastMessageAt = now,
            // Quien envió el último mensaje: el solicitante.
            lastSenderId = session.id,
            // El autor queda con 1 sin leer; el solicitante con 0.
            unreadCounts = mapOf(post.authorId to 1, session.id to 0),
            // Momento de creación.
            createdAt = now
        )
        // Construye el mensaje automático de interés de adopción enviado por el usuario con el contenido recortado y la marca de tiempo actual.
        val autoMessage = Message(
            // Remitente: el solicitante.
            senderId = session.id,
            // Tipo automático: la UI pone el texto traducido.
            type = MessageType.ADOPTION_INTEREST,
            // Mensaje opcional del solicitante.
            text = cleanMessage,
            // Momento del envío.
            sentAt = now
        )

        // 8. Guarda todo o nada y avisa al autor (la notificación abre el chat).
        // Solicita al repositorio guardar la solicitud, el chat y el mensaje en una sola transacción atómica, mapeando el resultado final para devolver la solicitud creada.
        return chatRepository.sendAdoptionInterest(request, chat, autoMessage)
            // Si se guardó, notifica al autor con acceso directo al chat.
            .onSuccess {
                // Crea la notificación de solicitud de adopción.
                notify(post.authorId, NotificationType.ADOPTION_REQUEST, session.id, session.userName, postId, chatId = chatId)
            }
            // Devuelve la solicitud creada.
            .map { request }
    }
}