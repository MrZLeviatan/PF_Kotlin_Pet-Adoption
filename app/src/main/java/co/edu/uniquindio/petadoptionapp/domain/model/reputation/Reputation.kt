package co.edu.uniquindio.petadoptionapp.domain.model.reputation

import kotlinx.serialization.Serializable


/**
Modelo de dominio: reputación de un usuario: puntos acumulados, nivel e insignias ganadas.

Solo [points] y [unlockedBadges] se guardan. El nivel se calcula desde
los puntos mediante funciones, así nunca queda desincronizado.

Es inmutable: [addPoints] y [unlockBadge] devuelven una copia nueva.
Va embebida en [User.reputation] y la usan los casos de uso de usecase/reputation y el perfil.
**/
// Permite convertir la reputación a/desde el formato de almacenamiento.
@Serializable
// Declara la reputación como clase de datos inmutable.
data class Reputation(
    // Puntos acumulados por participar.
    val points: Int = 0,
    // Insignias desbloqueadas; cada una apunta al catálogo mediante su `badgeId`.
    val unlockedBadges: List<UnlockedBadge> = emptyList()
) {

    // Nivel como clase (enum), calculado desde los puntos.
    fun level(): UserLevel = UserLevel.fromPoints(points)

    // Nivel como número: 1 para el primero, 4 para el más alto (PET_HERO).
    fun levelNumber(): Int = level().ordinal + 1

    // Devuelve una copia con los puntos sumados (nunca por debajo de 0).
    fun addPoints(amount: Int): Reputation =
        // Suma la cantidad y limita el resultado a un mínimo de cero.
        copy(points = (points + amount).coerceAtLeast(0))

    // ¿La persona ya desbloqueó esta insignia?
    fun hasBadge(badgeId: String): Boolean =
        // Busca si alguna insignia desbloqueada tiene ese id.
        unlockedBadges.any { it.badgeId == badgeId }

    // Devuelve una copia con la insignia desbloqueada, si aún no la tenía.
    fun unlockBadge(badgeId: String): Reputation =
        // Si ya la tenía, devuelve la misma reputación sin cambios.
        if (hasBadge(badgeId)) this
        // Si no, agrega un nuevo registro de insignia desbloqueada.
        else copy(unlockedBadges = unlockedBadges + UnlockedBadge(badgeId))
}
