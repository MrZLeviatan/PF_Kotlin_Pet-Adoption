package co.edu.uniquindio.petadoptionapp.data.repository.firebase

import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.COMMENTS
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.COMMENT_LIKES
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.POSTS
import co.edu.uniquindio.petadoptionapp.domain.error.CommentException
import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.comment.Comment
import co.edu.uniquindio.petadoptionapp.domain.model.comment.CommentLike
import co.edu.uniquindio.petadoptionapp.domain.model.comment.CommentStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.CommentRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/**
Comentarios en la subcolección `posts/{postId}/comments` y sus "me gusta" en `commentLikes/{commentId_userId}`.
Los contadores (`commentCount`, `replyCount`, `likeCount`) se ajustan en la misma transacción que el cambio.

Implementa CommentRepository del dominio y se enlaza en RepositoryModule. Lo usan los casos de
uso de comentarios (crear, responder, editar, borrar, dar "me gusta") que consumen las pantallas
de detalle y de comentarios. Los comentarios principales y las respuestas se paginan por cursor.
**/
// Una sola instancia compartida en toda la app.
@Singleton
// Hilt inyecta Firestore desde FirebaseModule.
class FirestoreCommentRepository @Inject constructor(
    // Base de datos Firestore.
    private val db: FirebaseFirestore,
) : CommentRepository {

    // Devuelve la subcolección de comentarios de una publicación concreta.
    private fun comments(postId: String) = db.collection(POSTS).document(postId).collection(COMMENTS)

    // Crea un comentario (o una respuesta) y actualiza los contadores; devuelve el comentario con su id.
    override suspend fun create(comment: Comment): Result<Comment> = runCatching {
        // Referencia a la publicación comentada.
        val postRef = db.collection(POSTS).document(comment.postId)
        // Nueva referencia con id automático dentro de la subcolección de comentarios.
        val ref = comments(comment.postId).document()
        // Copia del comentario con el id generado, que es lo que se guarda y se devuelve.
        val saved = comment.copy(id = ref.id)
        // Transacción: comentario y contadores cambian juntos.
        db.runTransaction { tx ->
            // La publicación debe existir; las lecturas van antes que las escrituras.
            if (!tx.get(postRef).exists()) throw PostException.PostNotFound
            // Si es una respuesta, obtiene la referencia del comentario padre.
            val parentRef = saved.parentId?.let { comments(saved.postId).document(it) }
            // Comprueba si el padre existe (false si no es respuesta).
            val parentExists = parentRef?.let { tx.get(it).exists() } ?: false

            // Guarda el comentario.
            tx.set(ref, saved.toFirestore())
            // Suma 1 al contador de comentarios de la publicación de forma atómica.
            tx.update(postRef, "commentCount", FieldValue.increment(1))
            // Si es respuesta, suma 1 al contador de respuestas del padre.
            if (parentRef != null && parentExists) tx.update(parentRef, "replyCount", FieldValue.increment(1))
        // Espera a que la transacción termine.
        }.await()
        // Devuelve el comentario guardado.
        saved
    // Extrae el error de dominio original si la transacción lo envolvió.
    }.unwrapTransactionError()

    // Busca un comentario por id; null si no existe o falla la lectura.
    override suspend fun findById(postId: String, commentId: String): Comment? =
        runCatching { comments(postId).document(commentId).get().await().toModel<Comment>() }.getOrNull()

    // Página de comentarios principales (los que no son respuesta).
    override suspend fun getTopLevel(postId: String, limit: Int, afterCommentId: String?): Result<List<Comment>> = runCatching {
        // Principales: los más nuevos primero.
        val query = comments(postId)
            // Solo los que no tienen padre.
            .whereEqualTo("parentId", null)
            // Del más reciente al más antiguo.
            .orderBy("createdAt", Query.Direction.DESCENDING)
        // Aplica la paginación por cursor.
        page(postId, query, limit, afterCommentId)
    }

    // Página de respuestas de un comentario.
    override suspend fun getReplies(
        // Publicación del comentario.
        postId: String,
        // Comentario padre.
        parentId: String,
        // Tamaño de la página.
        limit: Int,
        // Última respuesta mostrada (null para la primera página).
        afterCommentId: String?
    ): Result<List<Comment>> = runCatching {
        // Respuestas: las más antiguas primero.
        val query = comments(postId)
            // Solo las respuestas del comentario padre.
            .whereEqualTo("parentId", parentId)
            // En orden cronológico, para leerse como conversación.
            .orderBy("createdAt", Query.Direction.ASCENDING)
        // Aplica la paginación por cursor.
        page(postId, query, limit, afterCommentId)
    }

    // Paginación por cursor: empieza después del último comentario ya mostrado.
    private suspend fun page(postId: String, query: Query, limit: Int, afterCommentId: String?): List<Comment> {
        // Lee el documento cursor; si ya no existe, no hay más resultados.
        val cursor = afterCommentId?.let { id ->
            comments(postId).document(id).get().await().takeIf { it.exists() } ?: return emptyList()
        }
        // Empieza después del cursor si existe; si no, desde el principio.
        return (cursor?.let { query.startAfter(it) } ?: query)
            // Limita el tamaño de la página.
            .limit(limit.toLong())
            // Ejecuta la consulta.
            .get().await()
            // Convierte los documentos en comentarios.
            .toModels()
    }

    // Edita el texto de un comentario.
    override suspend fun updateText(postId: String, commentId: String, text: String): Result<Unit> = runCatching {
        // Referencia al comentario.
        val ref = comments(postId).document(commentId)
        // Si no existe, informa el error del dominio.
        if (!ref.get().await().exists()) throw CommentException.CommentNotFound
        // Solo toca el texto: no pisa likeCount ni replyCount. Marca el comentario como editado y guarda la fecha.
        ref.update(mapOf("text" to text, "edited" to true, "updatedAt" to System.currentTimeMillis())).await()
        // Resultado exitoso.
        Unit
    }

    // Marca un comentario como eliminado.
    override suspend fun markDeleted(postId: String, commentId: String): Result<Unit> = runCatching {
        // Referencia al comentario.
        val ref = comments(postId).document(commentId)
        // Si no existe, informa el error del dominio.
        if (!ref.get().await().exists()) throw CommentException.CommentNotFound
        // Borrado lógico: se conserva el documento por las respuestas; los contadores no cambian.
        ref.update(mapOf(
            // Cambia el estado a eliminado.
            "status" to CommentStatus.DELETED.name,
            // Vacía el texto para no conservar el contenido borrado.
            "text" to "",
            // Registra el momento del borrado.
            "updatedAt" to System.currentTimeMillis()
        )).await()
        // Resultado exitoso.
        Unit
    }

    // Da o quita "me gusta" a un comentario; devuelve true si quedó con "me gusta".
    override suspend fun toggleLike(postId: String, commentId: String, userId: String): Result<Boolean> = runCatching {
        // Referencia al comentario.
        val commentRef = comments(postId).document(commentId)
        // Referencia al "me gusta" con id determinista commentId_userId.
        val likeRef = db.collection(COMMENT_LIKES).document(CommentLike.buildId(commentId, userId))
        // Transacción: el "me gusta" y el contador cambian juntos.
        db.runTransaction { tx ->
            // Lee el comentario; si no existe, falla.
            val comment = tx.get(commentRef).toModel<Comment>() ?: throw CommentException.CommentNotFound
            // Revisa si ya tenía "me gusta" del usuario.
            val alreadyLiked = tx.get(likeRef).exists()
            // Id fijo: un doble clic nunca crea dos likes. Si ya existía se borra; si no, se crea.
            if (alreadyLiked) tx.delete(likeRef) else tx.set(likeRef, CommentLike(commentId, postId, userId).toFirestore())
            // Calcula el nuevo contador sin permitir negativos.
            val newCount = (comment.likeCount + if (alreadyLiked) -1 else 1).coerceAtLeast(0)
            // Guarda el nuevo contador en el comentario.
            tx.update(commentRef, "likeCount", newCount)
            // Devuelve el nuevo estado.
            !alreadyLiked
        // Espera a que la transacción termine.
        }.await()
    // Extrae el error de dominio original si la transacción lo envolvió.
    }.unwrapTransactionError()

    // Ids de los comentarios de una publicación a los que el usuario dio "me gusta" (para pintar los corazones).
    override suspend fun getLikedCommentIds(postId: String, userId: String): Result<Set<String>> = runCatching {
        // Consulta la colección de "me gusta".
        db.collection(COMMENT_LIKES)
            // De esta publicación.
            .whereEqualTo("postId", postId)
            // Del usuario actual.
            .whereEqualTo("userId", userId)
            // Ejecuta la consulta.
            .get().await()
            // Convierte los documentos en modelos.
            .toModels<CommentLike>()
            // Se queda con el id del comentario.
            .map { it.commentId }
            // Lo entrega como conjunto para búsquedas rápidas.
            .toSet()
    }
}
