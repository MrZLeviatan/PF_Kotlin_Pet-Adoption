package co.edu.uniquindio.petadoptionapp.domain.usecase.comment

import co.edu.uniquindio.petadoptionapp.domain.error.CommentException
import co.edu.uniquindio.petadoptionapp.domain.model.comment.CommentStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.CommentRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.CommentValidator

/**
Caso de uso que edita un comentario propio que sigue activo.
Solo el autor puede editarlo; el texto se guarda limpio en Firestore.
Lo usa el ViewModel del detalle de publicación (sección de comentarios).
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class EditCommentUseCase(
    // Inyecta el repositorio de comentarios.
    private val commentRepository: CommentRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase
) {
    // Define la función asíncrona principal que recibe el ID de la publicación, el ID del comentario y el texto, devolviendo un resultado vacío.
    suspend operator fun invoke(postId: String, commentId: String, text: String): Result<Unit> {

        // Solo un usuario con sesión puede editar.
        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // Formato del texto, sin internet.
        // Valida si el formato del texto es válido y retorna un fallo si no lo es.
        if (!CommentValidator.isTextValid(text)) return Result.failure(CommentException.InvalidText)

        // Busca el comentario; uno eliminado se trata como inexistente.
        // Busca el comentario activo por su ID y retorna un fallo si no existe o no está activo.
        val comment = commentRepository.findById(postId, commentId)
            // Solo lo conserva si sigue activo.
            ?.takeIf { it.status == CommentStatus.ACTIVE }
            // Si no existe o fue eliminado, falla con CommentNotFound.
            ?: return Result.failure(CommentException.CommentNotFound)

        // Solo el autor puede editarlo.
        // Comprueba si el usuario en sesión es el autor del comentario y retorna un fallo si no tiene permisos.
        if (comment.authorId != session.id) return Result.failure(CommentException.NotAllowed)

        // Actualiza el texto del comentario en el repositorio con el texto limpio y retorna el resultado.
        return commentRepository.updateText(postId, commentId, text.trim())
    }
}
