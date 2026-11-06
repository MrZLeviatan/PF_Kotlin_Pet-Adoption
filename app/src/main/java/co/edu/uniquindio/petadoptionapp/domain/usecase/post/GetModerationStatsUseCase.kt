package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.ModerationStats
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase

/**
Caso de uso que calcula los números del inicio del moderador.
Cuenta las publicaciones de toda la aplicación que ningún moderador ha tomado: por prioridad y en total.
Usa conteos en lugar de descargar documentos para cuidar la cuota gratuita.
Lo usa el ViewModel del inicio (panel) del moderador.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class GetModerationStatsUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    // Función principal: devuelve las estadísticas de la cola de moderación.
    suspend operator fun invoke(): Result<ModerationStats> {
        // Solo un moderador activo puede consultarlas; si no lo es, devuelve el error.
        requireModerator().getOrElse { return Result.failure(it) }
        // Sin moderador = sin verificar (las "en revisión" ya tienen dueño).
        val unassigned = setOf(VerificationStatus.UNVERIFIED)

        // Devuelve con éxito las estadísticas calculadas con conteos.
        return Result.success(
            // Construye el modelo con cada conteo.
            ModerationStats(
                // Sin moderador con prioridad alta o urgente.
                highPriority = postRepository.countForModeration(unassigned, priorities = setOf(PriorityLevel.HIGH, PriorityLevel.URGENT)),
                // Sin moderador con prioridad media.
                mediumPriority = postRepository.countForModeration(unassigned, priorities = setOf(PriorityLevel.MEDIUM)),
                // Sin moderador con prioridad baja.
                lowPriority = postRepository.countForModeration(unassigned, priorities = setOf(PriorityLevel.LOW)),
                // Total sin moderador (incluye las que aún no tienen prioridad).
                pending = postRepository.countForModeration(unassigned)
            )
        )
    }
}
