package co.edu.uniquindio.petadoptionapp.domain.usecase.comment

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.model.comment.Comment
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.CommentRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetPostDetailUseCase

/**
Documenta el caso de uso que entrega una página de comentarios principales o de respuestas.
Reutiliza GetPostDetailUseCase: quien no puede ver la publicación tampoco ve sus comentarios.
 */
class GetCommentsUseCase(
    // Inyecta el repositorio de comentarios.
    private val commentRepository: CommentRepository,
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository,
    // Inyecta el caso de uso que ya aplica las reglas de visibilidad de una publicación.
    private val getPostDetail: GetPostDetailUseCase
) {
    // Define un objeto de acompañamiento que establece la cantidad máxima de comentarios por página en 15.
    companion object { const val PAGE_SIZE = 15 }

    // Define la función asíncrona principal que recibe el ID de la publicación, el ID opcional del comentario padre y el paginador, devolviendo un resultado con la lista de comentarios.
    suspend operator fun invoke(
        postId: String,
        parentCommentId: String? = null,
        afterCommentId: String? = null
    ): Result<List<Comment>> {

        // Sin sesión no se ve nada.
        // Obtiene la cuenta actual de la sesión y retorna un fallo si el usuario no está autenticado.
        val session = authRepository.currentAccount.value
            ?: return Result.failure(AuthException.NotAuthenticated)

        // Si la persona no puede ver la publicación, no ve sus comentarios.
        // Valida la visibilidad de la publicación mediante el caso de uso correspondiente y retorna un fallo si falla.
        getPostDetail(session, postId).getOrElse { return Result.failure(it) }

        // Principales (sin padre) o respuestas de un comentario.
        // Evalúa si se solicitan comentarios principales de nivel superior o respuestas anidadas de un comentario específico.
        return if (parentCommentId == null)
        // Solicita al repositorio los comentarios principales paginados.
            commentRepository.getTopLevel(postId, PAGE_SIZE, afterCommentId)
        else
        // Solicita al repositorio las respuestas del comentario padre paginadas.
            commentRepository.getReplies(postId, parentCommentId, PAGE_SIZE, afterCommentId)
    }
}