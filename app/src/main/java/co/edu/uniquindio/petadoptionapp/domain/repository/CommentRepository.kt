package co.edu.uniquindio.petadoptionapp.domain.repository

import co.edu.uniquindio.petadoptionapp.domain.model.comment.Comment

/**
Contrato de comentarios guardados en la subcolección `posts/{postId}/comments` de Firestore.
Las validaciones, los permisos y la visibilidad de la publicación viven en los casos de uso, aquí solo se ejecuta.
 */
interface CommentRepository {

    // Crear y consultar

    /**
    Guarda un comentario o una respuesta.

    - Qué envía ([comment]): El comentario ya validado, con el `id` vacío.
    - Qué recibe: Un [Result] con el comentario guardado, ya con el `id` que generó Firestore.
    En una sola transacción: crea el comentario, suma 1 a `commentCount` del post y, si es respuesta, suma 1 a `replyCount` del comentario padre.
     */
    suspend fun create(comment: Comment): Result<Comment>

    /**
    Busca un comentario.

    - Qué envía: El [postId] y el [commentId].
    - Qué recibe: El [Comment] o `null` si no existe.
     */
    suspend fun findById(postId: String, commentId: String): Comment?

    /**
    Obtiene una página de comentarios principales (sin padre), de los más nuevos a los más antiguos.

    - Qué envía: El [postId], el [limit] de resultados y, opcionalmente, el [afterCommentId] del último ya mostrado.
    - Qué recibe: Un [Result] con la lista de comentarios.
     */
    suspend fun getTopLevel(postId: String, limit: Int, afterCommentId: String? = null): Result<List<Comment>>

    /**
    Obtiene una página de respuestas de un comentario, de las más antiguas a las más nuevas.

    - Qué envía: El [postId], el [parentId], el [limit] y, opcionalmente, el [afterCommentId].
    - Qué recibe: Un [Result] con la lista de respuestas.
    Se piden bajo demanda (cuando la persona pulsa "Ver respuestas") para gastar pocas lecturas.
     */
    suspend fun getReplies(
        postId: String,
        parentId: String,
        limit: Int,
        afterCommentId: String? = null
    ): Result<List<Comment>>


    // Modificar

    /**
    Cambia el texto de un comentario y lo marca como editado.

    - Qué envía: El [postId], el [commentId] y el nuevo [text] ya validado.
    - Qué recibe: Un [Result<Unit>] que confirma si se guardó.
    Solo toca el texto: no pisa `likeCount` ni `replyCount`.
     */
    suspend fun updateText(postId: String, commentId: String, text: String): Result<Unit>

    /**
    Marca un comentario como eliminado (borrado lógico) y vacía su texto.

    - Qué envía: El [postId] y el [commentId].
    - Qué recibe: Un [Result<Unit>] que confirma si se aplicó.
    El documento se conserva porque puede tener respuestas colgando. No cambia los contadores.
     */
    suspend fun markDeleted(postId: String, commentId: String): Result<Unit>


    // Likes

    /**
    Alterna el "me gusta" de una persona sobre un comentario.

    - Qué envía: El [postId], el [commentId] y el [userId].
    - Qué recibe: Un [Result] con `true` si quedó con like, o `false` si se quitó.
    En una transacción: crea o borra el documento `CommentLike` (id fijo `commentId_userId`) y ajusta `likeCount`.
     */
    suspend fun toggleLike(postId: String, commentId: String, userId: String): Result<Boolean>

    /**
    Obtiene los comentarios de una publicación a los que la persona ya dio like.

    - Qué envía: El [postId] y el [userId].
    - Qué recibe: Un [Result] con el conjunto de ids de comentarios.
    Una sola consulta permite pintar todos los corazones de la pantalla.
     */
    suspend fun getLikedCommentIds(postId: String, userId: String): Result<Set<String>>
}