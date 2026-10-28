package co.edu.uniquindio.petadoptionapp.domain.usecase.chat

import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Message
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

/**
Documenta el caso de uso que entrega, en vivo, los últimos mensajes de un chat.
Primero confirma la pertenencia y luego devuelve el flujo.
 */
class ObserveMessagesUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el caso de uso que confirma que la persona participa en el chat.
    private val requireChatMember: RequireChatMemberUseCase
) {
    // Define un objeto de acompañamiento que establece la cantidad de mensajes en vivo en 30 (cada mensaje nuevo cuenta como una lectura).
    companion object { const val LIVE_SIZE = 30 }

    // Define la función asíncrona principal que recibe el ID del chat y devuelve un resultado con el flujo de la lista de mensajes.
    suspend operator fun invoke(chatId: String): Result<Flow<List<Message>>> {
        // Confirma que el usuario participa en el chat y retorna un fallo si la validación no es exitosa.
        val membership = requireChatMember(chatId).getOrElse { return Result.failure(it) }
        // Si eliminó el chat para sí, solo ve los mensajes que llegaron después (en Firestore: where sentAt > desde).
        val since = membership.chat.visibleSince(membership.account.id)
        return Result.success(
            chatRepository.observeMessages(chatId, LIVE_SIZE).map { list -> list.filter { it.sentAt > since } }
        )
    }
}