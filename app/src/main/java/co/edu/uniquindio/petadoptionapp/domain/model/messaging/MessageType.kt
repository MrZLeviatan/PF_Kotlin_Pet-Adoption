package co.edu.uniquindio.petadoptionapp.domain.model.messaging


/**
 Clase de mensaje dentro de un chat.
 */
enum class MessageType {

    // Escrito por la persona.
    TEXT,

    // Generado por el sistema al pulsar "Me interesa adoptar".
    ADOPTION_INTEREST,
    // Una foto (en Message.imageUrl), con texto opcional como pie de foto.
    IMAGE
}