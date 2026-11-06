package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.ModeratorPerformance
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase
import java.util.Calendar

/**
Caso de uso que arma el dashboard del moderador con sesión.
Lee una sola vez las publicaciones que revisó o tiene en revisión y calcula todo a partir de ellas.
"Hoy" y los últimos 7 días se cuentan por día de calendario del teléfono, con la fecha de la decisión.
Lo usa el ViewModel del dashboard de desempeño del moderador.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class GetModeratorPerformanceUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    // Función principal: calcula el desempeño del moderador con sesión.
    suspend operator fun invoke(): Result<ModeratorPerformance> {
        // Exige un moderador activo; si no lo es, devuelve el error.
        val moderator = requireModerator().getOrElse { return Result.failure(it) }
        // Lee todas las publicaciones que revisó o tiene en revisión; si falla, devuelve el error.
        val posts = postRepository.getReviewedBy(moderator.id).getOrElse { return Result.failure(it) }

        // Separa las ya decididas (verificadas o rechazadas).
        val resolved = posts.filter {
            // Conserva las que tienen decisión final.
            it.verification.status == VerificationStatus.VERIFIED || it.verification.status == VerificationStatus.REJECTED
        }
        // Separa las que todavía están en revisión.
        val inReview = posts.filter { it.verification.status == VerificationStatus.IN_PROCESS }

        // Inicio de cada uno de los últimos 7 días (índice 6 = hoy).
        val dayStarts = (6 downTo 0).map { daysAgo ->
            // Parte de la fecha y hora actuales del teléfono.
            Calendar.getInstance().apply {
                // Retrocede la cantidad de días indicada.
                add(Calendar.DAY_OF_YEAR, -daysAgo)
                // Lleva la hora a la medianoche (00:00:00.000) para obtener el inicio del día.
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            // Convierte el inicio del día a milisegundos (epoch).
            }.timeInMillis
        }
        // Las decisiones sin fecha (datos viejos) cuentan en los totales, no en los días.
        val daily = dayStarts.mapIndexed { index, start ->
            // El día termina donde empieza el siguiente (hoy no tiene límite superior).
            val end = dayStarts.getOrNull(index + 1) ?: Long.MAX_VALUE
            // Cuenta las decisiones cuya fecha cae dentro del día.
            resolved.count { (it.verification.reviewedAt ?: -1L) in start until end }
        }

        // Devuelve con éxito el desempeño calculado.
        return Result.success(
            // Construye el modelo del dashboard.
            ModeratorPerformance(
                // Cantidad de aprobadas.
                approved = resolved.count { it.verification.status == VerificationStatus.VERIFIED },
                // Cantidad de rechazadas.
                rejected = resolved.count { it.verification.status == VerificationStatus.REJECTED },
                // Cantidad en revisión.
                inReview = inReview.size,
                // Decisiones de hoy (último elemento de la lista diaria).
                reviewedToday = daily.last(),
                // Decisiones de los últimos 7 días.
                reviewedLast7Days = daily.sum(),
                // Detalle por día.
                dailyReviews = daily,
                // Decisiones agrupadas por categoría, de la que más a la que menos.
                resolvedByCategory = resolved.groupingBy { it.categoryId }.eachCount().toList().sortedByDescending { it.second },
                // En revisión agrupadas por prioridad de la IA.
                inReviewByPriority = inReview.groupingBy { it.verification.priorityLevel }.eachCount(),
            )
        )
    }
}
