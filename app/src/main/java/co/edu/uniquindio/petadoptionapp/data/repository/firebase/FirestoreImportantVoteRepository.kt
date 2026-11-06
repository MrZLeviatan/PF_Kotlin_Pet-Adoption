package co.edu.uniquindio.petadoptionapp.data.repository.firebase

import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.IMPORTANT_VOTES
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.MAX_IN_VALUES
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.POSTS
import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.post.ImportantVote
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.repository.ImportantVoteRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/**
Votos "Es importante" en `importantVotes/{postId_userId}`; el contador vive en `importantCount` del post.

Implementa ImportantVoteRepository del dominio y se enlaza en RepositoryModule. El id del voto
combina publicación y usuario, así una persona solo puede tener un voto por publicación.
Lo usan los casos de uso de votar, el detalle de la publicación (para pintar el botón) y la
pantalla "Me gusta" (MyLikes) para listar las publicaciones marcadas.
**/
// Una sola instancia compartida en toda la app.
@Singleton
// Hilt inyecta Firestore desde FirebaseModule.
class FirestoreImportantVoteRepository @Inject constructor(
    // Base de datos Firestore.
    private val db: FirebaseFirestore,
) : ImportantVoteRepository {

    // Referencia a la colección de votos.
    private val votes = db.collection(IMPORTANT_VOTES)

    // Marca o desmarca el voto; devuelve true si quedó votado y false si se quitó.
    override suspend fun toggle(postId: String, userId: String): Result<Boolean> = runCatching {
        // Referencia a la publicación votada.
        val postRef = db.collection(POSTS).document(postId)
        // Referencia al voto con id determinista postId_userId.
        val voteRef = votes.document(ImportantVote.buildId(postId, userId))
        // Transacción: el voto y el contador cambian juntos o no cambia nada (evita contadores desfasados).
        db.runTransaction { tx ->
            // Si la publicación no existe, no se guarda ningún voto.
            val post = tx.get(postRef).toModel<Post>() ?: throw PostException.PostNotFound
            // Revisa si el usuario ya había votado.
            val alreadyVoted = tx.get(voteRef).exists()
            // Si ya votó, borra el voto; si no, lo crea.
            if (alreadyVoted) tx.delete(voteRef) else tx.set(voteRef, ImportantVote(postId, userId).toFirestore())
            // Calcula el nuevo contador (resta o suma uno) sin permitir valores negativos.
            val newCount = (post.importantCount + if (alreadyVoted) -1 else 1).coerceAtLeast(0)
            // Guarda el nuevo contador en la publicación.
            tx.update(postRef, "importantCount", newCount)
            // Devuelve el nuevo estado: votado si antes no lo estaba.
            !alreadyVoted
        // Espera a que la transacción termine.
        }.await()
    // Extrae el error de dominio original si la transacción lo envolvió.
    }.unwrapTransactionError()

    // Indica si el usuario ya votó por una publicación.
    override suspend fun hasVoted(postId: String, userId: String): Result<Boolean> = runCatching {
        // Lee el documento del voto y comprueba si existe.
        votes.document(ImportantVote.buildId(postId, userId)).get().await().exists()
    }

    // De una lista de publicaciones, devuelve los ids que el usuario votó (para pintar varias tarjetas a la vez).
    override suspend fun getVotedAmong(userId: String, postIds: List<String>): Result<Set<String>> = runCatching {
        // Quita repetidos y parte la lista en lotes de 30 (límite de whereIn); procesa cada lote.
        postIds.distinct().chunked(MAX_IN_VALUES).flatMap { chunk ->
            // Filtra los votos del usuario.
            votes.whereEqualTo("userId", userId)
                // Solo los de las publicaciones de este lote.
                .whereIn("postId", chunk)
                // Ejecuta la consulta y espera el resultado.
                .get().await()
                // Convierte los documentos en modelos.
                .toModels<ImportantVote>()
                // Se queda solo con el id de la publicación.
                .map { it.postId }
        // Une los resultados de todos los lotes en un conjunto.
        }.toSet()
    }

    // Lista paginada de publicaciones votadas por el usuario.
    override suspend fun getVotedPostIds(userId: String, limit: Int, afterPostId: String?): Result<List<String>> = runCatching {
        // De la más reciente a la más antigua.
        val query = votes.whereEqualTo("userId", userId).orderBy("createdAt", Query.Direction.DESCENDING)
        // Si se pidió la página siguiente, obtiene el documento del último voto mostrado como cursor.
        val cursor = afterPostId?.let { id ->
            // Lee el voto de referencia; si ya no existe, no hay más páginas que devolver.
            votes.document(ImportantVote.buildId(id, userId)).get().await().takeIf { it.exists() }
                ?: return@runCatching emptyList()
        }
        // Empieza después del cursor si existe; si no, desde el principio.
        (cursor?.let { query.startAfter(it) } ?: query)
            // Limita la cantidad de resultados de la página.
            .limit(limit.toLong())
            // Ejecuta la consulta.
            .get().await()
            // Convierte los documentos en votos.
            .toModels<ImportantVote>()
            // Devuelve solo los ids de las publicaciones.
            .map { it.postId }
    }
}
