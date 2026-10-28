package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStats
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

/**
Documenta el caso de uso que calcula las métricas del dashboard personal.
Usa conteos en lugar de descargar documentos para cuidar la cuota gratuita.
 */
class GetPostStatsUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    private val requireUser: RequireUserUseCase
) {
    suspend operator fun invoke(): Result<PostStats> {
        val session = requireUser().getOrElse { return Result.failure(it) }
        val id = session.id

        // Activas: en proceso y ya verificadas.
        val active = postRepository.countByAuthor(id, PostStatus.IN_PROGRESS, VerificationStatus.VERIFIED)
        // Finalizadas.
        val finished = postRepository.countByAuthor(id, PostStatus.FINISHED)
        // Pendientes: sin verificar o en revisión.
        val pending =
            postRepository.countByAuthor(id, PostStatus.IN_PROGRESS, VerificationStatus.UNVERIFIED) +
                    postRepository.countByAuthor(id, PostStatus.IN_PROGRESS, VerificationStatus.IN_PROCESS)

        return Result.success(PostStats(active, finished, pending))
    }
}