package co.edu.uniquindio.petadoptionapp.domain.model.comment

/**
 "Me gusta" de una persona sobre un comentario.

 Usa la misma técnica que [ImportantVote]: el id del documento es
 "commentId_userId" ([buildId]), así que una persona solo puede dar un
 like por comentario. Se guarda en la colección `commentLikes`.

 Se incluye [postId] para poder pintar de una sola consulta todos los
 corazones de una publicación (`postId == X` y `userId == yo`).
 */
data class CommentLike(
    val commentId: String = "",
    val postId: String = "",
    val userId: String = "",
    // Momento del like, en milisegundos (epoch).
    val createdAt: Long = System.currentTimeMillis()
) {

    companion object {
        // Id determinista: un like por persona y comentario.
        fun buildId(commentId: String, userId: String): String = "${commentId}_$userId"
    }
}