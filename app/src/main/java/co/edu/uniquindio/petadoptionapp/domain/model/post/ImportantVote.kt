package co.edu.uniquindio.petadoptionapp.domain.model.post

/**
 Voto "Es importante" de una persona sobre una publicación.

 El ID del documento se construye con [buildId] ("postId_userId"), lo que
 garantiza a nivel de base de datos que una persona vote UNA sola vez
 por publicación: un segundo clic apunta al mismo documento.

 Se guarda en la colección `importantVotes`.
 */
data class ImportantVote(
    // Publicación votada.
    val postId: String = "",
    // Persona que votó (`Account.id`).
    val userId: String = "",
    // Momento del voto, en milisegundos (epoch).
    val createdAt: Long = System.currentTimeMillis()
) {

    companion object {
        // Id determinista del documento: un voto por persona y publicación.
        fun buildId(postId: String, userId: String): String = "${postId}_$userId"
    }
}