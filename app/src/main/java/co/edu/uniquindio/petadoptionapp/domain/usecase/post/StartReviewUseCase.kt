package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase

/**
Caso de uso con el que un moderador toma una publicación para revisarla (queda "en proceso").
El moderador se toma de la sesión actual, nunca de un parámetro.
Lo usa el ViewModel de la cola de moderación al abrir una publicación para revisarla.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class StartReviewUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    // Define la función asíncrona principal que recibe el ID de la publicación y devuelve un resultado vacío.
    suspend operator fun invoke(postId: String): Result<Unit> {

        // Obtiene al moderador de la sesión actual; falla si no hay sesión o no es un moderador activo.
        val moderator = requireModerator().getOrElse { return Result.failure(it) }

        // Busca la publicación y retorna un fallo si no existe.
        val post = postRepository.findById(postId)
            // Si no existe, falla con PostNotFound.
            ?: return Result.failure(PostException.PostNotFound)

        // Solo se toma una publicación que siga en proceso.
        if (post.status != PostStatus.IN_PROGRESS) return Result.failure(PostException.PostNotActive)

        // Si ya está en revisión de otro moderador, se avisa; si ya fue resuelta, no se vuelve a revisar.
        val current = post.verification.status
        // Comprueba si está en revisión por otro moderador.
        if (current == VerificationStatus.IN_PROCESS && post.verification.reviewedBy != moderator.id)
            // Falla con ReviewedByAnotherModerator.
            return Result.failure(PostException.ReviewedByAnotherModerator)
        // Comprueba si ya fue verificada o rechazada.
        if (current != VerificationStatus.UNVERIFIED && current != VerificationStatus.IN_PROCESS)
            // Falla con AlreadyReviewed.
            return Result.failure(PostException.AlreadyReviewed)

        // Si ya la tenía él mismo, no hay nada que cambiar.
        if (current == VerificationStatus.IN_PROCESS) return Result.success(Unit)

        // Toma la revisión solo si el estado sigue siendo el que se leyó (la transacción evita pisar a otro moderador).
        return postRepository.updateVerification(postId, current, post.verification.startReview(moderator.id))
    }
}
