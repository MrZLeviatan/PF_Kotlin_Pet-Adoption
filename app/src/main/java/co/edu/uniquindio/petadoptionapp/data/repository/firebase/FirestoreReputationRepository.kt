package co.edu.uniquindio.petadoptionapp.data.repository.firebase

import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.BADGES
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.POINT_TRANSACTIONS
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.USERS
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.AwardResult
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Badge
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointReason
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointTransaction
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.UnlockedBadge
import co.edu.uniquindio.petadoptionapp.domain.model.users.User
import co.edu.uniquindio.petadoptionapp.domain.repository.ReputationRepository
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
Reputación: historial en `pointTransactions/{id}` (id determinista: un hecho da puntos una sola vez),
puntos e insignias dentro de `users/{uid}.reputation` y el catálogo de insignias en `badges/{id}`.
El perfil de la sesión se actualiza solo, porque FirestoreUserRepository lo escucha en vivo.
 */
@Singleton
class FirestoreReputationRepository @Inject constructor(
    private val db: FirebaseFirestore,
) : ReputationRepository {

    private val transactions = db.collection(POINT_TRANSACTIONS)
    private val users = db.collection(USERS)

    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    override val badges: StateFlow<List<Badge>> = _badges.asStateFlow()

    // El catálogo es pequeño y casi no cambia: se escucha desde que abre la app.
    init {
        db.collection(BADGES).addSnapshotListener { snapshot, _ ->
            snapshot?.let { _badges.value = it.toModels() }
        }
    }

    override suspend fun refreshBadges(): Result<Unit> = runCatching {
        _badges.value = db.collection(BADGES).get().await().toModels()
    }

    override suspend fun award(transaction: PointTransaction): Result<AwardResult> = runCatching {
        val transactionRef = transactions.document(transaction.id)
        val userRef = users.document(transaction.userId)
        db.runTransaction { tx ->
            val user = tx.get(userRef).toModel<User>()
            // Si ese hecho ya dio puntos, no se suma de nuevo.
            if (tx.get(transactionRef).exists()) {
                val points = user?.reputation?.points ?: 0
                return@runTransaction AwardResult(applied = false, previousPoints = points, newPoints = points)
            }
            // Falla si el perfil no existe o está eliminado.
            if (user == null || user.deleted) throw UserException.ProfileNotFound

            val updated = user.reputation.addPoints(transaction.points)
            tx.set(transactionRef, transaction.toFirestore())
            tx.update(userRef, "reputation.points", updated.points)
            AwardResult(applied = true, previousPoints = user.reputation.points, newPoints = updated.points)
        }.await()
    }.unwrapTransactionError()

    override suspend fun getTransactions(
        userId: String,
        limit: Int,
        afterTransactionId: String?
    ): Result<List<PointTransaction>> = runCatching {
        val query = transactions.whereEqualTo("userId", userId).orderBy("createdAt", Query.Direction.DESCENDING)
        val cursor = afterTransactionId?.let { id ->
            transactions.document(id).get().await().takeIf { it.exists() } ?: return@runCatching emptyList()
        }
        (cursor?.let { query.startAfter(it) } ?: query)
            .limit(limit.toLong())
            .get().await()
            .toModels()
    }

    override suspend fun countByReason(userId: String, reason: PointReason, since: Long): Result<Int> = runCatching {
        transactions.whereEqualTo("userId", userId)
            .whereEqualTo("reason", reason.name)
            .whereGreaterThanOrEqualTo("createdAt", since)
            .count().get(AggregateSource.SERVER).await()
            .count.toInt()
    }

    override suspend fun unlockBadge(userId: String, badgeId: String): Result<Boolean> = runCatching {
        val userRef = users.document(userId)
        db.runTransaction { tx ->
            val user = tx.get(userRef).toModel<User>()
            if (user == null || user.deleted) throw UserException.ProfileNotFound
            // Sin duplicados: es nueva solo si antes no la tenía.
            if (user.reputation.hasBadge(badgeId)) return@runTransaction false
            val badges = user.reputation.unlockedBadges + UnlockedBadge(badgeId)
            tx.update(userRef, "reputation.unlockedBadges", badges.map { it.toFirestore() })
            true
        }.await()
    }.unwrapTransactionError()
}
