package co.edu.uniquindio.petadoptionapp.domain.usecase.comment

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.error.CommentException
import co.edu.uniquindio.petadoptionapp.domain.model.comment.CommentStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.CommentRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase

/**
Documenta el caso de uso que elimina un comentario (borrado lógico).
Puede hacerlo su autor o cualquier moderador activo.
 */
class DeleteCommentUseCase(
    // Inyecta el repositorio de comentarios.
    private val commentRepository: CommentRepository,
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    // Define la función asíncrona principal que recibe el ID de la publicación y el ID del comentario, devolviendo un resultado vacío.
    suspend operator fun invoke(postId: String, commentId: String): Result<Unit> {

        // Sin sesión abierta no se puede hacer nada.
        // Obtiene la cuenta actual de la sesión y retorna un fallo si el usuario no está autenticado.
        val session = authRepository.currentAccount.value
            ?: return Result.failure(AuthException.NotAuthenticated)

        // Busca el comentario; uno ya eliminado se trata como inexistente.
        // Busca el comentario activo por su ID y retorna un fallo si no existe o no está activo.
        val comment = commentRepository.findById(postId, commentId)
            ?.takeIf { it.status == CommentStatus.ACTIVE }
            ?: return Result.failure(CommentException.CommentNotFound)

        // Permiso: el autor, o un moderador activo (se confirma contra su perfil).
        // Comprueba si el usuario en sesión es el autor del comentario.
        val isAuthor = comment.authorId == session.id
        // Valida si el usuario es moderador activo mediante el caso de uso correspondiente si no es el autor, retornando un fallo si no tiene permisos.
        if (!isAuthor) requireModerator().getOrElse { return Result.failure(CommentException.NotAllowed) }

        // Marca el comentario como eliminado en el repositorio y retorna el resultado.
        return commentRepository.markDeleted(postId, commentId)
    }
}