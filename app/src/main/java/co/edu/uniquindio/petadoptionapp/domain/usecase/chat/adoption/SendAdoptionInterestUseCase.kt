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
Documenta el caso de uso del botón "Me interesa adoptar".
Crea la solicitud, abre el chat con el autor y deja un mensaje automático, todo en una sola transacción.
Solo aplica a publicaciones visibles de categorías que admiten adopción, y nunca a la propia.
 */
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
        if (!ChatValidator.isAdoptionMessageValid(cleanMessage))
            return Result.failure(AdoptionException.InvalidMessage)

        // 3. La publicación debe existir, estar verificada y seguir en proceso.
        // Busca la publicación por su ID y retorna un fallo si no existe.
        val post = postRepository.findById(postId)
            ?: return Result.failure(PostException.PostNotFound)
        // Comprueba si la publicación se muestra en el feed público y retorna un fallo si no está activa.
        if (!post.showsInPublicFeed()) return Result.failure(PostException.PostNotActive)

        // 4. No se solicita la propia publicación.
        // Valida que el autor de la publicación no sea el mismo usuario en sesión y retorna un fallo si intenta solicitar su propia publicación.
        if (post.authorId == session.id) return Result.failure(AdoptionException.CannotRequestOwnPost)

        // 5. La categoría debe admitir adopción (lo define el catálogo, no el código).
        // Busca la categoría de la publicación en el catálogo y valida que exista y permita solicitudes de adopción, retornando un fallo en caso contrario.
        val category = catalogRepository.findCategoryById(post.categoryId)
        if (category == null || !category.allowsAdoptionRequests)
            return Result.failure(AdoptionException.NotAdoptable)

        // 6. Si ya hay una solicitud vigente (o respondida), no se repite; solo se reenvía si la cancelaron.
        // Busca si ya existe una solicitud previa para esta publicación por parte del usuario y retorna un fallo si se encuentra una solicitud activa o respondida.
        val existing = chatRepository.findAdoptionRequest(postId, session.id)
        if (existing != null && existing.status != AdoptionRequestStatus.CANCELLED)
            return Result.failure(AdoptionException.AlreadyRequested)

        // 7. Arma la solicitud, el chat (id fijo) y el mensaje automático.
        // Obtiene la marca de tiempo actual del sistema y construye el ID determinista del chat.
        val now = System.currentTimeMillis()
        val chatId = Chat.buildId(post.authorId, session.id, postId)
        // Construye la instancia de la solicitud de adopción con identificadores, autor, solicitante, mensaje y marcas de tiempo.
        val request = AdoptionRequest(
            id = AdoptionRequest.buildId(postId, session.id),
            postId = postId,
            postAuthorId = post.authorId,
            requesterId = session.id,
            requesterName = session.userName,
            message = cleanMessage,
            chatId = chatId,
            createdAt = now
        )
        // Construye la instancia del chat inicializado con los participantes, referencia a la publicación, tipo de último mensaje, contadores de no leídos y marcas de tiempo.
        val chat = Chat(
            id = chatId,
            participantIds = listOf(post.authorId, session.id),
            postId = postId,
            lastMessageType = MessageType.ADOPTION_INTEREST,
            lastMessageAt = now,
            lastSenderId = session.id,
            unreadCounts = mapOf(post.authorId to 1, session.id to 0),
            createdAt = now
        )
        // Construye el mensaje automático de interés de adopción enviado por el usuario con el contenido recortado y la marca de tiempo actual.
        val autoMessage = Message(
            senderId = session.id,
            type = MessageType.ADOPTION_INTEREST,
            text = cleanMessage,
            sentAt = now
        )

        // 8. Guarda todo o nada. Pendiente: NotifyUseCase ADOPTION_REQUEST al autor.
        // Solicita al repositorio guardar la solicitud, el chat y el mensaje en una sola transacción atómica, mapeando el resultado final para devolver la solicitud creada.
        return chatRepository.sendAdoptionInterest(request, chat, autoMessage)
            .onSuccess {
                notify(post.authorId, NotificationType.ADOPTION_REQUEST, session.id, session.userName, postId)
            }
            .map { request }
    }
}