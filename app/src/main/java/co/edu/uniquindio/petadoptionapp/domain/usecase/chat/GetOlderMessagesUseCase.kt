package co.edu.uniquindio.petadoptionapp.domain.usecase.chat

import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Message
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository


// Define la clase del caso de uso para cargar mensajes anteriores inyectando el repositorio de chats y el caso de uso de requerir miembro del chat.
class GetOlderMessagesUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el caso de uso que confirma que la persona participa en el chat.
    private val requireChatMember: RequireChatMemberUseCase
) {
    // Define un objeto de acompañamiento que establece la cantidad máxima de mensajes por página en 30.
    companion object { const val PAGE_SIZE = 30 }

    // Define la función asíncrona principal que recibe el ID del chat y la marca de tiempo límite, devolviendo un resultado con la lista de mensajes anteriores.
    suspend operator fun invoke(chatId: String, beforeSentAt: Long): Result<List<Message>> {
        // Confirma que el usuario participa en el chat y retorna un fallo si la validación no es exitosa.
        val membership = requireChatMember(chatId).getOrElse { return Result.failure(it) }
        // Solicita al repositorio los mensajes anteriores a la marca de tiempo especificada utilizando el tamaño de página y retorna el resultado.
        // Si eliminó el chat para sí, los anteriores a ese momento ya no se muestran.
        val since = membership.chat.visibleSince(membership.account.id)
        return chatRepository.getMessagesBefore(chatId, beforeSentAt, PAGE_SIZE)
            .map { list -> list.filter { it.sentAt > since } }
    }
}