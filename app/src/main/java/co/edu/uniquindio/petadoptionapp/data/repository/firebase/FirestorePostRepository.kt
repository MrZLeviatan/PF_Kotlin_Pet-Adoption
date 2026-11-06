package co.edu.uniquindio.petadoptionapp.data.repository.firebase

import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.MAX_IN_VALUES
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.POSTS
import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.post.AiReview
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel
import co.edu.uniquindio.petadoptionapp.domain.model.post.Verification
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.filter.PostFilter
import co.edu.uniquindio.petadoptionapp.domain.model.post.filter.PostSort
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/**
Publicaciones en Firestore: `posts/{id}`.

Para gastar pocos índices, las consultas filtran por uno o dos campos en Firestore y el resto
(ciudad, tamaño, vacunas, distancia) se refina en la app. Con el volumen del proyecto es suficiente.
Los índices compuestos que sí hacen falta están en firebase/firestore.indexes.json.
 */
@Singleton
class FirestorePostRepository @Inject constructor(
    private val db: FirebaseFirestore,
) : PostRepository {

    private val posts = db.collection(POSTS)

    override suspend fun create(post: Post): Result<Post> = runCatching {
        // Firestore genera el id; se guarda también dentro del documento.
        val ref = posts.document()
        val saved = post.copy(id = ref.id)
        ref.set(saved.toFirestore()).await()
        saved
    }

    override suspend fun findById(id: String): Post? =
        runCatching { posts.document(id).get().await().toModel<Post>() }.getOrNull()

    override suspend fun findByIds(ids: List<String>): List<Post> {
        if (ids.isEmpty()) return emptyList()
        val found = ids.distinct().chunked(MAX_IN_VALUES).flatMap { chunk ->
            runCatching {
                posts.whereIn(FieldPath.documentId(), chunk).get().await().toModels<Post>()
            }.getOrDefault(emptyList())
        }.associateBy { it.id }
        // Se respeta el orden pedido.
        return ids.mapNotNull { found[it] }
    }

    override suspend fun getFeed(filter: PostFilter, limit: Int, afterPostId: String?): Result<List<Post>> = runCatching {
        // Solo publicaciones verificadas y en proceso.
        var query = posts
            .whereEqualTo("verification.status", VerificationStatus.VERIFIED.name)
            .whereEqualTo("status", PostStatus.IN_PROGRESS.name)
        query = when (filter.sortBy) {
            PostSort.RECENT -> query.orderBy("createdAt", Query.Direction.DESCENDING)
            PostSort.MOST_IMPORTANT -> query
                .orderBy("importantCount", Query.Direction.DESCENDING)
                .orderBy("createdAt", Query.Direction.DESCENDING)
        }

        // Paginación por cursor: empieza después de la última publicación ya mostrada.
        var cursor: DocumentSnapshot? = afterPostId?.let { id ->
            posts.document(id).get().await().takeIf { it.exists() } ?: return@runCatching emptyList()
        }

        // Lee por tandas hasta llenar la página con las que cumplen los filtros (con un tope de lecturas).
        val result = mutableListOf<Post>()
        repeat(MAX_FEED_BATCHES) {
            val batch = (cursor?.let { query.startAfter(it) } ?: query).limit(FEED_BATCH_SIZE).get().await()
            for (document in batch.documents) {
                cursor = document
                val post = document.toModel<Post>() ?: continue
                if (post.matches(filter)) result += post
                if (result.size == limit) return@runCatching result
            }
            if (batch.size() < FEED_BATCH_SIZE) return@runCatching result
        }
        result
    }

    // Los filtros que no se piden a Firestore.
    private fun Post.matches(filter: PostFilter): Boolean {
        filter.categoryId?.let { if (categoryId != it) return false }
        filter.animalTypeId?.let { if (animalTypeId != it) return false }
        filter.city?.let { if (!location.city.equals(it, ignoreCase = true)) return false }
        filter.size?.let { if (size != it) return false }
        if (filter.vaccinatedOnly && vaccinated != true) return false
        if (filter.hasNearbyArea()) {
            val distance = location.distanceKmTo(filter.center!!) ?: return false
            if (distance > filter.radiusKm!!) return false
        }
        return true
    }

    override suspend fun getByAuthor(authorId: String): Result<List<Post>> = runCatching {
        byAuthor(authorId)
            .filter { it.status != PostStatus.DELETED }
            .sortedByDescending { it.createdAt }
    }

    private suspend fun byAuthor(authorId: String): List<Post> =
        posts.whereEqualTo("authorId", authorId).get().await().toModels()

    override suspend fun countByAuthor(
        authorId: String,
        status: PostStatus,
        verificationStatus: VerificationStatus?
    ): Int = runCatching {
        byAuthor(authorId).count {
            it.status == status && (verificationStatus == null || it.verification.status == verificationStatus)
        }
    }.getOrDefault(0)

    override suspend fun updateContent(post: Post): Result<Unit> = runCatching {
        // Solo toca el contenido editable: no pisa contadores ni estado.
        val document = post.toFirestore()
        posts.document(post.id).update(EDITABLE_FIELDS.associateWith { document[it] }).await()
        Unit
    }.mapNotFound()

    override suspend fun updateStatus(id: String, status: PostStatus, finishedAt: Long?): Result<Unit> = runCatching {
        posts.document(id).update(mapOf(
            "status" to status.name,
            "finishedAt" to finishedAt,
            "updatedAt" to System.currentTimeMillis()
        )).await()
        Unit
    }.mapNotFound()

    override suspend fun deleteAllByAuthor(authorId: String): Result<Unit> = runCatching {
        // Repetirlo no hace daño: solo cierra las que siguen en proceso.
        val now = System.currentTimeMillis()
        val active = posts.whereEqualTo("authorId", authorId)
            .whereEqualTo("status", PostStatus.IN_PROGRESS.name)
            .get().await().documents
        // Un lote acepta hasta 500 escrituras.
        active.chunked(BATCH_LIMIT).forEach { chunk ->
            db.batch().apply {
                chunk.forEach { update(it.reference, mapOf("status" to PostStatus.DELETED.name, "updatedAt" to now)) }
            }.commit().await()
        }
    }

    override suspend fun getPendingModeration(limit: Int): Result<List<Post>> = runCatching {
        posts.whereIn("verification.status", listOf(VerificationStatus.UNVERIFIED.name, VerificationStatus.IN_PROCESS.name))
            .get().await().toModels<Post>()
            .filter { it.status == PostStatus.IN_PROGRESS }
            .sortedBy { it.createdAt }
            .take(limit)
    }

    override suspend fun getInReviewBy(moderatorId: String): Result<List<Post>> = runCatching {
        reviewedBy(moderatorId).filter {
            it.status == PostStatus.IN_PROGRESS && it.verification.status == VerificationStatus.IN_PROCESS
        }
    }

    override suspend fun getReviewedBy(moderatorId: String): Result<List<Post>> = runCatching {
        reviewedBy(moderatorId).filter {
            it.status != PostStatus.DELETED && it.verification.status != VerificationStatus.UNVERIFIED
        }
    }

    private suspend fun reviewedBy(moderatorId: String): List<Post> =
        posts.whereEqualTo("verification.reviewedBy", moderatorId).get().await().toModels()

    override suspend fun countForModeration(
        statuses: Set<VerificationStatus>,
        priorities: Set<PriorityLevel>?,
        reviewedBy: String?,
        reviewedSince: Long?
    ): Int = runCatching {
        if (statuses.isEmpty()) return@runCatching 0
        // Firestore filtra por estado (o por moderador); lo demás se cuenta en la app.
        val query = if (reviewedBy != null) posts.whereEqualTo("verification.reviewedBy", reviewedBy)
        else posts.whereIn("verification.status", statuses.map { it.name })
        query.get().await().toModels<Post>().count {
            val v = it.verification
            it.status != PostStatus.DELETED &&
                v.status in statuses &&
                (priorities == null || v.priorityLevel in priorities) &&
                (reviewedBy == null || v.reviewedBy == reviewedBy) &&
                (reviewedSince == null || (v.reviewedAt ?: 0L) >= reviewedSince)
        }
    }.getOrDefault(0)

    override suspend fun updateVerification(
        id: String,
        expected: VerificationStatus,
        verification: Verification
    ): Result<Unit> = runCatching {
        val ref = posts.document(id)
        // Leer, comparar y escribir en una transacción: dos moderadores no deciden la misma publicación.
        db.runTransaction { tx ->
            val current = tx.get(ref).toModel<Post>() ?: throw PostException.PostNotFound
            if (current.verification.status != expected) throw PostException.AlreadyReviewed
            tx.update(ref, mapOf(
                "verification" to verification.toFirestore(),
                "updatedAt" to System.currentTimeMillis()
            ))
        }.await()
        Unit
    }.unwrapTransactionError()

    override suspend fun updateAiAnalysis(
        id: String,
        review: AiReview,
        priority: PriorityLevel,
        priorityReason: String
    ): Result<Unit> = runCatching {
        // Solo la revisión y la prioridad sugerida: el estado de verificación no cambia.
        posts.document(id).update(mapOf(
            "aiReview" to review.toFirestore(),
            "verification.priorityLevel" to priority.name,
            "verification.priorityReason" to priorityReason
        )).await()
        Unit
    }.mapNotFound()

    // `update` sobre un documento que no existe falla con NOT_FOUND.
    private fun <T> Result<T>.mapNotFound(): Result<T> = recoverCatching { error ->
        throw if (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.NOT_FOUND)
            PostException.PostNotFound else error
    }

    private companion object {
        const val FEED_BATCH_SIZE = 20L
        const val MAX_FEED_BATCHES = 5
        const val BATCH_LIMIT = 450

        val EDITABLE_FIELDS = listOf(
            "title", "categoryId", "description", "animalTypeId", "breed", "size", "ageInMonths",
            "vaccinated", "neutered", "photoUrls", "location", "verification", "aiReview", "updatedAt"
        )
    }
}

/**
Una excepción lanzada dentro de una transacción llega envuelta en otra de Firestore.
Esto devuelve la original (por ejemplo, PostException.AlreadyReviewed) para que la UI la entienda.
 */
private const val DOMAIN_ERRORS_PACKAGE = "co.edu.uniquindio.petadoptionapp.domain.error"

internal fun <T> Result<T>.unwrapTransactionError(): Result<T> = recoverCatching { error ->
    var cause: Throwable? = error
    while (cause != null) {
        if (cause !is FirebaseFirestoreException && cause.javaClass.name.startsWith(DOMAIN_ERRORS_PACKAGE)) throw cause
        cause = cause.cause
    }
    throw error
}
