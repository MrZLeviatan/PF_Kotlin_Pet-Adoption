package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase

/**
Caso de uso que lista las publicaciones que el moderador con sesión tomó para revisar
y aún no decide (en revisión por él; no las verificadas ni las rechazadas).
Las ordena como la cola: urgentes primero y, a igual prioridad, las más antiguas primero.
Lo usa el ViewModel de la pantalla "Mis revisiones" del moderador.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class GetMyReviewsUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    // Función principal: devuelve las publicaciones en revisión del moderador, ordenadas.
    suspend operator fun invoke(): Result<List<Post>> {
        // Exige un moderador activo; si no lo es, devuelve el error.
        val moderator = requireModerator().getOrElse { return Result.failure(it) }
        // Consulta las que tiene en revisión y las ordena.
        return postRepository.getInReviewBy(moderator.id).map { posts ->
            // Ordena con dos criterios.
            posts.sortedWith(
                // Primero por prioridad de la IA, de mayor a menor (sin prioridad = -1, al final).
                compareByDescending<Post> { it.verification.priorityLevel?.ordinal ?: -1 }
                    // A igual prioridad, la más antigua primero.
                    .thenBy { it.createdAt }
            )
        }
    }
}
