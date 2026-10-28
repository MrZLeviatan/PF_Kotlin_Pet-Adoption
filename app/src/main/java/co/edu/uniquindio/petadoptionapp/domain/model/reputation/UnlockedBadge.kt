package co.edu.uniquindio.petadoptionapp.domain.model.reputation

/**
 Registro de una insignia que una persona ya desbloqueó.

 Solo guarda la referencia al catálogo y la fecha. No copia el nombre ni
 la descripción: si el catálogo cambia, la insignia se ve actualizada
 sin modificar a ningún usuario.
 */
data class UnlockedBadge(
    val badgeId: String = "",
    val unlockedAt: Long = System.currentTimeMillis()
)