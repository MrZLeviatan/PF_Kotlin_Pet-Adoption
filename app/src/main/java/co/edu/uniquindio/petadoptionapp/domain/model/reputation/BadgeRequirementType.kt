package co.edu.uniquindio.petadoptionapp.domain.model.reputation

/**
 Tipo de logro que se mide para desbloquear una insignia.

 Se usa un enum en lugar de texto suelto para evitar errores de
 escritura y para que la lógica de insignias pueda recorrer todos los
 casos de forma segura.
 */
enum class BadgeRequirementType {
    // Cantidad de publicaciones creadas.
    POSTS_CREATED,

    // Cantidad de publicaciones verificadas por un moderador.
    POSTS_VERIFIED,

    // Cantidad de publicaciones marcadas como resueltas.
    POSTS_RESOLVED,

    // Cantidad de comentarios escritos.
    COMMENTS_WRITTEN,

    // Cantidad de votos recibidos en las publicaciones propias.
    VOTES_RECEIVED,

    // Puntos totales acumulados.
    TOTAL_POINTS
}