package co.edu.uniquindio.petadoptionapp.data.repository.memory

import co.edu.uniquindio.petadoptionapp.domain.error.CommentException
import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.comment.Comment
import co.edu.uniquindio.petadoptionapp.domain.model.comment.CommentLike
import co.edu.uniquindio.petadoptionapp.domain.repository.CommentRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
Documenta la implementación en memoria del repositorio de comentarios.
Simula la subcolección `posts/{postId}/comments` y la colección `commentLikes`.
Mantiene los contadores del comentario padre y de la publicación, como lo haría la transacción de Firestore.
 */
@Singleton
class CommentRepositoryImpl @Inject constructor(
    // Inyecta el repositorio de publicaciones para actualizar su contador de comentarios.
    private val postStore: PostRepositoryImpl
) : CommentRepository {

    // Guarda los comentarios, con el id como clave (los ids son únicos entre publicaciones).
    private val comments = MutableStateFlow<Map<String, Comment>>(emptyMap())

    // Guarda los likes, con el id fijo "commentId_userId" como clave.
    private val likes = MutableStateFlow<Map<String, CommentLike>>(emptyMap())

    override suspend fun create(comment: Comment): Result<Comment> {
        // La publicación debe existir (en Firestore la transacción fallaría igual).
        if (!postStore.adjustCommentCount(comment.postId, +1))
            return Result.failure(PostException.PostNotFound)

        val saved = comment.copy(id = UUID.randomUUID().toString())
        comments.update { map ->
            var result = map + (saved.id to saved)
            // Si es respuesta, suma 1 al contador de respuestas del padre.
            saved.parentId?.let { parentId ->
                result[parentId]?.let { parent -> result = result + (parentId to parent.copy(replyCount = parent.replyCount + 1)) }
            }
            result
        }
        return Result.success(saved)
    }

    override suspend fun findById(postId: String, commentId: String): Comment? =
        comments.value[commentId]?.takeIf { it.postId == postId }

    override suspend fun getTopLevel(postId: String, limit: Int, afterCommentId: String?): Result<List<Comment>> {
        // Principales: los más nuevos primero.
        val list = comments.value.values
            .filter { it.postId == postId && it.parentId == null }
            .sortedByDescending { it.createdAt }
        return page(list, limit, afterCommentId)
    }

    override suspend fun getReplies(
        postId: String,
        parentId: String,
        limit: Int,
        afterCommentId: String?
    ): Result<List<Comment>> {
        // Respuestas: las más antiguas primero.
        val list = comments.value.values
            .filter { it.postId == postId && it.parentId == parentId }
            .sortedBy { it.createdAt }
        return page(list, limit, afterCommentId)
    }

    // Aplica la paginación por cursor sobre una lista ya ordenada.
    private fun page(list: List<Comment>, limit: Int, afterCommentId: String?): Result<List<Comment>> {
        val start = if (afterCommentId == null) 0 else {
            val index = list.indexOfFirst { it.id == afterCommentId }
            if (index < 0) return Result.success(emptyList()) else index + 1
        }
        return Result.success(list.drop(start).take(limit))
    }

    override suspend fun updateText(postId: String, commentId: String, text: String): Result<Unit> {
        if (findById(postId, commentId) == null) return Result.failure(CommentException.CommentNotFound)
        // Solo toca el texto: no pisa likeCount ni replyCount.
        comments.update { map ->
            map[commentId]?.let { map + (commentId to it.copy(text = text, edited = true, updatedAt = System.currentTimeMillis())) } ?: map
        }
        return Result.success(Unit)
    }

    override suspend fun markDeleted(postId: String, commentId: String): Result<Unit> {
        if (findById(postId, commentId) == null) return Result.failure(CommentException.CommentNotFound)
        // Borrado lógico: se conserva el documento por las respuestas; los contadores no cambian.
        comments.update { map ->
            map[commentId]?.let { map + (commentId to it.markAsDeleted()) } ?: map
        }
        return Result.success(Unit)
    }

    override suspend fun toggleLike(postId: String, commentId: String, userId: String): Result<Boolean> {
        if (findById(postId, commentId) == null) return Result.failure(CommentException.CommentNotFound)

        val likeId = CommentLike.buildId(commentId, userId)
        val alreadyLiked = likes.value.containsKey(likeId)

        // Crea o borra el like (id fijo: un doble clic nunca crea dos) y ajusta el contador.
        likes.update { map ->
            if (alreadyLiked) map - likeId else map + (likeId to CommentLike(commentId, postId, userId))
        }
        val delta = if (alreadyLiked) -1 else 1
        comments.update { map ->
            map[commentId]?.let { map + (commentId to it.copy(likeCount = (it.likeCount + delta).coerceAtLeast(0))) } ?: map
        }
        return Result.success(!alreadyLiked)
    }

    override suspend fun getLikedCommentIds(postId: String, userId: String): Result<Set<String>> =
        Result.success(
            likes.value.values.filter { it.postId == postId && it.userId == userId }.map { it.commentId }.toSet()
        )
}