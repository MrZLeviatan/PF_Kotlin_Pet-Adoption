package co.edu.uniquindio.petadoptionapp.domain.usecase.chat

import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequestStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository

/**
Caso de uso que elimina un chat, solo para quien lo pide o para ambos.
Si el chat nació de "Me interesa adoptar" y desaparece para quien solicitó (porque lo borró para sí
o para ambos), la solicitud pendiente se cancela: así el botón "Me interesa adoptar" vuelve a estar disponible.
Si el autor lo borra solo para sí, la persona interesada conserva su chat y su solicitud.
Lo usan los ViewModels de la lista de chats y de la pantalla de conversación.
**/
// Declara el caso de uso de eliminación de chats.
class DeleteChatUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el caso de uso que confirma que la persona participa en el chat.
    private val requireChatMember: RequireChatMemberUseCase
) {
    // Define la función asíncrona que recibe el chat y si se borra para todos, y devuelve un resultado vacío.
    suspend operator fun invoke(chatId: String, forEveryone: Boolean): Result<Unit> {
        // Solo un participante puede eliminarlo.
        val membership = requireChatMember(chatId).getOrElse { return Result.failure(it) }
        // Toma el chat ya validado.
        val chat = membership.chat
        // Toma el id de quien pide el borrado.
        val myId = membership.account.id

        // Solicitud de adopción ligada a este chat (si la hay), buscada por cada participante como solicitante.
        val request = chat.postId?.let { postId ->
            // Prueba con cada participante y se queda con la primera solicitud encontrada.
            chat.participantIds.firstNotNullOfOrNull { id ->
                // Solo cuenta si la solicitud apunta a este mismo chat.
                chatRepository.findAdoptionRequest(postId, id)?.takeIf { it.chatId == chatId }
            }
        }

        // Elimina el chat (para todos) o solo lo oculta para mí.
        val deleted = if (forEveryone) chatRepository.deleteChat(chatId) else chatRepository.hideChatFor(chatId, myId)
        // Si el borrado falló, devuelve el error.
        deleted.getOrElse { return Result.failure(it) }

        // Restablece "Me interesa adoptar" si el chat ya no existe para quien solicitó.
        // Mejor esfuerzo: si falla, el chat ya quedó eliminado (sin Cloud Functions no hay transacción entre ambos).
        if (request != null && request.awaitsResponse() && (forEveryone || request.requesterId == myId)) {
            // Cancela la solicitud solo si sigue pendiente.
            chatRepository.updateRequestStatus(request.id, AdoptionRequestStatus.PENDING, request.cancel())
        }
        // Informa que el chat se eliminó.
        return Result.success(Unit)
    }
}
