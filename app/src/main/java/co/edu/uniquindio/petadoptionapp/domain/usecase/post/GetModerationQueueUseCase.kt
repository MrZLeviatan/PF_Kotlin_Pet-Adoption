package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase

/**
Caso de uso que entrega al moderador las publicaciones por revisar.
Las ordena por prioridad sugerida por la IA (urgentes primero) y, a igual prioridad, las más antiguas primero.
El moderador se toma de la sesión actual, nunca de un parámetro.
Con [unassignedOnly] deja solo las que ningún moderador ha tomado (las "en revisión" ya tienen dueño).
Lo usa el ViewModel de la cola de moderación.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class GetModerationQueueUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    // Define la cantidad de publicaciones que se traen de la cola (cada documento leído cuenta en la cuota gratuita).
    companion object { const val QUEUE_SIZE = 30 }

    // Define la función asíncrona principal; ya no recibe parámetros porque la identidad sale de la sesión.
    suspend operator fun invoke(unassignedOnly: Boolean = false): Result<List<Post>> {

        // Solo un moderador activo accede a la cola.
        requireModerator().getOrElse { return Result.failure(it) }

        // Pide las pendientes y las ordena: sin prioridad (-1) queda al final, el resto de mayor a menor.
        return postRepository.getPendingModeration(QUEUE_SIZE).map { posts ->
            // Si se piden solo las libres, descarta las que ya están en revisión; luego ordena.
            posts.filter { !unassignedOnly || it.verification.status == VerificationStatus.UNVERIFIED }.sortedWith(
                // Primer criterio: prioridad de la IA de mayor a menor (sin prioridad = -1, al final).
                compareByDescending<Post> { it.verification.priorityLevel?.ordinal ?: -1 }
                    // Segundo criterio: a igual prioridad, la más antigua primero.
                    .thenBy { it.createdAt }
            )
        }
    }
}
