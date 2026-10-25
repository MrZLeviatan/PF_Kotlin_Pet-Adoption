package co.edu.uniquindio.petadoptionapp.domain.usecase.comment

import co.edu.uniquindio.petadoptionapp.domain.error.CommentException
import co.edu.uniquindio.petadoptionapp.domain.model.comment.CommentStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.CommentRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

/**
Documenta el caso de uso que da o quita el "me gusta" a un comentario.
No se permite dar like al propio comentario, así no se inflan los puntos de reputación.
 */
class ToggleCommentLikeUseCase(
    // Inyecta el repositorio de comentarios.
    private val commentRepository: CommentRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase
) {
    // Define la función asíncrona principal que recibe el ID de la publicación y el ID del comentario, devolviendo un resultado con un valor booleano.
    suspend operator fun invoke(postId: String, commentId: String): Result<Boolean> {

        // Solo un usuario con sesión puede dar like.
        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // El comentario debe existir y seguir activo.
        // Busca el comentario activo por su ID y retorna un fallo si no existe o no está activo.
        val comment = commentRepository.findById(postId, commentId)
            ?.takeIf { it.status == CommentStatus.ACTIVE }
            ?: return Result.failure(CommentException.CommentNotFound)

        // No se da like al propio comentario.
        // Comprueba si el usuario es el autor del comentario y retorna un fallo si intenta dar me gusta a su propio comentario.
        if (comment.authorId == session.id) return Result.failure(CommentException.CannotLikeOwnComment)

        // Pendiente: puntos al autor (PointRules) solo cuando el resultado sea `true`.
        // Alterna el estado del "me gusta" en el repositorio utilizando el ID del usuario y retorna el resultado.
        return commentRepository.toggleLike(postId, commentId, session.id)
    }
}