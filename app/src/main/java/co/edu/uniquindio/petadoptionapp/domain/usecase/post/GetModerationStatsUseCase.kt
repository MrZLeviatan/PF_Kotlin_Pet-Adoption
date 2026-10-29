package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.ModerationStats
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase
import java.util.Calendar

/**
Documenta el caso de uso que calcula el dashboard de moderación.
Prioridad alta, en revisión y pendientes son de toda la aplicación; "revisadas hoy" es del moderador con sesión.
Usa conteos en lugar de descargar documentos para cuidar la cuota gratuita.
 */
class GetModerationStatsUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    suspend operator fun invoke(): Result<ModerationStats> {
        val moderator = requireModerator().getOrElse { return Result.failure(it) }
        val unresolved = setOf(VerificationStatus.UNVERIFIED, VerificationStatus.IN_PROCESS)

        // "Hoy" según el calendario del teléfono: desde las 00:00.
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return Result.success(
            ModerationStats(
                highPriority = postRepository.countForModeration(unresolved, priorities = setOf(PriorityLevel.HIGH, PriorityLevel.URGENT)),
                inReview = postRepository.countForModeration(setOf(VerificationStatus.IN_PROCESS)),
                reviewedToday = postRepository.countForModeration(
                    setOf(VerificationStatus.VERIFIED, VerificationStatus.REJECTED),
                    reviewedBy = moderator.id,
                    reviewedSince = startOfToday
                ),
                pending = postRepository.countForModeration(setOf(VerificationStatus.UNVERIFIED))
            )
        )
    }
}
