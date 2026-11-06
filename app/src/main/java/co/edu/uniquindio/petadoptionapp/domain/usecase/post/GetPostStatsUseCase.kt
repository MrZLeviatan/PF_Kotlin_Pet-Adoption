package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStats
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

/**
Caso de uso que calcula las métricas del dashboard personal.
Usa conteos en lugar de descargar documentos para cuidar la cuota gratuita.
Lo usa el ViewModel del perfil / dashboard personal del usuario.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class GetPostStatsUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que confirma que hay una sesión de usuario.
    private val requireUser: RequireUserUseCase
) {
    // Función principal: calcula las métricas de la persona con sesión.
    suspend operator fun invoke(): Result<PostStats> {
        // Exige sesión de usuario; si no la hay, devuelve el error.
        val session = requireUser().getOrElse { return Result.failure(it) }
        // Toma el id de la persona con sesión.
        val id = session.id

        // Activas: en proceso y ya verificadas.
        val active = postRepository.countByAuthor(id, PostStatus.IN_PROGRESS, VerificationStatus.VERIFIED)
        // Finalizadas.
        val finished = postRepository.countByAuthor(id, PostStatus.FINISHED)
        // Pendientes: sin verificar o en revisión.
        val pending =
            // Suma las sin verificar y las que están en revisión.
            postRepository.countByAuthor(id, PostStatus.IN_PROGRESS, VerificationStatus.UNVERIFIED) +
                    postRepository.countByAuthor(id, PostStatus.IN_PROGRESS, VerificationStatus.IN_PROCESS)

        // Devuelve las métricas armadas.
        return Result.success(PostStats(active, finished, pending))
    }
}
