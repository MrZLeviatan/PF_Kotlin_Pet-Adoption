package co.edu.uniquindio.petadoptionapp.features.user.post.comments

import co.edu.uniquindio.petadoptionapp.domain.model.comment.Comment
import co.edu.uniquindio.petadoptionapp.domain.model.comment.CommentStatus

/**
 Comentario listo para dibujar. Lo usan el detalle (vista previa) y la pantalla de comentarios.
 Los textos fijos ("Comentario eliminado", "hace 2 h") los pone la UI con strings.xml.
 */
data class CommentUi(
    val id: String,
    val parentId: String?,
    val depth: Int,
    val authorId: String,
    val authorName: String,
    val authorPhotoUrl: String?,
    val text: String,
    val replyToUserName: String?,
    val ageMinutes: Int,
    val likeCount: Int,
    val replyCount: Int,
    val isLiked: Boolean,
    // Propio: no se le puede dar "me gusta" (regla del caso de uso).
    val isOwn: Boolean,
    val isDeleted: Boolean,
    val isEdited: Boolean,
)

// Traduce el modelo del dominio; `likedIds` y `viewerId` vienen de la sesión.
fun Comment.toUi(likedIds: Set<String>, viewerId: String?, now: Long = System.currentTimeMillis()) = CommentUi(
    id = id,
    parentId = parentId,
    depth = depth,
    authorId = authorId,
    authorName = authorName,
    authorPhotoUrl = authorPhotoUrl,
    // El texto de uno eliminado ya llega vacío del dominio; la UI pone el aviso.
    text = if (status == CommentStatus.DELETED) "" else text,
    replyToUserName = replyToUserName,
    ageMinutes = ((now - createdAt) / MILLIS_PER_MINUTE).toInt().coerceAtLeast(0),
    likeCount = likeCount,
    replyCount = replyCount,
    isLiked = id in likedIds,
    isOwn = authorId == viewerId,
    isDeleted = status == CommentStatus.DELETED,
    isEdited = edited,
)

private const val MILLIS_PER_MINUTE = 60 * 1000L
