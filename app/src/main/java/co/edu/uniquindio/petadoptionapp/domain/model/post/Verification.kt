package co.edu.uniquindio.petadoptionapp.domain.model.post

import kotlinx.serialization.Serializable

/**
Modelo de dominio: resultado del análisis de IA y de la revisión del moderador.

Toda publicación nace con una verificación vacía (`UNVERIFIED`). La IA
sugiere una prioridad y la explica; el moderador decide el estado final.
La IA ayuda a ordenar la cola de revisión, pero NO verifica por sí sola.
Va embebido en [Post.verification] y lo modifican StartReview, ApprovePost,
RejectPost y AnalyzePost.
**/
// Permite convertir la verificación a/desde el formato de almacenamiento.
@Serializable
// Declara la verificación como clase de datos inmutable.
data class Verification(

    // Proceso de verificación.
    val status: VerificationStatus = VerificationStatus.UNVERIFIED,
    // Prioridad sugerida por la IA.
    val priorityLevel: PriorityLevel? = null,
    // Explicación de la IA sobre esa prioridad.
    val priorityReason: String = "",
    // Motivo escrito por el moderador por si el POST es rechazado.
    val rejectionReason: String? = null,
    // Id del moderador que la revisó.
    val reviewedBy: String? = null,
    // Momento de la revisión, en milisegundos (epoch).
    val reviewedAt: Long? = null
) {

    // Devuelve una copia aprobada por el moderador indicado.
    fun approve(moderatorId: String): Verification = copy(
        // Cambia el estado a verificada.
        status = VerificationStatus.VERIFIED,
        // Limpia cualquier motivo de rechazo anterior.
        rejectionReason = null,
        // Registra al moderador que aprobó.
        reviewedBy = moderatorId,
        // Registra el momento de la aprobación.
        reviewedAt = System.currentTimeMillis()
    )

    /**
     Devuelve una copia rechazada. Exige un motivo no vacío, como pide el
     enunciado; si está vacío lanza [IllegalArgumentException].
     */
    fun reject(moderatorId: String, reason: String): Verification {
        // Exige que el motivo del rechazo no esté vacío.
        require(reason.isNotBlank()) { "El rechazo requiere un motivo" }
        // Devuelve la copia rechazada.
        return copy(
            // Cambia el estado a rechazada.
            status = VerificationStatus.REJECTED,
            // Guarda el motivo sin espacios sobrantes.
            rejectionReason = reason.trim(),
            // Registra al moderador que rechazó.
            reviewedBy = moderatorId,
            // Registra el momento del rechazo.
            reviewedAt = System.currentTimeMillis()
        )
    }

    /**
     Devuelve una copia marcada "en proceso": un moderador la está revisando.
     */
    fun startReview(moderatorId: String): Verification = copy(
        // Cambia el estado a en revisión.
        status = VerificationStatus.IN_PROCESS,
        // Asigna la publicación al moderador que la tomó.
        reviewedBy = moderatorId
    )
}
