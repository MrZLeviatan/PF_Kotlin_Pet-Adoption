package co.edu.uniquindio.petadoptionapp.domain.usecase.comment

import co.edu.uniquindio.petadoptionapp.domain.repository.CommentRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

/**
Documenta el caso de uso que devuelve los comentarios de una publicación a los que la persona dio like.
 */
class GetLikedCommentsUseCase(
    // Inyecta el repositorio de comentarios.
    private val commentRepository: CommentRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase
) {
    // Define la función asíncrona principal que recibe el ID de la publicación y devuelve un resultado con un conjunto de identificadores de comentarios.
    suspend operator fun invoke(postId: String): Result<Set<String>> {
        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }
        // Solicita al repositorio el conjunto de IDs de comentarios que el usuario ha marcado como gustados para la publicación especificada.
        return commentRepository.getLikedCommentIds(postId, session.id)
    }
}