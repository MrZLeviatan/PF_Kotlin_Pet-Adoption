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

Implementa ReputationRepository del dominio y se enlaza en RepositoryModule. Los casos de uso de
reputación lo llaman cuando ocurre un hecho que da puntos (publicar, ser aprobado, recibir votos...)
y para desbloquear insignias; el dashboard de métricas lee el historial y el catálogo.
**/
// Una sola instancia: el listener del catálogo de insignias se registra una sola vez.
@Singleton
// Hilt inyecta Firestore desde FirebaseModule.
class FirestoreReputationRepository @Inject constructor(
    // Base de datos Firestore.
    private val db: FirebaseFirestore,
) : ReputationRepository {

    // Referencia a la colección del historial de puntos.
    private val transactions = db.collection(POINT_TRANSACTIONS)
    // Referencia a la colección de perfiles de usuario.
    private val users = db.collection(USERS)

    // Estado interno con el catálogo de insignias.
    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    // Vista de solo lectura del catálogo de insignias.
    override val badges: StateFlow<List<Badge>> = _badges.asStateFlow()

    // El catálogo es pequeño y casi no cambia: se escucha desde que abre la app.
    init {
        // Escucha en vivo la colección de insignias.
        db.collection(BADGES).addSnapshotListener { snapshot, _ ->
            // Si llegó una instantánea válida, publica las insignias leídas.
            snapshot?.let { _badges.value = it.toModels() }
        }
    }

    // Fuerza una lectura completa del catálogo de insignias.
    override suspend fun refreshBadges(): Result<Unit> = runCatching {
        // Lee todas las insignias y actualiza el estado.
        _badges.value = db.collection(BADGES).get().await().toModels()
    }

    // Otorga (o quita, si son negativos) puntos a un usuario por un hecho concreto, una sola vez.
    override suspend fun award(transaction: PointTransaction): Result<AwardResult> = runCatching {
        // Referencia al movimiento con id determinista (identifica el hecho).
        val transactionRef = transactions.document(transaction.id)
        // Referencia al perfil del usuario que recibe los puntos.
        val userRef = users.document(transaction.userId)
        // Transacción: el historial y los puntos del perfil se actualizan juntos o no se actualiza nada.
        db.runTransaction { tx ->
            // Lee el perfil actual del usuario.
            val user = tx.get(userRef).toModel<User>()
            // Si ese hecho ya dio puntos, no se suma de nuevo.
            if (tx.get(transactionRef).exists()) {
                // Puntos actuales (o 0 si no hay perfil).
                val points = user?.reputation?.points ?: 0
                // Devuelve que no se aplicó, con los puntos sin cambios.
                return@runTransaction AwardResult(applied = false, previousPoints = points, newPoints = points)
            }
            // Falla si el perfil no existe o está eliminado.
            if (user == null || user.deleted) throw UserException.ProfileNotFound

            // Calcula la nueva reputación aplicando los puntos con las reglas del dominio.
            val updated = user.reputation.addPoints(transaction.points)
            // Registra el movimiento en el historial.
            tx.set(transactionRef, transaction.toFirestore())
            // Actualiza solo el campo de puntos dentro de la reputación del perfil.
            tx.update(userRef, "reputation.points", updated.points)
            // Devuelve que se aplicó, con los puntos de antes y de después (para detectar subidas de nivel).
            AwardResult(applied = true, previousPoints = user.reputation.points, newPoints = updated.points)
        // Espera a que la transacción termine.
        }.await()
    // Extrae el error de dominio original si la transacción lo envolvió.
    }.unwrapTransactionError()

    // Historial paginado de movimientos de puntos de un usuario.
    override suspend fun getTransactions(
        // Usuario dueño del historial.
        userId: String,
        // Tamaño de la página.
        limit: Int,
        // Id del último movimiento de la página anterior (null para la primera).
        afterTransactionId: String?
    ): Result<List<PointTransaction>> = runCatching {
        // Movimientos del usuario, del más reciente al más antiguo.
        val query = transactions.whereEqualTo("userId", userId).orderBy("createdAt", Query.Direction.DESCENDING)
        // Documento cursor para continuar la paginación; si ya no existe, no hay más resultados.
        val cursor = afterTransactionId?.let { id ->
            transactions.document(id).get().await().takeIf { it.exists() } ?: return@runCatching emptyList()
        }
        // Empieza después del cursor si existe; si no, desde el principio.
        (cursor?.let { query.startAfter(it) } ?: query)
            // Limita el tamaño de la página.
            .limit(limit.toLong())
            // Ejecuta la consulta.
            .get().await()
            // Convierte los documentos en movimientos.
            .toModels()
    }

    // Cuenta cuántas veces el usuario recibió puntos por un motivo desde una fecha (para topes diarios o insignias).
    override suspend fun countByReason(userId: String, reason: PointReason, since: Long): Result<Int> = runCatching {
        // Filtra por usuario.
        transactions.whereEqualTo("userId", userId)
            // Filtra por el motivo (se guarda como texto del enum).
            .whereEqualTo("reason", reason.name)
            // Solo los movimientos desde la fecha indicada (milisegundos).
            .whereGreaterThanOrEqualTo("createdAt", since)
            // Pide al servidor solo el conteo, sin descargar los documentos.
            .count().get(AggregateSource.SERVER).await()
            // Convierte el conteo a entero.
            .count.toInt()
    }

    // Desbloquea una insignia para el usuario; devuelve true solo si es nueva.
    override suspend fun unlockBadge(userId: String, badgeId: String): Result<Boolean> = runCatching {
        // Referencia al perfil del usuario.
        val userRef = users.document(userId)
        // Transacción para leer y escribir las insignias sin perder cambios concurrentes.
        db.runTransaction { tx ->
            // Lee el perfil actual.
            val user = tx.get(userRef).toModel<User>()
            // Falla si el perfil no existe o está eliminado.
            if (user == null || user.deleted) throw UserException.ProfileNotFound
            // Sin duplicados: es nueva solo si antes no la tenía.
            if (user.reputation.hasBadge(badgeId)) return@runTransaction false
            // Agrega la nueva insignia a la lista de desbloqueadas.
            val badges = user.reputation.unlockedBadges + UnlockedBadge(badgeId)
            // Guarda la lista completa convertida a mapas de Firestore.
            tx.update(userRef, "reputation.unlockedBadges", badges.map { it.toFirestore() })
            // Indica que la insignia se desbloqueó ahora.
            true
        // Espera a que la transacción termine.
        }.await()
    // Extrae el error de dominio original si la transacción lo envolvió.
    }.unwrapTransactionError()
}
