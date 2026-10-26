package co.edu.uniquindio.petadoptionapp.domain.model.reputation

/**
Resultado de intentar otorgar puntos. No se guarda: es la respuesta de la operación.

 */
data class AwardResult(
    val applied: Boolean,
    val previousPoints: Int,
    val newPoints: Int
) {
    // ¿Este premio hizo subir de nivel a la persona?
    fun leveledUp(): Boolean =
        applied && UserLevel.fromPoints(newPoints) != UserLevel.fromPoints(previousPoints)
}