package co.edu.uniquindio.petadoptionapp.domain.model.reputation

import kotlinx.serialization.Serializable

/**
Modelo de dominio: registro de puntos otorgados. Se guarda en la colección `pointTransactions` de Firestore.
El id se construye con [buildId] ("usuario_motivo_origen"): como Firestore no permite dos documentos con el mismo id,
un mismo hecho solo puede dar puntos una vez, aunque la acción se repita.
Lo crea ReputationRepository al otorgar puntos y lo lee GetMyPointHistoryUseCase.
**/
// Permite convertir la transacción a/desde el formato de almacenamiento.
@Serializable
// Declara la transacción de puntos como clase de datos inmutable.
data class PointTransaction(
    // Identificador determinista ("usuario_motivo_origen").
    val id: String = "",
    // Persona que recibe los puntos.
    val userId: String = "",
    // Motivo por el que se otorgaron.
    val reason: PointReason = PointReason.COMMENT_CREATED,
    // Cantidad de puntos otorgados.
    val points: Int = 0,
    // Publicación relacionada, si aplica.
    val postId: String? = null,
    // Persona que provocó el premio (por ejemplo, quien votó), si aplica.
    val actorId: String? = null,
    // Momento del registro, en milisegundos (epoch).
    val createdAt: Long = System.currentTimeMillis()
) {
    // Utilidades compartidas sin necesidad de instanciar la clase.
    companion object {
        /**
        Id determinista. [sourceId] identifica el hecho que da los puntos:
        el id de la publicación (verificada, finalizada), del comentario, o "postId_votanteId" para los votos.
         */
        fun buildId(userId: String, reason: PointReason, sourceId: String): String =
            // Une usuario, nombre del motivo y origen con guiones bajos.
            "${userId}_${reason.name}_$sourceId"
    }
}
