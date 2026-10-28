package co.edu.uniquindio.petadoptionapp.domain.usecase.chat

import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

// Define la clase del caso de uso para contar los chats con mensajes sin leer (para el indicador o globito del ícono) inyectando el repositorio de chats y el caso de uso de requerir usuario.
class CountUnreadChatsUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase
) {
    // Define la función asíncrona principal que devuelve un resultado con el número total de chats que contienen mensajes sin leer.
    suspend operator fun invoke(): Result<Int> {
        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }
        // Solicita al repositorio el conteo de chats con mensajes sin leer utilizando el ID del usuario en sesión y retorna el resultado.
        return chatRepository.countChatsWithUnread(session.id)
    }
}