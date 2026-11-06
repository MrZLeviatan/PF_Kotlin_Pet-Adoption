package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository

/**
Caso de uso que abre el detalle de una publicación.
Quien no sea el autor ni moderador solo ve las verificadas; las eliminadas solo las ve un moderador.
Lo usan el ViewModel del detalle de publicación y GetCommentsUseCase (para aplicar la misma visibilidad).
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class GetPostDetailUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository
) {
    // Función principal: recibe quién mira y la publicación, y la devuelve solo si puede verla.
    suspend operator fun invoke(viewer: Account, postId: String): Result<Post> {

        // Busca la publicación; si no existe, avisa.
        val post = postRepository.findById(postId)
            // Si no existe, falla con PostNotFound.
            ?: return Result.failure(PostException.PostNotFound)

        // Comprueba si quien mira es moderador.
        val isModerator = viewer.role == UserRole.MODERATOR
        // Comprueba si quien mira es el autor.
        val isAuthor = post.authorId == viewer.id

        // Las eliminadas solo las ve un moderador (el autor ya no las ve, es un borrado lógico).
        if (post.status == PostStatus.DELETED && !isModerator) return Result.failure(PostException.PostNotFound)

        // El autor, los moderadores y cualquiera (si está verificada) pueden verla.
        val canView = isAuthor || isModerator || post.verification.status == VerificationStatus.VERIFIED
        // Devuelve la publicación si puede verla; si no, la trata como inexistente.
        return if (canView) Result.success(post) else Result.failure(PostException.PostNotFound)
    }
}
