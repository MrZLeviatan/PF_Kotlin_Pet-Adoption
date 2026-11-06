package co.edu.uniquindio.petadoptionapp.domain.error

/**
Errores del dominio de chats y mensajes.
Los lanzan ChatValidator y los casos de uso de usecase/chat (abrir chats, enviar
mensajes, borrar conversaciones). La UI traduce cada uno a un texto amigable usando strings.xml.
**/
// Jerarquía sellada con los errores posibles de la mensajería.
sealed class ChatException : Exception() {

    // Ocurre cuando el mensaje está vacío o supera el máximo de caracteres.
    object InvalidMessage : ChatException()

    // Ocurre cuando el chat no existe o la persona no participa en él.
    object ChatNotFound : ChatException()

    // Ocurre cuando alguien intenta abrir un chat consigo mismo.
    object CannotChatWithSelf : ChatException()
}
