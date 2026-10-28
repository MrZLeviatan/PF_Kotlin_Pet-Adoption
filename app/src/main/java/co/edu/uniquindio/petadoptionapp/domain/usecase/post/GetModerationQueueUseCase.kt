package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase

/**
Documenta el caso de uso que entrega al moderador las publicaciones por revisar.
Las ordena por prioridad sugerida por la IA (urgentes primero) y, a igual prioridad, las más antiguas primero.
El moderador se toma de la sesión actual, nunca de un parámetro.
 */
class GetModerationQueueUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    // Define la cantidad de publicaciones que se traen de la cola (cada documento leído cuenta en la cuota gratuita).
    companion object { const val QUEUE_SIZE = 30 }

    // Define la función asíncrona principal; ya no recibe parámetros porque la identidad sale de la sesión.
    suspend operator fun invoke(): Result<List<Post>> {

        // Solo un moderador activo accede a la cola.
        requireModerator().getOrElse { return Result.failure(it) }

        // Pide las pendientes y las ordena: sin prioridad (-1) queda al final, el resto de mayor a menor.
        return postRepository.getPendingModeration(QUEUE_SIZE).map { posts ->
            posts.sortedWith(
                compareByDescending<Post> { it.verification.priorityLevel?.ordinal ?: -1 }
                    .thenBy { it.createdAt }
            )
        }
    }
}