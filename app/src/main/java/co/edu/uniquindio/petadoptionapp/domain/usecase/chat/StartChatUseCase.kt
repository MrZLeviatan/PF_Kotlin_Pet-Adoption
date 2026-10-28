package co.edu.uniquindio.petadoptionapp.domain.usecase.chat

import co.edu.uniquindio.petadoptionapp.domain.error.ChatException
import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Chat
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase


// Define la clase del caso de uso para iniciar o abrir un chat inyectando los repositorios de chat, publicaciones y el caso de uso de requerir usuario.
class StartChatUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase
) {
    // Define la función asíncrona principal que recibe el ID de la publicación y devuelve un resultado con el objeto del chat.
    suspend operator fun invoke(postId: String): Result<Chat> {

        // 1. Solo un usuario con sesión puede escribir.
        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // 2. La publicación debe existir, estar verificada y seguir en proceso.
        // Busca la publicación por su ID y retorna un fallo si no existe.
        val post = postRepository.findById(postId)
            ?: return Result.failure(PostException.PostNotFound)
        // Comprueba si la publicación se muestra en el feed público y retorna un fallo si no está activa.
        if (!post.showsInPublicFeed()) return Result.failure(PostException.PostNotActive)

        // 3. No se abre un chat con uno mismo.
        // Valida que el autor de la publicación no sea el mismo usuario en sesión y retorna un fallo si intenta chatear consigo mismo.
        if (post.authorId == session.id) return Result.failure(ChatException.CannotChatWithSelf)

        // 4. Arma el chat con id fijo; si ya existe, el repositorio devuelve el existente.
        // Obtiene la marca de tiempo actual del sistema en milisegundos.
        val now = System.currentTimeMillis()
        // Construye una instancia del modelo Chat con ID determinista, participantes, referencia a la publicación, contadores de no leídos y tiempos inicializados.
        val chat = Chat(
            id = Chat.buildId(post.authorId, session.id, postId),
            participantIds = listOf(post.authorId, session.id),
            postId = postId,
            lastMessageAt = now,
            unreadCounts = mapOf(post.authorId to 0, session.id to 0),
            createdAt = now
        )
        // Solicita al repositorio abrir o recuperar el chat existente y retorna el resultado.
        return chatRepository.openChat(chat)
    }
}