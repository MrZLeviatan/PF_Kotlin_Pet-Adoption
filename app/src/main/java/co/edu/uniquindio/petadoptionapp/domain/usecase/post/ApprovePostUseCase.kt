package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointReason
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.NotifyUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.AwardPointsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase

/**
Caso de uso con el que un moderador aprueba una publicación (aparece en el feed).
El moderador se toma de la sesión actual, nunca de un parámetro.
Notifica al autor y le otorga puntos por la verificación. Lo usa el ViewModel de la pantalla de revisión del moderador.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class ApprovePostUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase,
    // Inyecta el caso de uso que crea notificaciones.
    private val notify: NotifyUseCase,
    // Inyecta el caso de uso que otorga puntos (también avisa si sube de nivel).
    private val awardPoints: AwardPointsUseCase

) {
    // Define la función asíncrona principal que recibe el ID de la publicación y devuelve un resultado vacío.
    suspend operator fun invoke(postId: String): Result<Unit> {

        // Obtiene al moderador de la sesión actual; falla si no hay sesión o no es un moderador activo.
        val moderator = requireModerator().getOrElse { return Result.failure(it) }

        // Busca la publicación y retorna un fallo si no existe.
        val post = postRepository.findById(postId)
            // Si no existe, falla con PostNotFound.
            ?: return Result.failure(PostException.PostNotFound)

        // Solo se aprueba mientras la publicación siga en proceso.
        if (post.status != PostStatus.IN_PROGRESS) return Result.failure(PostException.PostNotActive)

        // Si ya fue verificada o rechazada, no se vuelve a revisar.
        val current = post.verification.status
        // Comprueba si ya tiene una decisión final.
        if (current == VerificationStatus.VERIFIED || current == VerificationStatus.REJECTED)
            // Falla con AlreadyReviewed.
            return Result.failure(PostException.AlreadyReviewed)

        // Si otro moderador la tiene en revisión, solo ese moderador puede aprobarla.
        // Comprueba si está en revisión por otro moderador.
        if (current == VerificationStatus.IN_PROCESS && post.verification.reviewedBy != moderator.id)
            // Falla con ReviewedByAnotherModerator.
            return Result.failure(PostException.ReviewedByAnotherModerator)

        // Aprueba solo si el estado sigue siendo el que se leyó (la transacción evita pisar a otro moderador).
        return postRepository.updateVerification(postId, current, post.verification.approve(moderator.id))
            // Si se guardó, avisa y premia al autor (mejor esfuerzo).
            .onSuccess {
                // Notifica al autor que su publicación fue verificada.
                notify(post.authorId, NotificationType.POST_VERIFIED, moderator.id, moderator.userName, postId)
                // Otorga puntos al autor; el id de la publicación evita premiarla dos veces.
                awardPoints(post.authorId, PointReason.POST_VERIFIED, sourceId = postId, postId = postId)
                // Pendiente aparte: sumar 1 a approvedCount del moderador.
            }
    }
}
