package co.edu.uniquindio.petadoptionapp.domain.model.post

/**
 Números del inicio del moderador: publicaciones de toda la app que ningún moderador ha tomado,
 por prioridad sugerida. [pending] es el total sin moderador (incluye las que aún no tienen prioridad).
 */
data class ModerationStats(
    // Sin moderador, con prioridad alta o urgente.
    val highPriority: Int = 0,
    // Sin moderador, con prioridad media.
    val mediumPriority: Int = 0,
    // Sin moderador, con prioridad baja.
    val lowPriority: Int = 0,
    // Todas las que no tienen moderador asignado.
    val pending: Int = 0
)
