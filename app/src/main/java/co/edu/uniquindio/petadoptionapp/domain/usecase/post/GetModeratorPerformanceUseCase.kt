package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.ModeratorPerformance
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase
import java.util.Calendar

/**
Documenta el caso de uso que arma el dashboard del moderador con sesión.
Lee una sola vez las publicaciones que revisó o tiene en revisión y calcula todo a partir de ellas.
"Hoy" y los últimos 7 días se cuentan por día de calendario del teléfono, con la fecha de la decisión.
 */
class GetModeratorPerformanceUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    suspend operator fun invoke(): Result<ModeratorPerformance> {
        val moderator = requireModerator().getOrElse { return Result.failure(it) }
        val posts = postRepository.getReviewedBy(moderator.id).getOrElse { return Result.failure(it) }

        val resolved = posts.filter {
            it.verification.status == VerificationStatus.VERIFIED || it.verification.status == VerificationStatus.REJECTED
        }
        val inReview = posts.filter { it.verification.status == VerificationStatus.IN_PROCESS }

        // Inicio de cada uno de los últimos 7 días (índice 6 = hoy).
        val dayStarts = (6 downTo 0).map { daysAgo ->
            Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        // Las decisiones sin fecha (datos viejos) cuentan en los totales, no en los días.
        val daily = dayStarts.mapIndexed { index, start ->
            val end = dayStarts.getOrNull(index + 1) ?: Long.MAX_VALUE
            resolved.count { (it.verification.reviewedAt ?: -1L) in start until end }
        }

        return Result.success(
            ModeratorPerformance(
                approved = resolved.count { it.verification.status == VerificationStatus.VERIFIED },
                rejected = resolved.count { it.verification.status == VerificationStatus.REJECTED },
                inReview = inReview.size,
                reviewedToday = daily.last(),
                reviewedLast7Days = daily.sum(),
                dailyReviews = daily,
                resolvedByCategory = resolved.groupingBy { it.categoryId }.eachCount().toList().sortedByDescending { it.second },
                inReviewByPriority = inReview.groupingBy { it.verification.priorityLevel }.eachCount(),
            )
        )
    }
}
