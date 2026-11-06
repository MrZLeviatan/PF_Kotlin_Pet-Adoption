package co.edu.uniquindio.petadoptionapp.domain.model.post

/**
Modelo de dominio: números del inicio del moderador: publicaciones de toda la app que ningún moderador ha tomado,
por prioridad sugerida. [pending] es el total sin moderador (incluye las que aún no tienen prioridad).
Lo produce GetModerationStatsUseCase y lo muestra el panel principal del moderador.
**/
// Declara las estadísticas de moderación como clase de datos inmutable.
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
