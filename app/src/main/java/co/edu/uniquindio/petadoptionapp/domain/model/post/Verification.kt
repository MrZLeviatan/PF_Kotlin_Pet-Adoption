package co.edu.uniquindio.petadoptionapp.domain.model.post

/**
 Resultado del análisis de IA y de la revisión del moderador.

 Toda publicación nace con una verificación vacía (`UNVERIFIED`). La IA
 sugiere una prioridad y la explica; el moderador decide el estado final.
 La IA ayuda a ordenar la cola de revisión, pero NO verifica por sí sola.
 */
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
        status = VerificationStatus.VERIFIED,
        rejectionReason = null,
        reviewedBy = moderatorId,
        reviewedAt = System.currentTimeMillis()
    )

    /**
     Devuelve una copia rechazada. Exige un motivo no vacío, como pide el
     enunciado; si está vacío lanza [IllegalArgumentException].
     */
    fun reject(moderatorId: String, reason: String): Verification {
        require(reason.isNotBlank()) { "El rechazo requiere un motivo" }
        return copy(
            status = VerificationStatus.REJECTED,
            rejectionReason = reason.trim(),
            reviewedBy = moderatorId,
            reviewedAt = System.currentTimeMillis()
        )
    }
}