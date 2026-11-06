package co.edu.uniquindio.petadoptionapp.data.repository.firebase

import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.IMPORTANT_VOTES
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.MAX_IN_VALUES
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.POSTS
import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.post.ImportantVote
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.repository.ImportantVoteRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/**
Votos "Es importante" en `importantVotes/{postId_userId}`; el contador vive en `importantCount` del post.
 */
@Singleton
class FirestoreImportantVoteRepository @Inject constructor(
    private val db: FirebaseFirestore,
) : ImportantVoteRepository {

    private val votes = db.collection(IMPORTANT_VOTES)

    override suspend fun toggle(postId: String, userId: String): Result<Boolean> = runCatching {
        val postRef = db.collection(POSTS).document(postId)
        val voteRef = votes.document(ImportantVote.buildId(postId, userId))
        db.runTransaction { tx ->
            // Si la publicación no existe, no se guarda ningún voto.
            val post = tx.get(postRef).toModel<Post>() ?: throw PostException.PostNotFound
            val alreadyVoted = tx.get(voteRef).exists()
            if (alreadyVoted) tx.delete(voteRef) else tx.set(voteRef, ImportantVote(postId, userId).toFirestore())
            val newCount = (post.importantCount + if (alreadyVoted) -1 else 1).coerceAtLeast(0)
            tx.update(postRef, "importantCount", newCount)
            !alreadyVoted
        }.await()
    }.unwrapTransactionError()

    override suspend fun hasVoted(postId: String, userId: String): Result<Boolean> = runCatching {
        votes.document(ImportantVote.buildId(postId, userId)).get().await().exists()
    }

    override suspend fun getVotedAmong(userId: String, postIds: List<String>): Result<Set<String>> = runCatching {
        postIds.distinct().chunked(MAX_IN_VALUES).flatMap { chunk ->
            votes.whereEqualTo("userId", userId)
                .whereIn("postId", chunk)
                .get().await()
                .toModels<ImportantVote>()
                .map { it.postId }
        }.toSet()
    }

    override suspend fun getVotedPostIds(userId: String, limit: Int, afterPostId: String?): Result<List<String>> = runCatching {
        // De la más reciente a la más antigua.
        val query = votes.whereEqualTo("userId", userId).orderBy("createdAt", Query.Direction.DESCENDING)
        val cursor = afterPostId?.let { id ->
            votes.document(ImportantVote.buildId(id, userId)).get().await().takeIf { it.exists() }
                ?: return@runCatching emptyList()
        }
        (cursor?.let { query.startAfter(it) } ?: query)
            .limit(limit.toLong())
            .get().await()
            .toModels<ImportantVote>()
            .map { it.postId }
    }
}
