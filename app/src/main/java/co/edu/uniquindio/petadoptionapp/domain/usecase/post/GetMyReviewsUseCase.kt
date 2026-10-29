package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase

/**
Documenta el caso de uso que lista las publicaciones que el moderador con sesión tomó para revisar
y aún no decide (en revisión por él; no las verificadas ni las rechazadas).
Las ordena como la cola: urgentes primero y, a igual prioridad, las más antiguas primero.
 */
class GetMyReviewsUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    suspend operator fun invoke(): Result<List<Post>> {
        val moderator = requireModerator().getOrElse { return Result.failure(it) }
        return postRepository.getInReviewBy(moderator.id).map { posts ->
            posts.sortedWith(
                compareByDescending<Post> { it.verification.priorityLevel?.ordinal ?: -1 }
                    .thenBy { it.createdAt }
            )
        }
    }
}
