package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository

/**
Documenta el caso de uso que lista las publicaciones propias, para la pantalla "Mis publicaciones".
 */
class GetMyPostsUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository
) {
    // Define la función asíncrona que recibe el identificador del autor y devuelve las publicaciones.
    suspend operator fun invoke(authorId: String): Result<List<Post>> =
        postRepository.getByAuthor(authorId)
}