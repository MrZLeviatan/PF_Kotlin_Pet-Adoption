package co.edu.uniquindio.petadoptionapp.domain.model.reputation


/**
 Reputación de un usuario: puntos acumulados, nivel e insignias ganadas.

 Solo [points] y [unlockedBadges] se guardan. El nivel se calcula desde
 los puntos mediante funciones, así nunca queda desincronizado.

 Es inmutable: [addPoints] y [unlockBadge] devuelven una copia nueva.
 */
data class Reputation(
    // Puntos acumulados por participar.
    val points: Int = 0,
    // Insignias desbloqueadas; cada una apunta al catálogo mediante su `badgeId`.
    val unlockedBadges: List<UnlockedBadge> = emptyList()
) {

    // Nivel como clase (enum), calculado desde los puntos.
    fun level(): UserLevel = UserLevel.fromPoints(points)

    // Nivel como número: 1 para el primero, 5 para el más alto.
    fun levelNumber(): Int = level().ordinal + 1

    // Devuelve una copia con los puntos sumados (nunca por debajo de 0).
    fun addPoints(amount: Int): Reputation =
        copy(points = (points + amount).coerceAtLeast(0))

    // ¿La persona ya desbloqueó esta insignia?
    fun hasBadge(badgeId: String): Boolean =
        unlockedBadges.any { it.badgeId == badgeId }

    // Devuelve una copia con la insignia desbloqueada, si aún no la tenía.
    fun unlockBadge(badgeId: String): Reputation =
        if (hasBadge(badgeId)) this
        else copy(unlockedBadges = unlockedBadges + UnlockedBadge(badgeId))
}