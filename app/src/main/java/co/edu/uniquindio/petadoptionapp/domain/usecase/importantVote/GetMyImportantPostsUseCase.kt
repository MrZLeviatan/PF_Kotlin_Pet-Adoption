package co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote

import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.repository.ImportantVoteRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

/**
Documenta el caso de uso que lista las publicaciones que la persona marcó como importantes.
Une dos consultas: los ids de los votos y las publicaciones. Solo muestra las que siguen visibles al público.
 */
class GetMyImportantPostsUseCase(
    // Inyecta el repositorio de votos importantes.
    private val voteRepository: ImportantVoteRepository,
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase
) {
    // Define un objeto de acompañamiento que establece la cantidad máxima de publicaciones por página en 20.
    companion object { const val PAGE_SIZE = 20 }

    // Define la función asíncrona principal que recibe un identificador opcional de publicación para la paginación y devuelve un resultado con la lista de publicaciones.
    suspend operator fun invoke(afterPostId: String? = null): Result<List<Post>> {

        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // 1. Ids de las publicaciones votadas, de la más reciente a la más antigua.
        // Solicita al repositorio los identificadores de las publicaciones votadas y retorna un fallo si la operación no es exitosa.
        val ids = voteRepository.getVotedPostIds(session.id, PAGE_SIZE, afterPostId)
            .getOrElse { return Result.failure(it) }
        // Comprueba si la lista de IDs está vacía y retorna una lista vacía para evitar operaciones innecesarias.
        if (ids.isEmpty()) return Result.success(emptyList())

        // 2. Carga las publicaciones, conserva el orden de los votos y descarta las que ya no se muestran.
        // Carga las publicaciones por sus IDs desde el repositorio y las asocia en un mapa usando su ID como clave.
        val posts = postRepository.findByIds(ids).associateBy { it.id }
        // Mapea los IDs en orden preservando la secuencia de los votos, descarta las nulas y filtra únicamente aquellas que se muestran en el feed público, retornando el resultado exitoso.
        return Result.success(ids.mapNotNull { posts[it] }.filter { it.showsInPublicFeed() })
    }
}