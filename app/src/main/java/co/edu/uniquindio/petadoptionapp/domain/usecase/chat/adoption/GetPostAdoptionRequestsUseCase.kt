package co.edu.uniquindio.petadoptionapp.domain.usecase.chat.adoption

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequest
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

// Define la clase del caso de uso para listar las solicitudes de adopción recibidas por una publicación inyectando los repositorios de chats, publicaciones y el caso de uso de requerir usuario.
class GetPostAdoptionRequestsUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase
) {
    // Define un objeto de acompañamiento que establece el límite máximo de resultados por consulta en 50.
    companion object { const val MAX_RESULTS = 50 }

    // Define la función asíncrona principal que recibe el ID de la publicación y devuelve un resultado con la lista de solicitudes de adopción.
    suspend operator fun invoke(postId: String): Result<List<AdoptionRequest>> {
        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // Busca la publicación por su ID y retorna un fallo si no existe.
        val post = postRepository.findById(postId)
            ?: return Result.failure(PostException.PostNotFound)
        // Valida que el autor de la publicación coincida con el usuario en sesión y retorna un fallo si no está autorizado para ver las solicitudes.
        if (post.authorId != session.id) return Result.failure(PostException.NotAllowed)

        // Solicita al repositorio las solicitudes de adopción asociadas a la publicación utilizando el límite máximo definido y retorna el resultado.
        return chatRepository.getRequestsByPost(postId, MAX_RESULTS)
    }
}