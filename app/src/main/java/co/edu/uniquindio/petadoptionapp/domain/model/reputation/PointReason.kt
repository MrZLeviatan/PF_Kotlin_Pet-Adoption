package co.edu.uniquindio.petadoptionapp.domain.model.reputation

/**
Motivo por el que se otorgan puntos. Cada motivo toma su valor de [PointRules].
Se usa un enum (y no texto suelto) para evitar errores de escritura y para poder contar por motivo.
 */
enum class PointReason(val points: Int) {
    // Una publicación del usuario fue verificada.
    POST_VERIFIED(PointRules.POST_VERIFIED),
    // Una publicación del usuario fue finalizada.
    POST_FINISHED(PointRules.POST_FINISHED),
    // El usuario escribió un comentario.
    COMMENT_CREATED(PointRules.COMMENT_CREATED),
    // Alguien marcó "Es importante" en una publicación del usuario.
    IMPORTANT_RECEIVED(PointRules.IMPORTANT_RECEIVED)
}