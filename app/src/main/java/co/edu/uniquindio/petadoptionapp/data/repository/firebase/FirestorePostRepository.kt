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

Implementa PostRepository del dominio y se enlaza en RepositoryModule. Lo usan los casos de uso
de publicaciones (crear, editar, cambiar estado, feed con filtros), los de moderación (cola de
pendientes, aprobar/rechazar con control de concurrencia) y el análisis con IA. Este archivo
también define unwrapTransactionError, que comparten los demás repositorios Firestore.
**/
// Una sola instancia compartida en toda la app.
@Singleton
// Hilt inyecta Firestore desde FirebaseModule.
class FirestorePostRepository @Inject constructor(
    // Base de datos Firestore.
    private val db: FirebaseFirestore,
) : PostRepository {

    // Referencia a la colección de publicaciones.
    private val posts = db.collection(POSTS)

    // Crea una publicación y la devuelve con su id.
    override suspend fun create(post: Post): Result<Post> = runCatching {
        // Firestore genera el id; se guarda también dentro del documento.
        val ref = posts.document()
        // Copia de la publicación con el id generado.
        val saved = post.copy(id = ref.id)
        // Guarda el documento.
        ref.set(saved.toFirestore()).await()
        // Devuelve la publicación guardada.
        saved
    }

    // Busca una publicación por id; null si no existe o falla la lectura.
    override suspend fun findById(id: String): Post? =
        runCatching { posts.document(id).get().await().toModel<Post>() }.getOrNull()

    // Busca varias publicaciones por id, respetando el orden de la lista recibida.
    override suspend fun findByIds(ids: List<String>): List<Post> {
        // Sin ids no hay nada que consultar.
        if (ids.isEmpty()) return emptyList()
        // Quita repetidos y consulta por lotes de 30 (límite de whereIn); los lotes que fallen se ignoran.
        val found = ids.distinct().chunked(MAX_IN_VALUES).flatMap { chunk ->
            runCatching {
                // Consulta los documentos cuyo id está en el lote.
                posts.whereIn(FieldPath.documentId(), chunk).get().await().toModels<Post>()
            }.getOrDefault(emptyList())
        // Indexa los resultados por id para buscarlos rápido.
        }.associateBy { it.id }
        // Se respeta el orden pedido.
        return ids.mapNotNull { found[it] }
    }

    // Página del feed público con los filtros del usuario.
    override suspend fun getFeed(filter: PostFilter, limit: Int, afterPostId: String?): Result<List<Post>> = runCatching {
        // Solo publicaciones verificadas y en proceso.
        var query = posts
            // Aprobadas por un moderador.
            .whereEqualTo("verification.status", VerificationStatus.VERIFIED.name)
            // Que siguen activas.
            .whereEqualTo("status", PostStatus.IN_PROGRESS.name)
        // Aplica el orden elegido por el usuario.
        query = when (filter.sortBy) {
            // Más recientes primero.
            PostSort.RECENT -> query.orderBy("createdAt", Query.Direction.DESCENDING)
            // Más votadas como importantes primero y, en empate, las más recientes.
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
        // Hace como máximo MAX_FEED_BATCHES lecturas para no gastar demasiadas consultas.
        repeat(MAX_FEED_BATCHES) {
            // Lee la siguiente tanda a partir del cursor.
            val batch = (cursor?.let { query.startAfter(it) } ?: query).limit(FEED_BATCH_SIZE).get().await()
            // Recorre los documentos de la tanda.
            for (document in batch.documents) {
                // Avanza el cursor al documento actual (la próxima tanda empieza después de él).
                cursor = document
                // Convierte el documento; si está mal formado, lo salta.
                val post = document.toModel<Post>() ?: continue
                // Agrega la publicación si cumple los filtros que se aplican en la app.
                if (post.matches(filter)) result += post
                // Si la página ya está llena, termina.
                if (result.size == limit) return@runCatching result
            }
            // Si la tanda vino incompleta, ya no hay más publicaciones en Firestore.
            if (batch.size() < FEED_BATCH_SIZE) return@runCatching result
        }
        // Devuelve lo encontrado aunque la página no se haya llenado (se alcanzó el tope de lecturas).
        result
    }

    // Los filtros que no se piden a Firestore.
    private fun Post.matches(filter: PostFilter): Boolean {
        // Si se filtró por categoría, debe coincidir.
        filter.categoryId?.let { if (categoryId != it) return false }
        // Si se filtró por tipo de animal, debe coincidir.
        filter.animalTypeId?.let { if (animalTypeId != it) return false }
        // Si se filtró por ciudad, debe coincidir sin importar mayúsculas.
        filter.city?.let { if (!location.city.equals(it, ignoreCase = true)) return false }
        // Si se filtró por tamaño, debe coincidir.
        filter.size?.let { if (size != it) return false }
        // Si se pidieron solo vacunados, descarta los que no lo están o no lo indican.
        if (filter.vaccinatedOnly && vaccinated != true) return false
        // Si se pidió un área cercana, comprueba la distancia al centro.
        if (filter.hasNearbyArea()) {
            // Calcula la distancia en km; si la publicación no tiene coordenadas, se descarta.
            val distance = location.distanceKmTo(filter.center!!) ?: return false
            // Descarta las que estén fuera del radio.
            if (distance > filter.radiusKm!!) return false
        }
        // Cumple todos los filtros.
        return true
    }

    // Publicaciones de un autor (sin las eliminadas), de la más reciente a la más antigua.
    override suspend fun getByAuthor(authorId: String): Result<List<Post>> = runCatching {
        // Lee todas las del autor.
        byAuthor(authorId)
            // Quita las eliminadas.
            .filter { it.status != PostStatus.DELETED }
            // Ordena en memoria de la más reciente a la más antigua.
            .sortedByDescending { it.createdAt }
    }

    // Lee todas las publicaciones de un autor (consulta de un solo campo, sin índice compuesto).
    private suspend fun byAuthor(authorId: String): List<Post> =
        posts.whereEqualTo("authorId", authorId).get().await().toModels()

    // Cuenta las publicaciones de un autor con un estado y, opcionalmente, un estado de verificación.
    override suspend fun countByAuthor(
        // Autor.
        authorId: String,
        // Estado de la publicación a contar.
        status: PostStatus,
        // Estado de verificación a contar (null = cualquiera).
        verificationStatus: VerificationStatus?
    ): Int = runCatching {
        // Cuenta en memoria las que cumplen ambas condiciones.
        byAuthor(authorId).count {
            it.status == status && (verificationStatus == null || it.verification.status == verificationStatus)
        }
    // Ante cualquier error devuelve 0.
    }.getOrDefault(0)

    // Actualiza el contenido editable de una publicación.
    override suspend fun updateContent(post: Post): Result<Unit> = runCatching {
        // Solo toca el contenido editable: no pisa contadores ni estado.
        val document = post.toFirestore()
        // Actualiza únicamente los campos de EDITABLE_FIELDS con sus nuevos valores.
        posts.document(post.id).update(EDITABLE_FIELDS.associateWith { document[it] }).await()
        // Resultado exitoso.
        Unit
    // Traduce NOT_FOUND al error del dominio.
    }.mapNotFound()

    // Cambia el estado de la publicación (por ejemplo, finalizada o eliminada).
    override suspend fun updateStatus(id: String, status: PostStatus, finishedAt: Long?): Result<Unit> = runCatching {
        // Actualiza solo los campos de estado.
        posts.document(id).update(mapOf(
            // Nuevo estado.
            "status" to status.name,
            // Fecha de finalización (null si no aplica).
            "finishedAt" to finishedAt,
            // Fecha de la última modificación.
            "updatedAt" to System.currentTimeMillis()
        )).await()
        // Resultado exitoso.
        Unit
    // Traduce NOT_FOUND al error del dominio.
    }.mapNotFound()

    // Marca como eliminadas todas las publicaciones activas de un autor (al eliminar su cuenta).
    override suspend fun deleteAllByAuthor(authorId: String): Result<Unit> = runCatching {
        // Repetirlo no hace daño: solo cierra las que siguen en proceso.
        val now = System.currentTimeMillis()
        // Obtiene las publicaciones activas del autor.
        val active = posts.whereEqualTo("authorId", authorId)
            .whereEqualTo("status", PostStatus.IN_PROGRESS.name)
            .get().await().documents
        // Un lote acepta hasta 500 escrituras.
        active.chunked(BATCH_LIMIT).forEach { chunk ->
            // Crea un lote con una actualización por publicación.
            db.batch().apply {
                // Borrado lógico: cambia el estado a DELETED y la fecha de modificación.
                chunk.forEach { update(it.reference, mapOf("status" to PostStatus.DELETED.name, "updatedAt" to now)) }
            // Confirma el lote.
            }.commit().await()
        }
    }

    // Cola de moderación: publicaciones sin verificar o en revisión, de la más antigua a la más nueva.
    override suspend fun getPendingModeration(limit: Int): Result<List<Post>> = runCatching {
        // Consulta las publicaciones sin verificar o en proceso de verificación.
        posts.whereIn("verification.status", listOf(VerificationStatus.UNVERIFIED.name, VerificationStatus.IN_PROCESS.name))
            .get().await().toModels<Post>()
            // Solo las que siguen activas.
            .filter { it.status == PostStatus.IN_PROGRESS }
            // Primero las más antiguas (se atienden en orden de llegada).
            .sortedBy { it.createdAt }
            // Recorta al límite pedido.
            .take(limit)
    }

    // Publicaciones que un moderador tiene actualmente en revisión.
    override suspend fun getInReviewBy(moderatorId: String): Result<List<Post>> = runCatching {
        // Filtra las activas que siguen en proceso de verificación.
        reviewedBy(moderatorId).filter {
            it.status == PostStatus.IN_PROGRESS && it.verification.status == VerificationStatus.IN_PROCESS
        }
    }

    // Publicaciones que un moderador ya revisó (historial).
    override suspend fun getReviewedBy(moderatorId: String): Result<List<Post>> = runCatching {
        // Excluye las eliminadas y las que aún no tienen revisión.
        reviewedBy(moderatorId).filter {
            it.status != PostStatus.DELETED && it.verification.status != VerificationStatus.UNVERIFIED
        }
    }

    // Lee las publicaciones asignadas a un moderador.
    private suspend fun reviewedBy(moderatorId: String): List<Post> =
        posts.whereEqualTo("verification.reviewedBy", moderatorId).get().await().toModels()

    // Cuenta publicaciones para las estadísticas de moderación según varios criterios opcionales.
    override suspend fun countForModeration(
        // Estados de verificación a contar.
        statuses: Set<VerificationStatus>,
        // Prioridades a contar (null = todas).
        priorities: Set<PriorityLevel>?,
        // Moderador que las revisó (null = cualquiera).
        reviewedBy: String?,
        // Fecha mínima de revisión (null = sin límite).
        reviewedSince: Long?
    ): Int = runCatching {
        // Sin estados no hay nada que contar.
        if (statuses.isEmpty()) return@runCatching 0
        // Firestore filtra por estado (o por moderador); lo demás se cuenta en la app.
        val query = if (reviewedBy != null) posts.whereEqualTo("verification.reviewedBy", reviewedBy)
        else posts.whereIn("verification.status", statuses.map { it.name })
        // Ejecuta la consulta y cuenta en memoria las que cumplen todos los criterios.
        query.get().await().toModels<Post>().count {
            // Atajo a los datos de verificación.
            val v = it.verification
            // No eliminada, con estado, prioridad, moderador y fecha dentro de los criterios.
            it.status != PostStatus.DELETED &&
                v.status in statuses &&
                (priorities == null || v.priorityLevel in priorities) &&
                (reviewedBy == null || v.reviewedBy == reviewedBy) &&
                (reviewedSince == null || (v.reviewedAt ?: 0L) >= reviewedSince)
        }
    // Ante cualquier error devuelve 0.
    }.getOrDefault(0)

    // Actualiza la verificación solo si sigue en el estado esperado (control de concurrencia optimista).
    override suspend fun updateVerification(
        // Id de la publicación.
        id: String,
        // Estado de verificación que se espera encontrar.
        expected: VerificationStatus,
        // Nueva verificación a guardar.
        verification: Verification
    ): Result<Unit> = runCatching {
        // Referencia a la publicación.
        val ref = posts.document(id)
        // Leer, comparar y escribir en una transacción: dos moderadores no deciden la misma publicación.
        db.runTransaction { tx ->
            // Lee la publicación; si no existe, falla.
            val current = tx.get(ref).toModel<Post>() ?: throw PostException.PostNotFound
            // Si otro moderador ya cambió el estado, falla.
            if (current.verification.status != expected) throw PostException.AlreadyReviewed
            // Guarda la nueva verificación y la fecha de modificación.
            tx.update(ref, mapOf(
                "verification" to verification.toFirestore(),
                "updatedAt" to System.currentTimeMillis()
            ))
        // Espera a que la transacción termine.
        }.await()
        // Resultado exitoso.
        Unit
    // Extrae el error de dominio original si la transacción lo envolvió.
    }.unwrapTransactionError()

    // Guarda el resultado del análisis de IA en la publicación.
    override suspend fun updateAiAnalysis(
        // Id de la publicación.
        id: String,
        // Revisión generada por la IA.
        review: AiReview,
        // Prioridad sugerida.
        priority: PriorityLevel,
        // Motivo de la prioridad.
        priorityReason: String
    ): Result<Unit> = runCatching {
        // Solo la revisión y la prioridad sugerida: el estado de verificación no cambia.
        posts.document(id).update(mapOf(
            // Revisión completa de la IA.
            "aiReview" to review.toFirestore(),
            // Prioridad dentro de la verificación (la usa la cola del moderador).
            "verification.priorityLevel" to priority.name,
            // Motivo de la prioridad.
            "verification.priorityReason" to priorityReason
        )).await()
        // Resultado exitoso.
        Unit
    // Traduce NOT_FOUND al error del dominio.
    }.mapNotFound()

    // `update` sobre un documento que no existe falla con NOT_FOUND; aquí se traduce a PostNotFound.
    private fun <T> Result<T>.mapNotFound(): Result<T> = recoverCatching { error ->
        // Lanza el error del dominio si no existía; si no, relanza el error original.
        throw if (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.NOT_FOUND)
            PostException.PostNotFound else error
    }

    // Constantes privadas de la clase.
    private companion object {
        // Cantidad de publicaciones que se leen por tanda en el feed.
        const val FEED_BATCH_SIZE = 20L
        // Máximo de tandas por página del feed (tope de lecturas).
        const val MAX_FEED_BATCHES = 5
        // Escrituras por lote (por debajo del límite de 500 de Firestore).
        const val BATCH_LIMIT = 450

        // Campos que puede cambiar la edición de una publicación (nunca contadores ni estado).
        val EDITABLE_FIELDS = listOf(
            "title", "categoryId", "description", "animalTypeId", "breed", "size", "ageInMonths",
            "vaccinated", "neutered", "photoUrls", "location", "verification", "aiReview", "updatedAt"
        )
    }
}

// Paquete de las excepciones del dominio; sirve para reconocerlas dentro de la cadena de causas.
private const val DOMAIN_ERRORS_PACKAGE = "co.edu.uniquindio.petadoptionapp.domain.error"

/**
Una excepción lanzada dentro de una transacción llega envuelta en otra de Firestore.
Esto devuelve la original (por ejemplo, PostException.AlreadyReviewed) para que la UI la entienda.
Es `internal` porque la comparten todos los repositorios Firestore que usan transacciones.
**/
internal fun <T> Result<T>.unwrapTransactionError(): Result<T> = recoverCatching { error ->
    // Empieza revisando el error recibido.
    var cause: Throwable? = error
    // Recorre la cadena de causas.
    while (cause != null) {
        // Si encuentra una excepción del dominio (no de Firestore), la relanza tal cual.
        if (cause !is FirebaseFirestoreException && cause.javaClass.name.startsWith(DOMAIN_ERRORS_PACKAGE)) throw cause
        // Pasa a la causa siguiente.
        cause = cause.cause
    }
    // Si no había error del dominio, relanza el original.
    throw error
}
