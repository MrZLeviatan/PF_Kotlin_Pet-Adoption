package co.edu.uniquindio.petadoptionapp.domain.usecase.reputation

import co.edu.uniquindio.petadoptionapp.domain.model.reputation.AwardResult
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointReason
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointRules
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointTransaction
import co.edu.uniquindio.petadoptionapp.domain.repository.ReputationRepository

/**
Documenta el caso de uso interno que otorga puntos por una acción.
Lo llaman los demás casos de uso (verificar, comentar, votar) después de guardar su acción principal.
Es de "mejor esfuerzo": si falla, la acción principal no se deshace.
Cada hecho da puntos una sola vez (id determinista) y los comentarios tienen un tope diario.
 */
class AwardPointsUseCase(
    // Inyecta el repositorio de reputación.
    private val reputationRepository: ReputationRepository
) {
    companion object {
        // Define la ventana del tope diario: 24 horas en milisegundos.
        const val DAY_MILLIS = 24 * 60 * 60 * 1000L
        // Define el resultado de "no se aplicó nada", para no repetir el valor.
        private val NOT_APPLIED = AwardResult(applied = false, previousPoints = 0, newPoints = 0)
    }

    // Define la función asíncrona que recibe el beneficiario, el motivo y el hecho que lo origina.
    suspend operator fun invoke(
        userId: String,
        reason: PointReason,
        sourceId: String,
        postId: String? = null,
        actorId: String? = null
    ): Result<AwardResult> {

        // Sin beneficiario o sin origen no hay nada que premiar.
        if (userId.isBlank() || sourceId.isBlank()) return Result.success(NOT_APPLIED)

        // Nadie se premia a sí mismo por una acción de otros (por ejemplo, votar la propia publicación).
        if (reason == PointReason.IMPORTANT_RECEIVED && actorId == userId) return Result.success(NOT_APPLIED)

        // Tope diario de comentarios que dan puntos.
        if (reason == PointReason.COMMENT_CREATED) {
            val since = System.currentTimeMillis() - DAY_MILLIS
            val today = reputationRepository.countByReason(userId, reason, since)
                .getOrElse { return Result.failure(it) }
            if (today >= PointRules.MAX_REWARDED_COMMENTS_PER_DAY) return Result.success(NOT_APPLIED)
        }

        // Guarda el registro y suma los puntos (una sola vez por hecho).
        val transaction = PointTransaction(
            id = PointTransaction.buildId(userId, reason, sourceId),
            userId = userId,
            reason = reason,
            points = reason.points,
            postId = postId,
            actorId = actorId
        )
        return reputationRepository.award(transaction)
        // Pendiente: si result.leveledUp(), NotifyUseCase LEVEL_UP a la persona.
    }
}