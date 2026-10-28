package co.edu.uniquindio.petadoptionapp.domain.model.comment


/**
 Comentario de una publicación, que puede tener respuestas en un solo nivel
 ([MAX_DEPTH]), como en Facebook: todas las respuestas de un hilo cuelgan del
 comentario principal. Responder a una respuesta la deja en ese mismo hilo y
 [replyToUserName] indica a quién se contesta ("@alias").

 Se guarda en la subcolección `posts/{postId}/comments`. Todos los
 niveles viven en la MISMA subcolección; la jerarquía se arma con
 [parentId]. Los datos del autor ([authorName], [authorPhotoUrl]) se
 copian para dibujar la lista sin una lectura extra por comentario.
 Alias y foto son públicos incluso en perfiles privados.
 */
data class Comment(
    val id: String = "",
    // Publicación a la que pertenece.
    val postId: String = "",
    // Id del comentario al que responde; nulo si es principal.
    val parentId: String? = null,
    // Nivel: 0 principales, 1 respuestas (no hay más niveles).
    val depth: Int = 0,
    // Alias de la persona a quien se responde cuando se contesta a otra respuesta del hilo; nulo en los demás casos. Sirve para mostrar "@alias".
    val replyToUserName: String? = null,
    // Id de quien comenta (`Account.id`).
    val authorId: String = "",
    // Alias del autor al momento de comentar.
    val authorName: String = "",
    // Foto del autor; nula si no tiene.
    val authorPhotoUrl: String? = null,
    // Contenido del comentario.
    val text: String = "",
    // Estado del comentario.
    val status: CommentStatus = CommentStatus.ACTIVE,
    // Cantidad de "me gusta".
    val likeCount: Int = 0,
    // Cantidad de respuestas del hilo (en un comentario principal son todas las respuestas).
    val replyCount: Int = 0,
    // `true` si el autor lo editó después de publicarlo.
    val edited: Boolean = false,
    // Momento de creación, en milisegundos (epoch).
    val createdAt: Long = System.currentTimeMillis(),
    // Última modificación, en milisegundos (epoch).
    val updatedAt: Long = System.currentTimeMillis()
) {

    // ¿Es una respuesta (tiene comentario padre)?
    fun hasParent(): Boolean = parentId != null

    // ¿Se puede responder? No si está eliminado.
    fun canBeRepliedTo(): Boolean = status == CommentStatus.ACTIVE

    // Texto para mostrar: el contenido o un aviso si fue eliminado.
    fun visibleText(): String =
        if (status == CommentStatus.DELETED) DELETED_PLACEHOLDER else text

    // Devuelve una copia con el texto editado y marcada como editada.
    fun edit(newText: String): Comment = copy(
        text = validate(newText),
        edited = true,
        updatedAt = System.currentTimeMillis()
    )

    // Devuelve una copia eliminada lógicamente, sin conservar el texto.
    fun markAsDeleted(): Comment = copy(
        status = CommentStatus.DELETED,
        text = "",
        updatedAt = System.currentTimeMillis()
    )

    companion object {
        // Longitud máxima del texto.
        const val MAX_LENGTH = 500

        // Niveles de respuesta: 1 = respuestas solo bajo el comentario principal (estilo Facebook).
        const val MAX_DEPTH = 1

        // Texto mostrado en lugar de un comentario eliminado.
        const val DELETED_PLACEHOLDER = "Comentario eliminado"

        // Válida el texto: no vacío y dentro del límite. Devuelve el texto limpio.
        private fun validate(text: String): String {
            val clean = text.trim()
            require(clean.isNotEmpty()) { "El comentario no puede estar vacío" }
            require(clean.length <= MAX_LENGTH) { "Máximo $MAX_LENGTH caracteres" }
            return clean
        }

        // Crea un comentario principal (nivel 0) sobre una publicación.
        fun createTopLevel(
            id: String,
            postId: String,
            authorId: String,
            authorName: String,
            authorPhotoUrl: String?,
            text: String
        ): Comment = Comment(
            id = id,
            postId = postId,
            authorId = authorId,
            authorName = authorName,
            authorPhotoUrl = authorPhotoUrl,
            text = validate(text)
        )

        /**
         Crea una respuesta a [parent]. Si el padre ya es una respuesta (está en
         el último nivel, [MAX_DEPTH]), la nueva se "aplana": queda en el mismo
         hilo, bajo el comentario principal, y [replyToUserName] indica a quién
         se contesta. Al responder al principal no hace falta "@alias".
         */
        fun createReply(
            id: String,
            parent: Comment,
            authorId: String,
            authorName: String,
            authorPhotoUrl: String?,
            text: String
        ): Comment {
            require(parent.canBeRepliedTo()) { "No se puede responder a un comentario eliminado" }
            val flatten = parent.depth >= MAX_DEPTH
            return Comment(
                id = id,
                postId = parent.postId,
                parentId = if (flatten) parent.parentId else parent.id,
                depth = if (flatten) parent.depth else parent.depth + 1,
                replyToUserName = if (flatten) parent.authorName else null,
                authorId = authorId,
                authorName = authorName,
                authorPhotoUrl = authorPhotoUrl,
                text = validate(text)
            )
        }
    }
}