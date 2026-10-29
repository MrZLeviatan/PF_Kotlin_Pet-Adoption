package co.edu.uniquindio.petadoptionapp.domain.model.post

/**
 Números del dashboard de moderación.
 [highPriority], [inReview] y [pending] son de toda la aplicación; [reviewedToday] es solo del moderador con sesión.
 */
data class ModerationStats(
    // Sin resolver (sin moderador o en revisión) con prioridad alta o urgente según la IA.
    val highPriority: Int = 0,
    // Tomadas por algún moderador y aún sin decisión.
    val inReview: Int = 0,
    // Aprobadas o rechazadas hoy por el moderador con sesión.
    val reviewedToday: Int = 0,
    // Sin moderador asignado todavía.
    val pending: Int = 0
)
