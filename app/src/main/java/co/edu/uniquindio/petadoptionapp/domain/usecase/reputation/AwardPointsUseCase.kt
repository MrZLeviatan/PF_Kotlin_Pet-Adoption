package co.edu.uniquindio.petadoptionapp.domain.usecase.reputation

import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.AwardResult
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointReason
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointRules
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointTransaction
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.UserLevel
import co.edu.uniquindio.petadoptionapp.domain.repository.ReputationRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.NotifyUseCase

/**
Caso de uso interno que otorga puntos por una acción.
Lo llaman los demás casos de uso (verificar, comentar, votar) después de guardar su acción principal.
Es de "mejor esfuerzo": si falla, la acción principal no se deshace.
Cada hecho da puntos una sola vez (id determinista) y los comentarios tienen un tope diario.
Guarda la transacción y suma los puntos en Firestore mediante ReputationRepository; si la persona sube de nivel, la notifica.
Lo llaman ApprovePost, FinishPost, CreateComment y ToggleImportant.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class AwardPointsUseCase(
    // Inyecta el repositorio de reputación.
    private val reputationRepository: ReputationRepository,
    // Inyecta el caso de uso que crea notificaciones.
    private val notify: NotifyUseCase
) {
    // Constantes del caso de uso.
    companion object {
        // Define la ventana del tope diario: 24 horas en milisegundos.
        const val DAY_MILLIS = 24 * 60 * 60 * 1000L
        // Define el resultado de "no se aplicó nada", para no repetir el valor.
        private val NOT_APPLIED = AwardResult(applied = false, previousPoints = 0, newPoints = 0)
    }

    // Define la función asíncrona que recibe el beneficiario, el motivo y el hecho que lo origina.
    suspend operator fun invoke(
        // Persona que recibe los puntos.
        userId: String,
        // Motivo del premio (define la cantidad de puntos).
        reason: PointReason,
        // Hecho que origina el premio (id de publicación, comentario o "postId_votanteId").
        sourceId: String,
        // Publicación relacionada, si aplica.
        postId: String? = null,
        // Persona que provocó el premio, si aplica.
        actorId: String? = null
    ): Result<AwardResult> {

        // Sin beneficiario o sin origen no hay nada que premiar.
        if (userId.isBlank() || sourceId.isBlank()) return Result.success(NOT_APPLIED)

        // Nadie se premia a sí mismo por una acción de otros (por ejemplo, votar la propia publicación).
        if (reason == PointReason.IMPORTANT_RECEIVED && actorId == userId) return Result.success(NOT_APPLIED)

        // Tope diario de comentarios que dan puntos.
        if (reason == PointReason.COMMENT_CREATED) {
            // Calcula el inicio de la ventana de 24 horas.
            val since = System.currentTimeMillis() - DAY_MILLIS
            // Cuenta los comentarios premiados en esa ventana.
            val today = reputationRepository.countByReason(userId, reason, since)
                // Si el conteo falla, devuelve el error.
                .getOrElse { return Result.failure(it) }
            // Si ya alcanzó el tope, no premia este comentario.
            if (today >= PointRules.MAX_REWARDED_COMMENTS_PER_DAY) return Result.success(NOT_APPLIED)
        }

        // Guarda el registro y suma los puntos (una sola vez por hecho).
        val transaction = PointTransaction(
            // Id determinista: el mismo hecho nunca suma dos veces.
            id = PointTransaction.buildId(userId, reason, sourceId),
            // Beneficiario.
            userId = userId,
            // Motivo.
            reason = reason,
            // Puntos según el motivo (definidos en PointRules).
            points = reason.points,
            // Publicación relacionada.
            postId = postId,
            // Actor que provocó el premio.
            actorId = actorId
        )
        // Otorga los puntos en una transacción y revisa el resultado.
        return reputationRepository.award(transaction).onSuccess { result ->
            // Si el premio hizo subir de nivel, avisa a la propia persona.
            if (result.leveledUp()) {
                // Notifica el nuevo nivel; su nombre va en `extra`.
                notify(userId, NotificationType.LEVEL_UP, extra = UserLevel.fromPoints(result.newPoints).name)
            }
        }
    }
}
