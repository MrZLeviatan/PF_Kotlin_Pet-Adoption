package co.edu.uniquindio.petadoptionapp.domain.usecase.comment

import co.edu.uniquindio.petadoptionapp.domain.error.CommentException
import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.comment.Comment
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointReason
import co.edu.uniquindio.petadoptionapp.domain.repository.CommentRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.NotifyUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.AwardPointsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.CommentValidator

/**
Caso de uso que crea un comentario o una respuesta en una publicación.
Solo se comenta en publicaciones verificadas y en proceso. Si la respuesta apunta a un comentario del último nivel, se aplana (lo resuelve el modelo).
Después de guardar, avisa a las personas involucradas y da puntos al comentarista (mejor esfuerzo: un fallo ahí no deshace el comentario).
Lo usa el ViewModel del detalle de publicación (sección de comentarios).
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class CreateCommentUseCase(
    // Inyecta el repositorio de comentarios.
    private val commentRepository: CommentRepository,
    // Inyecta el repositorio de publicaciones para comprobar que se pueda comentar.
    private val postRepository: PostRepository,
    // Inyecta el repositorio de usuarios para copiar la foto del autor.
    private val userRepository: UserRepository,
    // Inyecta el caso de uso que confirma que hay una sesión de usuario.
    private val requireUser: RequireUserUseCase,
    // Inyecta el caso de uso que crea notificaciones.
    private val notify: NotifyUseCase,
    // Inyecta el caso de uso que otorga puntos (también avisa si sube de nivel).
    private val awardPoints: AwardPointsUseCase
) {
    // Define la función asíncrona que recibe la publicación, el texto y, si es respuesta, el comentario padre.
    suspend operator fun invoke(
        // Publicación comentada.
        postId: String,
        // Texto del comentario.
        text: String,
        // Comentario al que se responde; null si es un comentario principal.
        parentCommentId: String? = null
    ): Result<Comment> {

        // 1. Solo un usuario con sesión puede comentar; si no la hay, devuelve el error.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // 2. Formato del texto, sin internet.
        if (!CommentValidator.isTextValid(text)) return Result.failure(CommentException.InvalidText)

        // 3. La publicación debe existir, estar verificada y seguir en proceso.
        val post = postRepository.findById(postId)
            // Si no existe, falla con PostNotFound.
            ?: return Result.failure(PostException.PostNotFound)
        // Si no está visible al público, falla con PostNotActive.
        if (!post.showsInPublicFeed()) return Result.failure(PostException.PostNotActive)

        // 4. El perfil debe existir y no estar eliminado (de ahí sale la foto).
        val profile = userRepository.findById(session.id)?.takeIf { !it.deleted }
            // Si no hay perfil válido, falla con ProfileNotFound.
            ?: return Result.failure(UserException.ProfileNotFound)

        // 5. Arma el comentario: principal o respuesta. Se guarda el padre para notificar después.
        var parent: Comment? = null
        // Decide si se crea un comentario principal o una respuesta.
        val comment = if (parentCommentId == null) {
            // Sin padre: crea un comentario principal (el id lo asigna Firestore).
            Comment.createTopLevel("", postId, session.id, session.userName, profile.photoUrl, text)
        } else {
            // Con padre: lo busca para responderle.
            parent = commentRepository.findById(postId, parentCommentId)
                // Si el padre no existe, falla con CommentNotFound.
                ?: return Result.failure(CommentException.CommentNotFound)
            // No se puede responder a un comentario eliminado.
            if (!parent.canBeRepliedTo()) return Result.failure(CommentException.CannotReplyDeleted)
            // Crea la respuesta (el modelo la aplana si el padre ya es respuesta).
            Comment.createReply("", parent, session.id, session.userName, profile.photoUrl, text)
        }

        // 6. Guarda el comentario; si sale bien, da puntos y avisa (mejor esfuerzo).
        return commentRepository.create(comment).onSuccess { saved ->

            // Puntos al comentarista; el id del comentario evita premiarlo dos veces.
            // El aviso de subir de nivel (LEVEL_UP) lo envía AwardPointsUseCase.
            awardPoints(session.id, PointReason.COMMENT_CREATED, sourceId = saved.id, postId = postId)

            // Avisa al autor del comentario padre si es una respuesta.
            if (parent != null) {
                // Crea la notificación de respuesta para el autor del comentario padre.
                notify(
                    // Destinatario: autor del comentario padre.
                    recipientId = parent.authorId,
                    // Tipo: respuesta a comentario.
                    type = NotificationType.COMMENT_REPLY,
                    // Quien responde.
                    actorId = session.id,
                    // Alias de quien responde.
                    actorName = session.userName,
                    // Publicación relacionada.
                    postId = postId,
                    // Comentario recién creado.
                    commentId = saved.id
                )
            }

            // Avisa al autor del post, salvo que ya se le avisó como autor del padre.
            if (parent == null || parent.authorId != post.authorId) {
                // Crea la notificación de comentario nuevo para el autor de la publicación.
                notify(
                    // Destinatario: autor de la publicación.
                    recipientId = post.authorId,
                    // Tipo: comentario nuevo.
                    type = NotificationType.NEW_COMMENT,
                    // Quien comenta.
                    actorId = session.id,
                    // Alias de quien comenta.
                    actorName = session.userName,
                    // Publicación relacionada.
                    postId = postId,
                    // Comentario recién creado.
                    commentId = saved.id
                )
            }
        }
    }
}
