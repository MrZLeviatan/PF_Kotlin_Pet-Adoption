package co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.error.VoteException
import co.edu.uniquindio.petadoptionapp.domain.repository.ImportantVoteRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

/**
Documenta el caso de uso que da o quita el "Es importante" a una publicación.
Solo se vota en publicaciones visibles al público, y nunca la propia, así no se inflan puntos ni relevancia.
 */
class ToggleImportantUseCase(
    // Inyecta el repositorio de votos.
    private val voteRepository: ImportantVoteRepository,
    // Inyecta el repositorio de publicaciones para comprobar que se pueda votar.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que hay una sesión de usuario.
    private val requireUser: RequireUserUseCase
) {
    // Define la función asíncrona que recibe el ID de la publicación y devuelve si quedó votada.
    suspend operator fun invoke(postId: String): Result<Boolean> {

        // 1. Solo un usuario con sesión puede votar.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // 2. La publicación debe existir, estar verificada y seguir en proceso.
        val post = postRepository.findById(postId)
            ?: return Result.failure(PostException.PostNotFound)
        if (!post.showsInPublicFeed()) return Result.failure(PostException.PostNotActive)

        // 3. No se vota la propia publicación.
        if (post.authorId == session.id) return Result.failure(VoteException.CannotVoteOwnPost)

        // 4. Alterna el voto. Pendiente: notificación y puntos al autor solo cuando el resultado sea `true`.
        return voteRepository.toggle(postId, session.id)
    }
}