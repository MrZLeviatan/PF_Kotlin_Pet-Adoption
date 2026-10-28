package co.edu.uniquindio.petadoptionapp.data.repository.memory

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.post.ImportantVote
import co.edu.uniquindio.petadoptionapp.domain.repository.ImportantVoteRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
Documenta la implementación en memoria del repositorio de votos "Es importante".
Simula la colección `importantVotes` con id fijo "postId_userId" y mantiene el contador de la publicación.
 */
@Singleton
class ImportantVoteRepositoryImpl @Inject constructor(
    // Inyecta el repositorio de publicaciones para actualizar `importantCount`.
    private val postStore: PostRepositoryImpl
) : ImportantVoteRepository {

    // Guarda los votos, con el id fijo como clave: un voto por persona y publicación.
    private val votes = MutableStateFlow<Map<String, ImportantVote>>(emptyMap())

    override suspend fun toggle(postId: String, userId: String): Result<Boolean> {
        val voteId = ImportantVote.buildId(postId, userId)
        val alreadyVoted = votes.value.containsKey(voteId)
        val delta = if (alreadyVoted) -1 else 1

        // Primero el contador: si la publicación no existe, no se guarda ningún voto.
        if (!postStore.adjustImportantCount(postId, delta)) return Result.failure(PostException.PostNotFound)

        votes.update { map ->
            if (alreadyVoted) map - voteId else map + (voteId to ImportantVote(postId, userId))
        }
        return Result.success(!alreadyVoted)
    }

    override suspend fun hasVoted(postId: String, userId: String): Result<Boolean> =
        Result.success(votes.value.containsKey(ImportantVote.buildId(postId, userId)))

    override suspend fun getVotedAmong(userId: String, postIds: List<String>): Result<Set<String>> =
        Result.success(
            votes.value.values.filter { it.userId == userId && it.postId in postIds }.map { it.postId }.toSet()
        )

    override suspend fun getVotedPostIds(userId: String, limit: Int, afterPostId: String?): Result<List<String>> {
        // De la más reciente a la más antigua.
        val ids = votes.value.values
            .filter { it.userId == userId }
            .sortedByDescending { it.createdAt }
            .map { it.postId }
        val start = if (afterPostId == null) 0 else {
            val index = ids.indexOf(afterPostId)
            if (index < 0) return Result.success(emptyList()) else index + 1
        }
        return Result.success(ids.drop(start).take(limit))
    }
}