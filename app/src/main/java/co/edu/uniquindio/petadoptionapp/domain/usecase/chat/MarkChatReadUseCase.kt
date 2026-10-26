package co.edu.uniquindio.petadoptionapp.domain.usecase.chat

import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository

/**
Documenta el caso de uso que marca un chat como leído al abrirlo.
Si no hay mensajes sin leer, no gasta una escritura.
 */
class MarkChatReadUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el caso de uso que confirma que la persona participa en el chat.
    private val requireChatMember: RequireChatMemberUseCase
) {
    // Define la función asíncrona principal que recibe el ID del chat y devuelve un resultado vacío.
    suspend operator fun invoke(chatId: String): Result<Unit> {
        // Confirma que el usuario participa en el chat y obtiene su membresía, retornando un fallo si no está autorizado.
        val membership = requireChatMember(chatId).getOrElse { return Result.failure(it) }
        // Comprueba si el conteo de mensajes sin leer para el usuario actual en el chat es cero y, de ser así, retorna éxito anticipadamente para evitar operaciones de escritura innecesarias.
        if (membership.chat.unreadFor(membership.account.id) == 0) return Result.success(Unit)
        // Solicita al repositorio marcar el chat como leído para el usuario actual utilizando su ID y retorna el resultado.
        return chatRepository.markAsRead(chatId, membership.account.id)
    }
}