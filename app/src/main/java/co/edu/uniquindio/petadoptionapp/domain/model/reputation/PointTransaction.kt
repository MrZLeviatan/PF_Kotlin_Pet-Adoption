package co.edu.uniquindio.petadoptionapp.domain.model.reputation

/**
Registro de puntos otorgados. Se guarda en la colección `pointTransactions`.
El id se construye con [buildId] ("usuario_motivo_origen"): como Firestore no permite dos documentos con el mismo id,
un mismo hecho solo puede dar puntos una vez, aunque la acción se repita.

 */
data class PointTransaction(
    val id: String = "",
    val userId: String = "",
    val reason: PointReason = PointReason.COMMENT_CREATED,
    val points: Int = 0,
    val postId: String? = null,
    val actorId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
        Id determinista. [sourceId] identifica el hecho que da los puntos:
        el id de la publicación (verificada, finalizada), del comentario, o "postId_votanteId" para los votos.
         */
        fun buildId(userId: String, reason: PointReason, sourceId: String): String =
            "${userId}_${reason.name}_$sourceId"
    }
}