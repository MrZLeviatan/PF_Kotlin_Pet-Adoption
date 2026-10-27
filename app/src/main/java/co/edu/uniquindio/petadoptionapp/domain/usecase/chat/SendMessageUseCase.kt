package co.edu.uniquindio.petadoptionapp.domain.usecase.chat

import co.edu.uniquindio.petadoptionapp.domain.error.ChatException
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Message
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.MessageType
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.ChatValidator

// Define la clase del caso de uso para enviar un mensaje de texto inyectando el repositorio de chats y el caso de uso de requerir miembro del chat.
class SendMessageUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el caso de uso que confirma que la persona participa en el chat.
    private val requireChatMember: RequireChatMemberUseCase
) {
    // Define la función asíncrona principal que recibe el ID del chat y el texto, devolviendo un resultado con el objeto del mensaje enviado.
    suspend operator fun invoke(chatId: String, text: String): Result<Message> {

        // 1. Formato del texto, sin internet.
        // Válida que el texto del mensaje cumpla con las reglas permitidas y retorna un fallo si no es válido.
        if (!ChatValidator.isMessageValid(text)) return Result.failure(ChatException.InvalidMessage)

        // 2. La persona debe participar en el chat.
        // Confirma que el usuario actual participa en el chat y obtiene su membresía, retornando un fallo si no está autorizado.
        val membership = requireChatMember(chatId).getOrElse { return Result.failure(it) }

        // 3. Arma el mensaje con el remitente de la sesión, nunca el de un parámetro.
        // Construye una instancia del modelo Message asignando el ID del remitente desde la membresía, el tipo de texto, el contenido recortado y la marca de tiempo actual.
        val message = Message(
            senderId = membership.account.id,
            type = MessageType.TEXT,
            text = text.trim(),
            sentAt = System.currentTimeMillis()
        )

        // 4. Guarda. Pendiente: NotifyUseCase al otro participante (si se decide notificar mensajes).
        // Solicita al repositorio enviar y guardar el mensaje en el chat especificado, retornando el resultado.
        return chatRepository.sendMessage(chatId, message)
    }
}