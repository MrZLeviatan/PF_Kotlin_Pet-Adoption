package co.edu.uniquindio.petadoptionapp.domain.model.reputation

/**
Modelo de dominio: resultado de intentar otorgar puntos. No se guarda: es la respuesta de la operación.
Lo devuelve ReputationRepository al otorgar puntos y lo usa AwardPointsUseCase para
decidir si debe avisar que la persona subió de nivel.
**/
// Declara el resultado del premio como clase de datos inmutable.
data class AwardResult(
    // `true` si los puntos se aplicaron (no se habían otorgado antes por el mismo hecho).
    val applied: Boolean,
    // Puntos que tenía la persona antes del premio.
    val previousPoints: Int,
    // Puntos que tiene la persona después del premio.
    val newPoints: Int
) {
    // ¿Este premio hizo subir de nivel a la persona?
    fun leveledUp(): Boolean =
        // Solo cuenta si se aplicó y el nivel calculado antes y después es distinto.
        applied && UserLevel.fromPoints(newPoints) != UserLevel.fromPoints(previousPoints)
}
