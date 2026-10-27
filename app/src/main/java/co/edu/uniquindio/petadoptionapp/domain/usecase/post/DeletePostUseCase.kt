package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository

/**
Documenta el caso de uso que borra una publicación propia.
Es un borrado lógico: el documento se conserva (para comentarios, chats y estadísticas) pero deja de mostrarse.
Una publicación finalizada ya cerró su caso: queda como historial y no se puede eliminar.
 */
class DeletePostUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository
) {
    // Define la función asíncrona que recibe el autor y el identificador de la publicación, y devuelve el resultado.
    suspend operator fun invoke(userId: String, postId: String): Result<Unit> {

        // Busca la publicación; si no existe, avisa.
        val post = postRepository.findById(postId)
            ?: return Result.failure(PostException.PostNotFound)

        // Solo el autor puede borrarla.
        if (post.authorId != userId) return Result.failure(PostException.NotAllowed)

        // Si ya estaba eliminada, no hay nada que hacer.
        if (post.status == PostStatus.DELETED) return Result.failure(PostException.PostNotFound)

        // Las finalizadas quedan como historial (igual que no se pueden editar).
        if (post.status == PostStatus.FINISHED) return Result.failure(PostException.PostNotActive)

        // Marca como eliminada.
        return postRepository.updateStatus(postId, PostStatus.DELETED, null)
    }
}