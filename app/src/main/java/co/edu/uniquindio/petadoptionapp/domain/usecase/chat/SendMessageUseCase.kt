package co.edu.uniquindio.petadoptionapp.domain.usecase.chat

import co.edu.uniquindio.petadoptionapp.domain.error.ChatException
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Message
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.MessageType
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.NotifyUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.image.UploadImagesUseCase
import co.edu.uniquindio.petadoptionapp.domain.repository.ImageFolder
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.ChatValidator

/**
Caso de uso que envía un mensaje (texto o foto con pie opcional) en un chat y avisa a la otra persona.
Para no llenar sus notificaciones (una por mensaje), solo avisa con el primer mensaje que tenga sin leer
en ese chat; los siguientes se ven en el contador del chat.
Lo usa el ViewModel de la pantalla de conversación.
**/
// Declara el caso de uso de envío de mensajes.
class SendMessageUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el caso de uso que confirma que la persona participa en el chat.
    private val requireChatMember: RequireChatMemberUseCase,
    // Inyecta el caso de uso que crea notificaciones.
    private val notify: NotifyUseCase,
    // Inyecta el caso de uso que sube las fotos del chat a Cloudinary.
    private val uploadImages: UploadImagesUseCase
) {
    // Define la función asíncrona principal que recibe el ID del chat y el texto, devolviendo un resultado con el objeto del mensaje enviado.
    // Con [imageUrl] envía una foto; el texto es entonces un pie de foto opcional.
    suspend operator fun invoke(chatId: String, text: String, imageUrl: String? = null): Result<Message> {

        // 1. Formato del texto, sin internet (una foto puede ir sin texto): sin foto se valida como mensaje, con foto como pie.
        val validText = if (imageUrl.isNullOrBlank()) ChatValidator.isMessageValid(text) else ChatValidator.isCaptionValid(text)
        // Falla si el texto no es válido.
        if (!validText) return Result.failure(ChatException.InvalidMessage)

        // 2. La persona debe participar en el chat.
        // Confirma que el usuario actual participa en el chat y obtiene su membresía, retornando un fallo si no está autorizado.
        val membership = requireChatMember(chatId).getOrElse { return Result.failure(it) }

        // 3. Si es una foto, se sube primero (en el chat queda su URL pública); si falla la subida, devuelve el error.
        val finalImage = uploadImages(imageUrl?.takeIf { it.isNotBlank() }, ImageFolder.CHATS).getOrElse { return Result.failure(it) }

        // 3b. Arma el mensaje con el remitente de la sesión, nunca el de un parámetro.
        val message = Message(
            // Remitente: la cuenta de la sesión.
            senderId = membership.account.id,
            // Tipo: texto si no hay foto, imagen si la hay.
            type = if (finalImage == null) MessageType.TEXT else MessageType.IMAGE,
            // Texto sin espacios sobrantes.
            text = text.trim(),
            // URL pública de la foto (o null).
            imageUrl = finalImage,
            // Momento del envío.
            sentAt = System.currentTimeMillis()
        )

        // 4. Guarda el mensaje; si falla, devuelve el error.
        val saved = chatRepository.sendMessage(chatId, message).getOrElse { return Result.failure(it) }

        // 5. Avisa a la otra persona si no tenía nada pendiente en este chat (mejor esfuerzo: si falla, el mensaje ya quedó).
        val chat = membership.chat
        // Obtiene el id del otro participante.
        val recipientId = chat.otherParticipantId(membership.account.id)
        // Solo notifica si existe destinatario y no tenía mensajes sin leer en este chat.
        if (recipientId != null && chat.unreadFor(recipientId) == 0) {
            // Crea la notificación de mensaje nuevo apuntando al chat.
            notify(
                recipientId, NotificationType.NEW_MESSAGE,
                actorId = membership.account.id, actorName = membership.account.userName,
                postId = chat.postId, chatId = chatId
            )
        }
        // Devuelve el mensaje guardado.
        return Result.success(saved)
    }
}
