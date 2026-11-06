package co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.error.VoteException
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointReason
import co.edu.uniquindio.petadoptionapp.domain.repository.ImportantVoteRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.NotifyUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.AwardPointsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

/**
Caso de uso que da o quita el "Es importante" a una publicación.
Solo se vota en publicaciones visibles al público, y nunca la propia, así no se inflan puntos ni relevancia.
Al votar notifica al autor y le otorga puntos de reputación. Lo usan los ViewModels del feed y del detalle.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class ToggleImportantUseCase(
    // Inyecta el repositorio de votos.
    private val voteRepository: ImportantVoteRepository,
    // Inyecta el repositorio de publicaciones para comprobar que se pueda votar.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que hay una sesión de usuario.
    private val requireUser: RequireUserUseCase,
    // Inyecta el caso de uso que crea notificaciones.
    private val notify: NotifyUseCase,
    // Inyecta el caso de uso que otorga puntos (también avisa si sube de nivel).
    private val awardPoints: AwardPointsUseCase
) {
    // Define la función asíncrona que recibe el ID de la publicación y devuelve si quedó votada.
    suspend operator fun invoke(postId: String): Result<Boolean> {

        // 1. Solo un usuario con sesión puede votar; si no la hay, devuelve el error.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // 2. La publicación debe existir, estar verificada y seguir en proceso.
        val post = postRepository.findById(postId)
            // Si no existe, falla con PostNotFound.
            ?: return Result.failure(PostException.PostNotFound)
        // Si no está visible al público, falla con PostNotActive.
        if (!post.showsInPublicFeed()) return Result.failure(PostException.PostNotActive)

        // 3. No se vota la propia publicación.
        if (post.authorId == session.id) return Result.failure(VoteException.CannotVoteOwnPost)

        // 4. Alterna el voto; notificación y puntos al autor solo cuando el resultado sea `true` (voto dado).
        return voteRepository.toggle(postId, session.id).onSuccess { voted ->
            // Solo actúa si el voto quedó dado (no al quitarlo).
            if (voted) {
                // Avisa al autor que alguien marcó su publicación como importante.
                notify(post.authorId, NotificationType.POST_IMPORTANT, session.id, session.userName, postId)
                // sourceId = "postId_votanteId": dar, quitar y volver a dar no suma dos veces.
                awardPoints(post.authorId, PointReason.IMPORTANT_RECEIVED, "${postId}_${session.id}", postId, session.id)
            }
        }
    }
}
