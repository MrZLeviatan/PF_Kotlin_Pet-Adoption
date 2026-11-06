package co.edu.uniquindio.petadoptionapp.domain.model.comment

import kotlinx.serialization.Serializable

/**
Modelo de dominio: "me gusta" de una persona sobre un comentario.

Usa la misma técnica que [ImportantVote]: el id del documento es
"commentId_userId" ([buildId]), así que una persona solo puede dar un
like por comentario. Se guarda en la colección `commentLikes` de Firestore.

Se incluye [postId] para poder pintar de una sola consulta todos los
corazones de una publicación (`postId == X` y `userId == yo`).
Lo usan CommentRepository, ToggleCommentLikeUseCase y GetLikedCommentsUseCase.
**/
// Permite convertir el like a/desde el formato de almacenamiento.
@Serializable
// Declara el "me gusta" como clase de datos inmutable.
data class CommentLike(
    // Id del comentario que recibe el like.
    val commentId: String = "",
    // Id de la publicación a la que pertenece el comentario.
    val postId: String = "",
    // Id de la persona que dio el like.
    val userId: String = "",
    // Momento del like, en milisegundos (epoch).
    val createdAt: Long = System.currentTimeMillis()
) {

    // Utilidades compartidas sin necesidad de instanciar la clase.
    companion object {
        // Id determinista: un like por persona y comentario.
        fun buildId(commentId: String, userId: String): String = "${commentId}_$userId"
    }
}
