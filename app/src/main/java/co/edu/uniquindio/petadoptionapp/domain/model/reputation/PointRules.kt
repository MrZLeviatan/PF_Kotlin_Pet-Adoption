package co.edu.uniquindio.petadoptionapp.domain.model.reputation

/**
Reglas de puntos. Es el único lugar donde se definen: para ajustar el sistema de reputación se cambia solo este archivo.
 */
object PointRules {
    // Puntos para el autor cuando un moderador verifica su publicación.
    const val POST_VERIFIED = 20
    // Puntos por escribir un comentario.
    const val COMMENT_CREATED = 2
    // Puntos para el autor por cada persona que marca "Es importante" en su publicación.
    const val IMPORTANT_RECEIVED = 5
    // Puntos para el autor cuando su publicación se finaliza.
    const val POST_FINISHED = 30
    // Máximo de comentarios que dan puntos en 24 horas.
    const val MAX_REWARDED_COMMENTS_PER_DAY = 10
}