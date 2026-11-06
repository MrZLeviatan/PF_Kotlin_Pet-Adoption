package co.edu.uniquindio.petadoptionapp.domain.model.post

import kotlinx.serialization.Serializable

/**
Modelo de dominio: voto "Es importante" de una persona sobre una publicación.

El ID del documento se construye con [buildId] ("postId_userId"), lo que
garantiza a nivel de base de datos que una persona vote UNA sola vez
por publicación: un segundo clic apunta al mismo documento.

Se guarda en la colección `importantVotes` de Firestore.
Lo usan ImportantVoteRepository y los casos de uso de usecase/importantVote.
**/
// Permite convertir el voto a/desde el formato de almacenamiento.
@Serializable
// Declara el voto como clase de datos inmutable.
data class ImportantVote(
    // Publicación votada.
    val postId: String = "",
    // Persona que votó (`Account.id`).
    val userId: String = "",
    // Momento del voto, en milisegundos (epoch).
    val createdAt: Long = System.currentTimeMillis()
) {

    // Utilidades compartidas sin necesidad de instanciar la clase.
    companion object {
        // Id determinista del documento: un voto por persona y publicación.
        fun buildId(postId: String, userId: String): String = "${postId}_$userId"
    }
}
