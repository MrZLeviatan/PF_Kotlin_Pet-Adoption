package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.PostValidator

/**
Documenta el caso de uso con el que un moderador rechaza una publicación.
El motivo es obligatorio y la publicación rechazada no aparece en el feed público.
El moderador se toma de la sesión actual, nunca de un parámetro.
 */
class RejectPostUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    // Define la función asíncrona principal que recibe el ID de la publicación y el motivo, devolviendo un resultado vacío.
    suspend operator fun invoke(postId: String, reason: String): Result<Unit> {

        // Obtiene al moderador de la sesión actual; falla si no hay sesión o no es un moderador activo.
        val moderator = requireModerator().getOrElse { return Result.failure(it) }

        // Sin motivo válido no se rechaza (evita el `require` del modelo, que lanza excepciones).
        if (!PostValidator.isRejectionReasonValid(reason))
            return Result.failure(PostException.RejectionReasonRequired)

        // Busca la publicación y retorna un fallo si no existe.
        val post = postRepository.findById(postId)
            ?: return Result.failure(PostException.PostNotFound)

        // Solo se rechaza mientras la publicación siga en proceso.
        if (post.status != PostStatus.IN_PROGRESS) return Result.failure(PostException.PostNotActive)

        // Si ya fue verificada o rechazada, no se vuelve a revisar.
        val current = post.verification.status
        if (current == VerificationStatus.VERIFIED || current == VerificationStatus.REJECTED)
            return Result.failure(PostException.AlreadyReviewed)

        // Si otro moderador la tiene en revisión, solo ese moderador puede rechazarla.
        if (current == VerificationStatus.IN_PROCESS && post.verification.reviewedBy != moderator.id)
            return Result.failure(PostException.ReviewedByAnotherModerator)

        // Rechaza solo si el estado sigue siendo el que se leyó (la transacción evita pisar a otro moderador).
        return postRepository.updateVerification(
            postId, current, post.verification.reject(moderator.id, reason)
        )
    }
}