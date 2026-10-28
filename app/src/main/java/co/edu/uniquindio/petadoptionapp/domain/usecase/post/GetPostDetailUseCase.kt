package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository

/**
Documenta el caso de uso que abre el detalle de una publicación.
Quien no sea el autor ni moderador solo ve las verificadas; las eliminadas solo las ve un moderador.
 */
class GetPostDetailUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(viewer: Account, postId: String): Result<Post> {

        // Busca la publicación; si no existe, avisa.
        val post = postRepository.findById(postId)
            ?: return Result.failure(PostException.PostNotFound)

        val isModerator = viewer.role == UserRole.MODERATOR
        val isAuthor = post.authorId == viewer.id

        // Las eliminadas solo las ve un moderador (el autor ya no las ve, es un borrado lógico).
        if (post.status == PostStatus.DELETED && !isModerator) return Result.failure(PostException.PostNotFound)

        // El autor, los moderadores y cualquiera (si está verificada) pueden verla.
        val canView = isAuthor || isModerator || post.verification.status == VerificationStatus.VERIFIED
        return if (canView) Result.success(post) else Result.failure(PostException.PostNotFound)
    }
}