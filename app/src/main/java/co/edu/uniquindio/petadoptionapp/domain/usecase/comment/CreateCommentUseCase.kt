package co.edu.uniquindio.petadoptionapp.domain.usecase.comment

import co.edu.uniquindio.petadoptionapp.domain.error.CommentException
import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.comment.Comment
import co.edu.uniquindio.petadoptionapp.domain.repository.CommentRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.CommentValidator

/**
Documenta el caso de uso que crea un comentario o una respuesta en una publicación.
Solo se comenta en publicaciones verificadas y en proceso. Si la respuesta apunta a un comentario del último nivel, se aplana (lo resuelve el modelo).
 */
class CreateCommentUseCase(
    // Inyecta el repositorio de comentarios.
    private val commentRepository: CommentRepository,
    // Inyecta el repositorio de publicaciones para comprobar que se pueda comentar.
    private val postRepository: PostRepository,
    // Inyecta el repositorio de usuarios para copiar la foto del autor.
    private val userRepository: UserRepository,
    // Inyecta el caso de uso que confirma que hay una sesión de usuario.
    private val requireUser: RequireUserUseCase
) {
    suspend operator fun invoke(
        postId: String,
        text: String,
        parentCommentId: String? = null
    ): Result<Comment> {

        // 1. Solo un usuario con sesión puede comentar.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // 2. Formato del texto, sin internet.
        if (!CommentValidator.isTextValid(text)) return Result.failure(CommentException.InvalidText)

        // 3. La publicación debe existir, estar verificada y seguir en proceso.
        val post = postRepository.findById(postId)
            ?: return Result.failure(PostException.PostNotFound)
        if (!post.showsInPublicFeed()) return Result.failure(PostException.PostNotActive)

        // 4. El perfil debe existir y no estar eliminado (de ahí sale la foto).
        val profile = userRepository.findById(session.id)?.takeIf { !it.deleted }
            ?: return Result.failure(UserException.ProfileNotFound)

        // 5. Arma el comentario: principal o respuesta.
        val comment = if (parentCommentId == null) {
            Comment.createTopLevel("", postId, session.id, session.userName, profile.photoUrl, text)
        } else {
            val parent = commentRepository.findById(postId, parentCommentId)
                ?: return Result.failure(CommentException.CommentNotFound)
            if (!parent.canBeRepliedTo()) return Result.failure(CommentException.CannotReplyDeleted)
            Comment.createReply("", parent, session.id, session.userName, profile.photoUrl, text)
        }

        // 6. Guarda. Pendiente: notificación al autor del post o del comentario padre (no a uno mismo) y puntos.
        return commentRepository.create(comment)
    }
}