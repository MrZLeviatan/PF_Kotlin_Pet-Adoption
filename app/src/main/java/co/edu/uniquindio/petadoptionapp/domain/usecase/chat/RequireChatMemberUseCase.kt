package co.edu.uniquindio.petadoptionapp.domain.usecase.chat

import co.edu.uniquindio.petadoptionapp.domain.error.ChatException
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Chat
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

/**
Caso de uso auxiliar que confirma que la persona con sesión participa en el chat indicado.
Si el chat no existe o la persona no participa, devuelve el mismo error para no revelar chats ajenos.
Define también [ChatMembership], el resultado con la cuenta de la sesión y el chat.
Lo reutilizan los demás casos de uso de chat (enviar, observar, marcar leído, borrar...).
**/
// Resultado de comprobar la pertenencia a un chat: la cuenta de la sesión y el chat.
data class ChatMembership(val account: Account, val chat: Chat)


// Declara el caso de uso que exige ser participante del chat.
class RequireChatMemberUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el caso de uso que confirma que hay una sesión de usuario.
    private val requireUser: RequireUserUseCase
) {
    // Define la función asíncrona que recibe el id del chat y devuelve la cuenta y el chat.
    suspend operator fun invoke(chatId: String): Result<ChatMembership> {

        // Solo un usuario con sesión tiene chats.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // El chat debe existir y la persona debe ser participante.
        val chat = chatRepository.findChatById(chatId)
            // Conserva el chat solo si la persona está entre sus participantes.
            ?.takeIf { session.id in it.participantIds }
            // Si no existe o no participa, falla con ChatNotFound.
            ?: return Result.failure(ChatException.ChatNotFound)

        // Devuelve la cuenta y el chat validados.
        return Result.success(ChatMembership(session, chat))
    }
}
