package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.filter.PostFilter
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository

/**
Caso de uso que entrega una página del feed público con sus filtros.
El tamaño de página es pequeño a propósito: cada documento leído cuenta en la cuota gratuita.
Lo usan los ViewModels del feed y del mapa.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class GetFeedUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository
) {
    // Define la cantidad de publicaciones por página.
    companion object { const val PAGE_SIZE = 20 }

    // Define la función asíncrona que recibe el filtro y el identificador de la última publicación mostrada, y devuelve la página.
    suspend operator fun invoke(filter: PostFilter, afterPostId: String? = null): Result<List<Post>> {

        // Si se pidió cercanía pero el punto o el radio no son válidos, avisa.
        val askedNearby = filter.center != null || filter.radiusKm != null
        // Si se pidió pero el área no está completa, falla con InvalidLocation.
        if (askedNearby && !filter.hasNearbyArea()) return Result.failure(PostException.InvalidLocation)

        // Pide la página al repositorio y descarta cualquier publicación que no deba verse en público.
        return postRepository.getFeed(filter, PAGE_SIZE, afterPostId)
            // Conserva solo las publicaciones verificadas y en proceso.
            .map { posts -> posts.filter { it.showsInPublicFeed() }}
    }
}
