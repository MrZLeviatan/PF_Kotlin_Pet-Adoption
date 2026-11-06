package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointReason
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.NotifyUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.AwardPointsUseCase

/**
Caso de uso que marca una publicación como finalizada.
Pueden hacerlo el autor (la suya) o cualquier moderador (cualquiera).
Otorga puntos al autor y, si finalizó un moderador, le avisa. Lo usan los ViewModels del detalle y del moderador.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class FinishPostUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que crea notificaciones.
    private val notify: NotifyUseCase,
    // Inyecta el caso de uso que otorga puntos (también avisa si sube de nivel).
    private val awardPoints: AwardPointsUseCase
) {
    // Define la función asíncrona que recibe el autor y el identificador de la publicación, y devuelve el resultado.
    suspend operator fun invoke(requester: Account, postId: String): Result<Unit> {

        // Busca la publicación; si no existe, avisa.
        val post = postRepository.findById(postId)
            // Si no existe, falla con PostNotFound.
            ?: return Result.failure(PostException.PostNotFound)

        // Solo el autor o un moderador tienen permiso.
        val isAuthor = post.authorId == requester.id
        // Comprueba si quien pide es moderador.
        val isModerator = requester.role == UserRole.MODERATOR
        // Falla con NotAllowed si no es ni autor ni moderador.
        if (!isAuthor && !isModerator) return Result.failure(PostException.NotAllowed)

        // Solo se finaliza lo que sigue en proceso.
        if (post.status != PostStatus.IN_PROGRESS) return Result.failure(PostException.PostNotActive)

        // Marca como finalizada y guarda el momento.
        return postRepository.updateStatus(postId, PostStatus.FINISHED, System.currentTimeMillis())
            // Si se guardó, premia y avisa (mejor esfuerzo).
            .onSuccess {
                // Puntos al autor por resolver (sourceId = postId: se dan una sola vez).
                awardPoints(post.authorId, PointReason.POST_FINISHED, sourceId = postId, postId = postId)
                // Solo se avisa si quien finalizó es un moderador; si fue el propio autor, no hace falta.
                if (!isAuthor) notify(post.authorId, NotificationType.POST_FINISHED, requester.id, requester.userName, postId)
            }
    }
}
