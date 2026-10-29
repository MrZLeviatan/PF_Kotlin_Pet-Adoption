package co.edu.uniquindio.petadoptionapp.data.repository.memory

import co.edu.uniquindio.petadoptionapp.data.repository.send.SeedData
import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.filter.PostFilter
import co.edu.uniquindio.petadoptionapp.domain.model.post.filter.PostSort
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel
import co.edu.uniquindio.petadoptionapp.domain.model.post.Verification
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
Documenta la implementación en memoria del repositorio de publicaciones.
Simula Firestore: las publicaciones viven en un mapa temporal y se pierden al cerrar la app.
Solo ejecuta: los permisos y las validaciones viven en los casos de uso.
 */
@Singleton
class PostRepositoryImpl @Inject constructor() : PostRepository {

    // Guarda las publicaciones, con el id como clave.
    private val posts = MutableStateFlow(SeedData.posts.associateBy { it.id })

    override suspend fun create(post: Post): Result<Post> {
        // Firestore genera el id al guardar; aquí se simula con un UUID.
        val saved = post.copy(id = UUID.randomUUID().toString())
        posts.update { it + (saved.id to saved) }
        return Result.success(saved)
    }

    override suspend fun findById(id: String): Post? = posts.value[id]

    override suspend fun findByIds(ids: List<String>): List<Post> =
        ids.mapNotNull { posts.value[it] }

    override suspend fun getFeed(filter: PostFilter, limit: Int, afterPostId: String?): Result<List<Post>> {
        // Solo publicaciones verificadas y en proceso.
        var list = posts.value.values.filter { it.showsInPublicFeed() }

        // Aplica cada filtro solo si viene.
        filter.categoryId?.let { c -> list = list.filter { it.categoryId == c } }
        filter.animalTypeId?.let { a -> list = list.filter { it.animalTypeId == a } }
        filter.city?.let { c -> list = list.filter { it.location.city.equals(c, ignoreCase = true) } }

        // Cercanía: se mide la distancia en la app, igual que se hará con Firestore.
        if (filter.hasNearbyArea()) {
            val center = filter.center!!
            val radius = filter.radiusKm!!
            list = list.filter { (it.location.distanceKmTo(center) ?: Double.MAX_VALUE) <= radius }
        }

        // Orden: más recientes, o más destacadas (y a igual votos, las más recientes).
        list = when (filter.sortBy) {
            PostSort.RECENT -> list.sortedByDescending { it.createdAt }
            PostSort.MOST_IMPORTANT ->
                list.sortedWith(compareByDescending<Post> { it.importantCount }.thenByDescending { it.createdAt })
        }

        // Paginación por cursor: empieza después de la última publicación ya mostrada.
        val start = if (afterPostId == null) 0 else {
            val index = list.indexOfFirst { it.id == afterPostId }
            if (index < 0) return Result.success(emptyList()) else index + 1
        }
        return Result.success(list.drop(start).take(limit))
    }

    override suspend fun getByAuthor(authorId: String): Result<List<Post>> =
        Result.success(
            posts.value.values
                .filter { it.authorId == authorId && it.status != PostStatus.DELETED }
                .sortedByDescending { it.createdAt }
        )

    override suspend fun countByAuthor(
        authorId: String,
        status: PostStatus,
        verificationStatus: VerificationStatus?
    ): Int = posts.value.values.count {
        it.authorId == authorId && it.status == status &&
                (verificationStatus == null || it.verification.status == verificationStatus)
    }

    override suspend fun updateContent(post: Post): Result<Unit> {
        if (posts.value[post.id] == null) return Result.failure(PostException.PostNotFound)
        // Solo toca el contenido editable: no pisa contadores ni estado.
        posts.update { map ->
            map[post.id]?.let { current ->
                map + (post.id to current.copy(
                    title = post.title, categoryId = post.categoryId, description = post.description,
                    animalTypeId = post.animalTypeId, breed = post.breed, size = post.size,
                    ageInMonths = post.ageInMonths, vaccinated = post.vaccinated, neutered = post.neutered,
                    photoUrls = post.photoUrls, location = post.location,
                    verification = post.verification, aiSummary = post.aiSummary,
                    updatedAt = post.updatedAt
                ))
            } ?: map
        }
        return Result.success(Unit)
    }

