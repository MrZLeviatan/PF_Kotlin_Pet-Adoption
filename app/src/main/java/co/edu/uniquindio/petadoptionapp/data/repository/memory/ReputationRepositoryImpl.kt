package co.edu.uniquindio.petadoptionapp.data.repository.memory

import co.edu.uniquindio.petadoptionapp.data.repository.send.SeedData
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.AwardResult
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Badge
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointReason
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointTransaction
import co.edu.uniquindio.petadoptionapp.domain.repository.ReputationRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
Documenta la implementación en memoria del repositorio de reputación.
Simula la colección `pointTransactions` y el catálogo de insignias.
Cada hecho da puntos una sola vez gracias al id determinista de la transacción.
 */
@Singleton
class ReputationRepositoryImpl @Inject constructor(
    // Inyecta el repositorio de usuarios para sumar los puntos al perfil.
    private val userStore: UserRepositoryImpl
) : ReputationRepository {

    // Guarda el catálogo de insignias; en memoria ya viene cargado.
    private val _badges = MutableStateFlow<List<Badge>>(SeedData.badges)
    override val badges: StateFlow<List<Badge>> = _badges.asStateFlow()

    // Guarda las transacciones de puntos, de la más reciente a la más antigua.
    private val transactions = MutableStateFlow<List<PointTransaction>>(emptyList())

    // Candado que vuelve atómico "comprobar que no existe, guardar y sumar".
    private val lock = Mutex()

    override suspend fun refreshBadges(): Result<Unit> = Result.success(Unit)

    override suspend fun award(transaction: PointTransaction): Result<AwardResult> = lock.withLock {
        // Si ese hecho ya dio puntos, no se suma de nuevo.
        if (transactions.value.any { it.id == transaction.id }) {
            val points = userStore.findById(transaction.userId)?.reputation?.points ?: 0
            return Result.success(AwardResult(applied = false, previousPoints = points, newPoints = points))
        }

        // Suma al perfil; falla si el perfil no existe o está eliminado.
        val change = userStore.updateReputation(transaction.userId) { it.addPoints(transaction.points) }
            ?: return Result.failure(UserException.ProfileNotFound)

        transactions.update { listOf(transaction) + it }
        Result.success(AwardResult(applied = true, previousPoints = change.first.points, newPoints = change.second.points))
    }

    override suspend fun getTransactions(
        userId: String,
        limit: Int,
        afterTransactionId: String?
    ): Result<List<PointTransaction>> {
        val list = transactions.value.filter { it.userId == userId }
        val start = if (afterTransactionId == null) 0 else {
            val index = list.indexOfFirst { it.id == afterTransactionId }
            if (index < 0) return Result.success(emptyList()) else index + 1
        }
        return Result.success(list.drop(start).take(limit))
    }

    override suspend fun countByReason(userId: String, reason: PointReason, since: Long): Result<Int> =
        Result.success(transactions.value.count { it.userId == userId && it.reason == reason && it.createdAt >= since })

    override suspend fun unlockBadge(userId: String, badgeId: String): Result<Boolean> {
        val change = userStore.updateReputation(userId) { it.unlockBadge(badgeId) }
            ?: return Result.failure(UserException.ProfileNotFound)
        // Es nueva solo si antes no la tenía y ahora sí.
        return Result.success(!change.first.hasBadge(badgeId) && change.second.hasBadge(badgeId))
    }
}