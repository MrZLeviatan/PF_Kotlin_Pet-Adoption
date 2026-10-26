package co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote

import co.edu.uniquindio.petadoptionapp.domain.repository.ImportantVoteRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

/**
 Documenta el caso de uso que indica a cuáles publicaciones de la pantalla ya votó la persona.
 */
class GetMyVotedPostIdsUseCase(
    // Inyecta el repositorio de votos importantes.
    private val voteRepository: ImportantVoteRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase
) {
    // Define la función asíncrona principal que recibe una lista de IDs de publicaciones y devuelve un resultado con un conjunto de identificadores.
    suspend operator fun invoke(postIds: List<String>): Result<Set<String>> {

        // Solo un usuario con sesión tiene votos.
        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // Sin publicaciones no hay nada que consultar (ahorra una lectura).
        // Comprueba si la lista de IDs está vacía y retorna un conjunto vacío para evitar consultas innecesarias.
        if (postIds.isEmpty()) return Result.success(emptySet())

        // Solicita al repositorio los IDs de las publicaciones entre las cuales el usuario ha votado y retorna el resultado.
        return voteRepository.getVotedAmong(session.id, postIds)
    }
}