package co.edu.uniquindio.petadoptionapp.domain.usecase.chat

import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Chat
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

// Define la clase del caso de uso para listar los chats del usuario inyectando el repositorio de chats y el caso de uso de requerir usuario.
class GetMyChatsUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase
) {
    // Define un objeto de acompañamiento que establece la cantidad máxima de chats por página en 20 (cada documento leído cuenta en la cuota gratuita).
    companion object { const val PAGE_SIZE = 20 }

    // Define la función asíncrona principal que recibe un identificador opcional de chat para la paginación y devuelve un resultado con la lista de chats.
    suspend operator fun invoke(afterChatId: String? = null): Result<List<Chat>> {
        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }
        // Solicita al repositorio los chats en los que participa el usuario utilizando su ID, el tamaño de página y el paginador, retornando el resultado.
        // Los chats abiertos con "Empezar a chatear" en los que nadie ha escrito aún no se listan:
        // aparecen con el primer mensaje, como en WhatsApp (en Firestore: where lastSenderId != "").
        return chatRepository.getChatsByParticipant(session.id, PAGE_SIZE, afterChatId)
            .map { chats -> chats.filter { it.lastSenderId.isNotBlank() } }
    }
}