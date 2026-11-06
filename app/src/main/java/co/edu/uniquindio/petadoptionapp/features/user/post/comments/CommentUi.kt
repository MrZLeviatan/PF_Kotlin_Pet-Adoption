package co.edu.uniquindio.petadoptionapp.features.user.post.comments

import co.edu.uniquindio.petadoptionapp.domain.model.comment.Comment
import co.edu.uniquindio.petadoptionapp.domain.model.comment.CommentStatus

/**
Modelo de presentación de un comentario, listo para dibujar en la interfaz.
Lo usan tanto el detalle de la publicación (vista previa de comentarios) como la pantalla
completa de comentarios (CommentsScreen) a través de CommentsViewModel y PostDetailViewModel.
Contiene los datos del autor, el texto, contadores y banderas ya calculadas (si es propio,
si el usuario le dio "me gusta", si fue eliminado o editado) para que la UI no tenga lógica.
Los textos fijos ("Comentario eliminado", "hace 2 h") los pone la UI con strings.xml.
**/
data class CommentUi(
    // Identificador único del comentario.
    val id: String,
    // Identificador del comentario padre; es null si es un comentario principal.
    val parentId: String?,
    // Nivel de anidación del comentario dentro del hilo (0 = principal).
    val depth: Int,
    // Identificador de la cuenta del autor del comentario.
    val authorId: String,
    // Nombre de usuario del autor que se muestra en la tarjeta.
    val authorName: String,
    // URL de la foto de perfil del autor; puede ser null si no tiene foto.
    val authorPhotoUrl: String?,
    // Texto del comentario (vacío si fue eliminado).
    val text: String,
    // Nombre del usuario al que se responde, para mostrar la mención "@usuario".
    val replyToUserName: String?,
    // Antigüedad del comentario en minutos, usada para el texto relativo ("hace 2 h").
    val ageMinutes: Int,
    // Cantidad de "me gusta" que tiene el comentario.
    val likeCount: Int,
    // Cantidad de respuestas que tiene el comentario.
    val replyCount: Int,
    // Indica si el usuario actual ya le dio "me gusta" a este comentario.
    val isLiked: Boolean,
    // Indica si el comentario es del usuario actual (según ToggleCommentLikeUseCase, al propio también se le puede dar "me gusta").
    val isOwn: Boolean,
    // Indica si el comentario fue eliminado (la UI muestra un aviso en lugar del texto).
    val isDeleted: Boolean,
    // Indica si el comentario fue editado después de publicarse.
    val isEdited: Boolean,
)

/**
Función de extensión que traduce el modelo del dominio Comment a CommentUi.
`likedIds` (comentarios a los que el usuario dio "me gusta") y `viewerId` (cuenta actual)
vienen de la sesión; `now` permite calcular la antigüedad y se puede fijar en pruebas.
**/
fun Comment.toUi(likedIds: Set<String>, viewerId: String?, now: Long = System.currentTimeMillis()) = CommentUi(
    // Copia el identificador del comentario.
    id = id,
    // Copia el identificador del comentario padre.
    parentId = parentId,
    // Copia el nivel de anidación.
    depth = depth,
    // Copia el identificador del autor.
    authorId = authorId,
    // Copia el nombre del autor.
    authorName = authorName,
    // Copia la URL de la foto del autor.
    authorPhotoUrl = authorPhotoUrl,
    // El texto de uno eliminado ya llega vacío del dominio; la UI pone el aviso.
    text = if (status == CommentStatus.DELETED) "" else text,
    // Copia el nombre del usuario al que se responde.
    replyToUserName = replyToUserName,
    // Calcula los minutos transcurridos desde la creación, sin permitir valores negativos.
    ageMinutes = ((now - createdAt) / MILLIS_PER_MINUTE).toInt().coerceAtLeast(0),
    // Copia el contador de "me gusta".
    likeCount = likeCount,
    // Copia el contador de respuestas.
    replyCount = replyCount,
    // Marca como "me gusta" si el id está en el conjunto de comentarios que le gustan al usuario.
    isLiked = id in likedIds,
    // Marca como propio si el autor coincide con el usuario que está viendo.
    isOwn = authorId == viewerId,
    // Marca como eliminado si el estado del dominio es DELETED.
    isDeleted = status == CommentStatus.DELETED,
    // Copia la bandera de editado del dominio.
    isEdited = edited,
)

// Constante con la cantidad de milisegundos que tiene un minuto, usada para calcular la antigüedad.
private const val MILLIS_PER_MINUTE = 60 * 1000L
