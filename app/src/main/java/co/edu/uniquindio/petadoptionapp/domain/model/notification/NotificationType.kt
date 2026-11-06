package co.edu.uniquindio.petadoptionapp.domain.model.notification


/**
Modelo de dominio: evento que originó una notificación ([Notification.type]).

La interfaz usa este valor para elegir el ícono y el texto
(desde `strings.xml`), así el modelo no guarda mensajes ya escritos.
Lo asignan los casos de uso que llaman a NotifyUseCase (comentarios, moderación,
votos, adopción, reputación y chat).
**/
// Enumera los eventos que generan notificaciones.
enum class NotificationType {
    // Alguien comentó una publicación del usuario.
    NEW_COMMENT,

    // Alguien respondió a un comentario del usuario.
    COMMENT_REPLY,

    // Un moderador verificó una publicación del usuario.
    POST_VERIFIED,

    // Un moderador rechazó una publicación; el motivo va en Notification.extra.
    POST_REJECTED,

    // Alguien marcó "Es importante" en una publicación del usuario.
    POST_IMPORTANT,

    // Alguien marcó "Me interesa adoptar" en una publicación del usuario.
    ADOPTION_REQUEST,

    // Un moderador marcó como resuelta una publicación del usuario.
    POST_FINISHED,

    // El usuario desbloqueó una insignia; el id va en Notification.extra.
    BADGE_UNLOCKED,

    // El usuario subió de nivel; el nivel va en Notification.extra.
    LEVEL_UP,

    // El autor respondió (aceptó o rechazó) una solicitud de adopción.
    ADOPTION_RESPONSE,
    // Alguien escribió en un chat del usuario; el chat va en Notification.chatId.
    NEW_MESSAGE,
}
