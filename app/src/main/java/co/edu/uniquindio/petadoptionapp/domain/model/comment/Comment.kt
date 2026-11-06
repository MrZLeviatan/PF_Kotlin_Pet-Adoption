package co.edu.uniquindio.petadoptionapp.domain.model.comment

import kotlinx.serialization.Serializable


/**
Modelo de dominio: comentario de una publicación, que puede tener respuestas en un solo nivel
([MAX_DEPTH]), como en Facebook: todas las respuestas de un hilo cuelgan del
comentario principal. Responder a una respuesta la deja en ese mismo hilo y
[replyToUserName] indica a quién se contesta ("@alias").

Se guarda en la subcolección `posts/{postId}/comments` de Firestore. Todos los
niveles viven en la MISMA subcolección; la jerarquía se arma con
[parentId]. Los datos del autor ([authorName], [authorPhotoUrl]) se
copian para dibujar la lista sin una lectura extra por comentario.
Alias y foto son públicos incluso en perfiles privados.
Lo usan CommentRepository y los casos de uso de usecase/comment.
**/
// Permite convertir el comentario a/desde el formato de almacenamiento.
@Serializable
// Declara el comentario como clase de datos inmutable.
data class Comment(
    // Identificador único del comentario.
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
        // Si está eliminado muestra el aviso fijo; si no, el texto original.
        if (status == CommentStatus.DELETED) DELETED_PLACEHOLDER else text

    // Devuelve una copia con el texto editado y marcada como editada.
    fun edit(newText: String): Comment = copy(
        // Valida y limpia el nuevo texto antes de guardarlo.
        text = validate(newText),
        // Marca el comentario como editado.
        edited = true,
        // Actualiza la fecha de modificación.
        updatedAt = System.currentTimeMillis()
    )

    // Devuelve una copia eliminada lógicamente, sin conservar el texto.
    fun markAsDeleted(): Comment = copy(
        // Cambia el estado a eliminado.
        status = CommentStatus.DELETED,
        // Borra el contenido para no conservar el texto original.
        text = "",
        // Actualiza la fecha de modificación.
        updatedAt = System.currentTimeMillis()
    )

    // Constantes y fábricas compartidas sin necesidad de instanciar la clase.
    companion object {
        // Longitud máxima del texto.
        const val MAX_LENGTH = 500

        // Niveles de respuesta: 1 = respuestas solo bajo el comentario principal (estilo Facebook).
        const val MAX_DEPTH = 1

        // Texto mostrado en lugar de un comentario eliminado.
        const val DELETED_PLACEHOLDER = "Comentario eliminado"

        // Valida el texto: no vacío y dentro del límite. Devuelve el texto limpio.
        private fun validate(text: String): String {
            // Quita los espacios al inicio y al final.
            val clean = text.trim()
            // Exige que el texto no quede vacío.
            require(clean.isNotEmpty()) { "El comentario no puede estar vacío" }
            // Exige que el texto no supere la longitud máxima.
            require(clean.length <= MAX_LENGTH) { "Máximo $MAX_LENGTH caracteres" }
            // Devuelve el texto ya limpio y validado.
            return clean
        }

        // Crea un comentario principal (nivel 0) sobre una publicación.
        fun createTopLevel(
            // Id asignado al nuevo comentario.
            id: String,
            // Publicación comentada.
            postId: String,
            // Id del autor.
            authorId: String,
            // Alias del autor.
            authorName: String,
            // Foto del autor (opcional).
            authorPhotoUrl: String?,
            // Texto escrito por el autor.
            text: String
        ): Comment = Comment(
            // Asigna el id recibido.
            id = id,
            // Asocia el comentario a la publicación.
            postId = postId,
            // Guarda el id del autor.
            authorId = authorId,
            // Copia el alias del autor.
            authorName = authorName,
            // Copia la foto del autor.
            authorPhotoUrl = authorPhotoUrl,
            // Valida y limpia el texto antes de crear el comentario.
            text = validate(text)
        )

        /**
         Crea una respuesta a [parent]. Si el padre ya es una respuesta (está en
         el último nivel, [MAX_DEPTH]), la nueva se "aplana": queda en el mismo
         hilo, bajo el comentario principal. En todas las respuestas [replyToUserName]
         indica a quién se contesta ("@alias: respuesta").
         */
        fun createReply(
            // Id asignado a la nueva respuesta.
            id: String,
            // Comentario al que se responde.
            parent: Comment,
            // Id del autor de la respuesta.
            authorId: String,
            // Alias del autor de la respuesta.
            authorName: String,
            // Foto del autor de la respuesta (opcional).
            authorPhotoUrl: String?,
            // Texto de la respuesta.
            text: String
        ): Comment {
            // Impide responder a un comentario eliminado.
            require(parent.canBeRepliedTo()) { "No se puede responder a un comentario eliminado" }
            // Determina si el padre ya está en el nivel máximo y hay que aplanar.
            val flatten = parent.depth >= MAX_DEPTH
            // Construye y devuelve la respuesta.
            return Comment(
                // Asigna el id recibido.
                id = id,
                // Hereda la publicación del comentario padre.
                postId = parent.postId,
                // Si se aplana, cuelga del mismo principal que el padre; si no, del propio padre.
                parentId = if (flatten) parent.parentId else parent.id,
                // Si se aplana, conserva el nivel del padre; si no, baja un nivel.
                depth = if (flatten) parent.depth else parent.depth + 1,
                // Guarda a quién se contesta para mostrar "@alias".
                replyToUserName = parent.authorName,
                // Guarda el id del autor.
                authorId = authorId,
                // Copia el alias del autor.
                authorName = authorName,
                // Copia la foto del autor.
                authorPhotoUrl = authorPhotoUrl,
                // Valida y limpia el texto de la respuesta.
                text = validate(text)
            )
        }
    }
}
