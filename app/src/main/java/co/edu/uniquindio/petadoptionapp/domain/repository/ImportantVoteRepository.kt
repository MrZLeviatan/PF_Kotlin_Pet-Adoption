package co.edu.uniquindio.petadoptionapp.domain.repository


/**
Contrato de los votos "Es importante" guardados en la colección `importantVotes` de Firestore.
Los permisos y la visibilidad de la publicación viven en los casos de uso, aquí solo se ejecuta.
 */
interface ImportantVoteRepository {

    // Votar

    /**
    Alterna el voto "Es importante" de una persona sobre una publicación.

    - Qué envía: El [postId] y el [userId].
    - Qué recibe: Un [Result] con `true` si quedó votada, o `false` si se quitó el voto.
    En una transacción: crea o borra el documento `ImportantVote` (id fijo `postId_userId`) y ajusta `importantCount` del post.
    Como el id es fijo, un doble clic nunca crea dos votos.
     */
    suspend fun toggle(postId: String, userId: String): Result<Boolean>


    // Consultar

    /**
    Verifica si una persona ya votó una publicación.

    - Qué envía: El [postId] y el [userId].
    - Qué recibe: Un [Result] con `true` si ya votó.
    Es una lectura de un solo documento (la más barata de Firestore).
     */
    suspend fun hasVoted(postId: String, userId: String): Result<Boolean>

    /**
    Obtiene, de una lista de publicaciones, a cuáles votó la persona.

    - Qué envía: El [userId] y los [postIds] que se muestran en pantalla.
    - Qué recibe: Un [Result] con el conjunto de ids votados.
    Pinta todos los botones de una página del feed con una sola consulta. La implementación parte la lista en grupos por el límite de Firestore.
     */
    suspend fun getVotedAmong(userId: String, postIds: List<String>): Result<Set<String>>

    /**
    Obtiene una página de los ids de publicaciones que la persona marcó, de la más reciente a la más antigua.

    - Qué envía: El [userId], el [limit] y, opcionalmente, el [afterPostId] del último ya mostrado.
    - Qué recibe: Un [Result] con la lista de ids.
     */
    suspend fun getVotedPostIds(userId: String, limit: Int, afterPostId: String? = null): Result<List<String>>
}