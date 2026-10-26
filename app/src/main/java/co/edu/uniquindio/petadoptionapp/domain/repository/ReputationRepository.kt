package co.edu.uniquindio.petadoptionapp.domain.repository

import co.edu.uniquindio.petadoptionapp.domain.model.reputation.AwardResult
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Badge
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointReason
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointTransaction
import kotlinx.coroutines.flow.StateFlow

/**
Contrato de reputación: puntos, historial e insignias guardados en Firestore.
Las reglas de cuándo premiar y qué insignias se cumplen viven en los casos de uso, aquí solo se ejecuta.
 */
interface ReputationRepository {

    // Catálogo de insignias

    /**
    Flujo reactivo del catálogo de insignias.

    - Qué emite: Un [StateFlow] con las insignias cargadas en memoria.
    El catálogo es pequeño y casi no cambia: se lee una vez y se reutiliza para ahorrar lecturas de la cuota gratuita.
     */
    val badges: StateFlow<List<Badge>>

    /**
    Lee el catálogo de Firestore y actualiza el flujo.

    - Qué recibe: Un [Result<Unit>] que confirma si se cargó.
     */
    suspend fun refreshBadges(): Result<Unit>


    // Puntos

    /**
    Otorga puntos a una persona, una sola vez por hecho.

    - Qué envía ([transaction]): El registro con su id determinista.
    - Qué recibe: Un [Result] con el [AwardResult]; `applied = false` si ese id ya existía (no suma de nuevo).
    En una transacción: crea el documento `PointTransaction` solo si no existe y suma los puntos a `reputation.points` del perfil.
    Falla si el perfil no existe. Si la beneficiaria es la persona con sesión, la implementación actualiza `currentUser`.
     */
    suspend fun award(transaction: PointTransaction): Result<AwardResult>

    /**
    Obtiene una página del historial de puntos, del más reciente al más antiguo.

    - Qué envía: El [userId], el [limit] y, opcionalmente, el [afterTransactionId] del último ya mostrado.
    - Qué recibe: Un [Result] con la lista de transacciones.
     */
    suspend fun getTransactions(
        userId: String,
        limit: Int,
        afterTransactionId: String? = null
    ): Result<List<PointTransaction>>

    /**
    Cuenta las transacciones de una persona por motivo, opcionalmente desde un momento dado.

    - Qué envía: El [userId], el [reason] y [since] (milisegundos; 0 cuenta todas).
    - Qué recibe: Un [Result] con la cantidad. Un conteo cuesta una fracción de descargar los documentos.
     */
    suspend fun countByReason(userId: String, reason: PointReason, since: Long = 0L): Result<Int>


    // Insignias

    /**
    Desbloquea una insignia para una persona, sin duplicados.

    - Qué envía: El [userId] y el [badgeId] del catálogo.
    - Qué recibe: Un [Result] con `true` si se desbloqueó ahora, o `false` si ya la tenía.
    En una transacción: lee el perfil, comprueba que no la tenga y agrega la `UnlockedBadge`.
     */
    suspend fun unlockBadge(userId: String, badgeId: String): Result<Boolean>
}