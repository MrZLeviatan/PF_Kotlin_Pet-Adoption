package co.edu.uniquindio.petadoptionapp.domain.model.post

/**
 Desempeño del moderador con sesión, para su dashboard: lo que aprobó, rechazó y tiene en revisión,
 y cómo se reparte en el tiempo, por categoría y por prioridad.
 */
data class ModeratorPerformance(
    val approved: Int = 0,
    val rejected: Int = 0,
    // Tomadas y aún sin decidir.
    val inReview: Int = 0,
    val reviewedToday: Int = 0,
    val reviewedLast7Days: Int = 0,
    // Decisiones por día de los últimos 7 días, del más antiguo (índice 0) a hoy (índice 6).
    val dailyReviews: List<Int> = List(7) { 0 },
    // Decisiones por categoría (id del catálogo), de la que más a la que menos.
    val resolvedByCategory: List<Pair<String, Int>> = emptyList(),
    // Las que tiene en revisión, por prioridad de la IA (null = sin prioridad).
    val inReviewByPriority: Map<PriorityLevel?, Int> = emptyMap(),
) {
    // Aprobadas más rechazadas.
    val resolved: Int get() = approved + rejected

    // Porcentaje de aprobación sobre las resueltas (0 a 100); 0 si aún no resolvió ninguna.
    val approvalRate: Int get() = if (resolved == 0) 0 else Math.round(approved * 100f / resolved)
}
