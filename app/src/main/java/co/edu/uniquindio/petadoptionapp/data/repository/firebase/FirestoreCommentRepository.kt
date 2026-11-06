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
 */
@Singleton
class FirestoreCommentRepository @Inject constructor(
    private val db: FirebaseFirestore,
) : CommentRepository {

    private fun comments(postId: String) = db.collection(POSTS).document(postId).collection(COMMENTS)

    override suspend fun create(comment: Comment): Result<Comment> = runCatching {
        val postRef = db.collection(POSTS).document(comment.postId)
        val ref = comments(comment.postId).document()
        val saved = comment.copy(id = ref.id)
        db.runTransaction { tx ->
            // La publicación debe existir; las lecturas van antes que las escrituras.
            if (!tx.get(postRef).exists()) throw PostException.PostNotFound
            val parentRef = saved.parentId?.let { comments(saved.postId).document(it) }
            val parentExists = parentRef?.let { tx.get(it).exists() } ?: false

            tx.set(ref, saved.toFirestore())
            tx.update(postRef, "commentCount", FieldValue.increment(1))
            // Si es respuesta, suma 1 al contador de respuestas del padre.
            if (parentRef != null && parentExists) tx.update(parentRef, "replyCount", FieldValue.increment(1))
        }.await()
        saved
    }.unwrapTransactionError()

    override suspend fun findById(postId: String, commentId: String): Comment? =
        runCatching { comments(postId).document(commentId).get().await().toModel<Comment>() }.getOrNull()

    override suspend fun getTopLevel(postId: String, limit: Int, afterCommentId: String?): Result<List<Comment>> = runCatching {
        // Principales: los más nuevos primero.
        val query = comments(postId)
            .whereEqualTo("parentId", null)
            .orderBy("createdAt", Query.Direction.DESCENDING)
        page(postId, query, limit, afterCommentId)
    }

    override suspend fun getReplies(
        postId: String,
        parentId: String,
        limit: Int,
        afterCommentId: String?
    ): Result<List<Comment>> = runCatching {
        // Respuestas: las más antiguas primero.
        val query = comments(postId)
            .whereEqualTo("parentId", parentId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
        page(postId, query, limit, afterCommentId)
    }

    // Paginación por cursor: empieza después del último comentario ya mostrado.
    private suspend fun page(postId: String, query: Query, limit: Int, afterCommentId: String?): List<Comment> {
        val cursor = afterCommentId?.let { id ->
            comments(postId).document(id).get().await().takeIf { it.exists() } ?: return emptyList()
        }
        return (cursor?.let { query.startAfter(it) } ?: query)
            .limit(limit.toLong())
            .get().await()
            .toModels()
    }

    override suspend fun updateText(postId: String, commentId: String, text: String): Result<Unit> = runCatching {
        val ref = comments(postId).document(commentId)
        if (!ref.get().await().exists()) throw CommentException.CommentNotFound
        // Solo toca el texto: no pisa likeCount ni replyCount.
        ref.update(mapOf("text" to text, "edited" to true, "updatedAt" to System.currentTimeMillis())).await()
        Unit
    }

    override suspend fun markDeleted(postId: String, commentId: String): Result<Unit> = runCatching {
        val ref = comments(postId).document(commentId)
        if (!ref.get().await().exists()) throw CommentException.CommentNotFound
        // Borrado lógico: se conserva el documento por las respuestas; los contadores no cambian.
        ref.update(mapOf(
            "status" to CommentStatus.DELETED.name,
            "text" to "",
            "updatedAt" to System.currentTimeMillis()
        )).await()
        Unit
    }

    override suspend fun toggleLike(postId: String, commentId: String, userId: String): Result<Boolean> = runCatching {
        val commentRef = comments(postId).document(commentId)
        val likeRef = db.collection(COMMENT_LIKES).document(CommentLike.buildId(commentId, userId))
        db.runTransaction { tx ->
            val comment = tx.get(commentRef).toModel<Comment>() ?: throw CommentException.CommentNotFound
            val alreadyLiked = tx.get(likeRef).exists()
            // Id fijo: un doble clic nunca crea dos likes.
            if (alreadyLiked) tx.delete(likeRef) else tx.set(likeRef, CommentLike(commentId, postId, userId).toFirestore())
            val newCount = (comment.likeCount + if (alreadyLiked) -1 else 1).coerceAtLeast(0)
            tx.update(commentRef, "likeCount", newCount)
            !alreadyLiked
        }.await()
    }.unwrapTransactionError()

    override suspend fun getLikedCommentIds(postId: String, userId: String): Result<Set<String>> = runCatching {
        db.collection(COMMENT_LIKES)
            .whereEqualTo("postId", postId)
            .whereEqualTo("userId", userId)
            .get().await()
            .toModels<CommentLike>()
            .map { it.commentId }
            .toSet()
    }
}
