package co.edu.uniquindio.petadoptionapp.domain.model.reputation

import kotlinx.serialization.Serializable

/**
Modelo de dominio: registro de una insignia que una persona ya desbloqueó.

Solo guarda la referencia al catálogo y la fecha. No copia el nombre ni
la descripción: si el catálogo cambia, la insignia se ve actualizada
sin modificar a ningún usuario.
Se guarda dentro de [Reputation.unlockedBadges].
**/
// Permite convertir el registro a/desde el formato de almacenamiento.
@Serializable
// Declara la insignia desbloqueada como clase de datos inmutable.
data class UnlockedBadge(
    // Id de la insignia en el catálogo ([Badge.id]).
    val badgeId: String = "",
    // Momento del desbloqueo, en milisegundos (epoch).
    val unlockedAt: Long = System.currentTimeMillis()
)
