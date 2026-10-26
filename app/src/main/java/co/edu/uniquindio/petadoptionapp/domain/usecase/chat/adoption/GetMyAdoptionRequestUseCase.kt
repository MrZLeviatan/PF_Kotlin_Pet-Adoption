package co.edu.uniquindio.petadoptionapp.domain.usecase.chat.adoption

import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequest
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

// Define la clase del caso de uso para obtener la solicitud de adopción del usuario (para pintar el estado del botón) inyectando el repositorio de chats y el caso de uso de requerir usuario.
class GetMyAdoptionRequestUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase
) {
    // Define la función asíncrona principal que recibe el ID de la publicación y devuelve un resultado con la solicitud de adopción opcional.
    suspend operator fun invoke(postId: String): Result<AdoptionRequest?> {
        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }
        // Solicita al repositorio buscar la solicitud de adopción correspondiente a la publicación y al ID del usuario en sesión, retornando el resultado exitoso con la solicitud o nulo.
        return Result.success(chatRepository.findAdoptionRequest(postId, session.id))
    }
}