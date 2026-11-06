package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository

/**
Caso de uso que lista las publicaciones propias, para la pantalla "Mis publicaciones".
Incluye las publicaciones en cualquier estado excepto las eliminadas. Lo usa el ViewModel de "Mis publicaciones".
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class GetMyPostsUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository
) {
    // Define la función asíncrona que recibe el identificador del autor y devuelve las publicaciones.
    suspend operator fun invoke(authorId: String): Result<List<Post>> =
        // Delega la consulta en el repositorio.
        postRepository.getByAuthor(authorId)
}
