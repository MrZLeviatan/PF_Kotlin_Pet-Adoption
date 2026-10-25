package co.edu.uniquindio.petadoptionapp.domain.repository

import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.filter.PostFilter
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.Verification
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus

/**
Contrato de publicaciones guardadas en Firestore.
Las validaciones de formato y las reglas de permisos viven en los casos de uso, aquí solo se ejecuta.
 */
interface PostRepository {

    // Crear y consultar

    /**
    Guarda una publicación nueva.

    - Qué envía ([post]): La publicación ya validada, con el `id` vacío.
    - Qué recibe: Un [Result] con la publicación guardada, ya con el `id` que generó Firestore.
     */
    suspend fun create(post: Post): Result<Post>

    /**
    Busca una publicación.

    - Qué envía ([id]): El identificador de la publicación.
    - Qué recibe: El [Post] o `null` si no existe. No encontrarlo es un caso normal, no un error.
     */
    suspend fun findById(id: String): Post?

    /**
    Busca varias publicaciones a la vez (por ejemplo, las marcadas "Es importante").

    - Qué envía ([ids]): Los identificadores a buscar.
    - Qué recibe: Las publicaciones encontradas.
    Firestore limita las consultas por lista; la implementación las parte en grupos.
     */
    suspend fun findByIds(ids: List<String>): List<Post>


    // Feed y listados

    /**
    Obtiene una página del feed público.

    - Qué envía: El [filter] (categoría, animal, ciudad, cercanía, orden), el [limit] de resultados y, opcionalmente, el [afterPostId] de la última publicación ya mostrada.
    - Qué recibe: Un [Result] con la lista de publicaciones.
    Solo devuelve publicaciones VERIFIED y en proceso. Firestore no mide distancias, así que para "cercanos" la implementación consulta un rango de latitud y refina el resto en la app.
     */
    suspend fun getFeed(filter: PostFilter, limit: Int, afterPostId: String? = null): Result<List<Post>>

    /**
    Obtiene las publicaciones de una persona (mis publicaciones).

    - Qué envía ([authorId]): El identificador del autor.
    - Qué recibe: Un [Result] con sus publicaciones en cualquier estado, excepto las eliminadas.
     */
    suspend fun getByAuthor(authorId: String): Result<List<Post>>

    /**
    Cuenta las publicaciones de una persona (alimenta el dashboard).

    - Qué envía: El [authorId] y los filtros opcionales [status] y [verificationStatus].
    - Qué recibe: La cantidad que cumple.
    Un conteo cuesta mucho menos que descargar los documentos.
     */
    suspend fun countByAuthor(
        authorId: String,
        status: PostStatus,
        verificationStatus: VerificationStatus? = null
    ): Int


    // Modificar

    /**
    Actualiza el contenido editable de una publicación.

    - Qué envía ([post]): La publicación con los datos nuevos (título, categoría, descripción, datos del animal, fotos, ubicación) y su verificación reiniciada.
    - Qué recibe: Un [Result<Unit>] que confirma si se guardó.
    Solo toca esos campos: no pisa los contadores (votos y comentarios), que cambian por otras acciones.
     */
    suspend fun updateContent(post: Post): Result<Unit>

    /**
    Cambia el estado de la publicación.

    - Qué envía: El [id], el nuevo [status] y el momento [finishedAt] (solo si se finaliza).
    - Qué recibe: Un [Result<Unit>] que confirma si se guardó.
    Sirve para finalizar y para el borrado lógico (DELETED).
     */
    suspend fun updateStatus(id: String, status: PostStatus, finishedAt: Long?): Result<Unit>

    /**
    Cierra todas las publicaciones activas de una cuenta (las pasa a DELETED).

    - Qué envía ([authorId]): El identificador del autor.
    - Qué recibe: Un [Result<Unit>] que confirma si se aplicó.
    Se usa al eliminar una cuenta, para que nadie contacte a alguien que ya no existe.
     */
    suspend fun deleteAllByAuthor(authorId: String): Result<Unit>


    // Moderación

    /**
    Obtiene las publicaciones pendientes de revisión.

    - Qué recibe: Un [Result] con las publicaciones sin verificar o en proceso, de la más antigua a la más nueva.
    La prioridad de la IA se ordena en el caso de uso, porque Firestore no ordena enums por importancia.
     */
    suspend fun getPendingModeration(limit: Int): Result<List<Post>>


    /**
    Guarda el resultado de la revisión solo si el estado actual sigue siendo el esperado.

    - Qué envía: El [id], el estado que se leyó ([expected]) y la [verification] nueva.
    - Qué recibe: Un [Result<Unit>]; falla con AlreadyReviewed si otro moderador cambió el estado antes.
    La implementación lo hace en una transacción de Firestore (leer, comparar y escribir).
     */
    suspend fun updateVerification(id: String, expected: VerificationStatus, verification: Verification): Result<Unit>
}