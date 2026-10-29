package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.ModerationStats
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase

/**
Documenta el caso de uso que calcula los números del inicio del moderador.
Cuenta las publicaciones de toda la aplicación que ningún moderador ha tomado: por prioridad y en total.
Usa conteos en lugar de descargar documentos para cuidar la cuota gratuita.
 */
class GetModerationStatsUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    suspend operator fun invoke(): Result<ModerationStats> {
        requireModerator().getOrElse { return Result.failure(it) }
        // Sin moderador = sin verificar (las "en revisión" ya tienen dueño).
        val unassigned = setOf(VerificationStatus.UNVERIFIED)

        return Result.success(
            ModerationStats(
                highPriority = postRepository.countForModeration(unassigned, priorities = setOf(PriorityLevel.HIGH, PriorityLevel.URGENT)),
                mediumPriority = postRepository.countForModeration(unassigned, priorities = setOf(PriorityLevel.MEDIUM)),
                lowPriority = postRepository.countForModeration(unassigned, priorities = setOf(PriorityLevel.LOW)),
                pending = postRepository.countForModeration(unassigned)
            )
        )
    }
}
