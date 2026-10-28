package co.edu.uniquindio.petadoptionapp.domain.usecase.chat

import co.edu.uniquindio.petadoptionapp.domain.error.ChatException
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Message
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.MessageType
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.NotifyUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.ChatValidator

/**
Documenta el caso de uso que envía un mensaje de texto en un chat y avisa a la otra persona.
Para no llenar sus notificaciones (una por mensaje), solo avisa con el primer mensaje que tenga sin leer
en ese chat; los siguientes se ven en el contador del chat.
 */
class SendMessageUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el caso de uso que confirma que la persona participa en el chat.
    private val requireChatMember: RequireChatMemberUseCase,
    // Inyecta el caso de uso que crea notificaciones.
    private val notify: NotifyUseCase
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

        // 4. Guarda el mensaje.
        val saved = chatRepository.sendMessage(chatId, message).getOrElse { return Result.failure(it) }

        // 5. Avisa a la otra persona si no tenía nada pendiente en este chat (mejor esfuerzo: si falla, el mensaje ya quedó).
        val chat = membership.chat
        val recipientId = chat.otherParticipantId(membership.account.id)
        if (recipientId != null && chat.unreadFor(recipientId) == 0) {
            notify(
                recipientId, NotificationType.NEW_MESSAGE,
                actorId = membership.account.id, actorName = membership.account.userName,
                postId = chat.postId, chatId = chatId
            )
        }
        return Result.success(saved)
    }
}