    override suspend fun updateStatus(id: String, status: PostStatus, finishedAt: Long?): Result<Unit> {
        if (posts.value[id] == null) return Result.failure(PostException.PostNotFound)
        posts.update { map ->
            map[id]?.let { map + (id to it.copy(status = status, finishedAt = finishedAt, updatedAt = System.currentTimeMillis())) } ?: map
        }
        return Result.success(Unit)
    }

    override suspend fun deleteAllByAuthor(authorId: String): Result<Unit> {
        // Repetirlo no hace daño: solo cierra las que siguen en proceso.
        posts.update { map ->
            map.mapValues { (_, post) ->
                if (post.authorId == authorId && post.status == PostStatus.IN_PROGRESS)
                    post.copy(status = PostStatus.DELETED, updatedAt = System.currentTimeMillis())
                else post
            }
        }
        return Result.success(Unit)
    }

    override suspend fun countForModeration(
        statuses: Set<VerificationStatus>,
        priorities: Set<PriorityLevel>?,
        reviewedBy: String?,
        reviewedSince: Long?
    ): Int = posts.value.values.count {
        val v = it.verification
        it.status != PostStatus.DELETED &&
            v.status in statuses &&
            (priorities == null || v.priorityLevel in priorities) &&
            (reviewedBy == null || v.reviewedBy == reviewedBy) &&
            (reviewedSince == null || (v.reviewedAt ?: 0L) >= reviewedSince)
    }

    override suspend fun getReviewedBy(moderatorId: String): Result<List<Post>> =
        Result.success(
            posts.value.values.filter {
                it.status != PostStatus.DELETED &&
                    it.verification.reviewedBy == moderatorId &&
                    it.verification.status != VerificationStatus.UNVERIFIED
            }
        )

    override suspend fun getInReviewBy(moderatorId: String): Result<List<Post>> =
        Result.success(
            posts.value.values.filter {
                it.status == PostStatus.IN_PROGRESS &&
                    it.verification.status == VerificationStatus.IN_PROCESS &&
                    it.verification.reviewedBy == moderatorId
            }
        )

    override suspend fun getPendingModeration(limit: Int): Result<List<Post>> =
        Result.success(
            posts.value.values
                .filter {
                    it.status == PostStatus.IN_PROGRESS &&
                            (it.verification.status == VerificationStatus.UNVERIFIED ||
                                    it.verification.status == VerificationStatus.IN_PROCESS)
                }
                .sortedBy { it.createdAt }
                .take(limit)
        )

    override suspend fun updateVerification(
        id: String,
        expected: VerificationStatus,
        verification: Verification
    ): Result<Unit> {
        var error: Exception? = null
        // La comparación y la escritura ocurren en el mismo bloque: simula la transacción de Firestore.
        posts.update { map ->
            val current = map[id]
            when {
                current == null -> { error = PostException.PostNotFound; map }
                current.verification.status != expected -> { error = PostException.AlreadyReviewed; map }
                else -> {
                    error = null
                    map + (id to current.copy(verification = verification, updatedAt = System.currentTimeMillis()))
                }
            }
        }
        return error?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    // ---- Funciones internas (no están en la interfaz): las usan otros repositorios ----

    // Suma o resta al contador "Es importante"; devuelve false si la publicación no existe.
    fun adjustImportantCount(postId: String, delta: Int): Boolean {
        if (posts.value[postId] == null) return false
        posts.update { map ->
            map[postId]?.let { map + (postId to it.copy(importantCount = (it.importantCount + delta).coerceAtLeast(0))) } ?: map
        }
        return true
    }

    // Suma o resta al contador de comentarios; devuelve false si la publicación no existe.
    fun adjustCommentCount(postId: String, delta: Int): Boolean {
        if (posts.value[postId] == null) return false
        posts.update { map ->
            map[postId]?.let { map + (postId to it.copy(commentCount = (it.commentCount + delta).coerceAtLeast(0))) } ?: map
        }
        return true
    }
}