package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStats
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository

/**
Documenta el caso de uso que calcula las métricas del dashboard personal.
Usa conteos en lugar de descargar documentos para cuidar la cuota gratuita.
 */
class GetPostStatsUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository
) {
    // Define la función asíncrona que recibe el identificador del autor y devuelve las estadísticas.
    suspend operator fun invoke(authorId: String): PostStats {

        // Activas: en proceso y ya verificadas.
        val active = postRepository.countByAuthor(authorId, PostStatus.IN_PROGRESS, VerificationStatus.VERIFIED)

        // Finalizadas.
        val finished = postRepository.countByAuthor(authorId, PostStatus.FINISHED)

        // Pendientes: en proceso y sin verificar, o en revisión.
        val pending =
            postRepository.countByAuthor(authorId, PostStatus.IN_PROGRESS, VerificationStatus.UNVERIFIED) +
                    postRepository.countByAuthor(authorId, PostStatus.IN_PROGRESS, VerificationStatus.IN_PROCESS)

        return PostStats(active, finished, pending)
    }
}