package co.edu.uniquindio.petadoptionapp.domain.model.messaging


/**
Modelo de dominio: clase de mensaje dentro de un chat ([Message.type]).
Permite a la UI decidir cómo dibujar cada mensaje (texto, aviso automático o foto)
y a [Chat] mostrar la vista previa correcta del último mensaje.
**/
// Enumera los tipos de mensaje admitidos.
enum class MessageType {

    // Escrito por la persona.
    TEXT,

    // Generado por el sistema al pulsar "Me interesa adoptar".
    ADOPTION_INTEREST,
    // Una foto (en Message.imageUrl), con texto opcional como pie de foto.
    IMAGE
}
