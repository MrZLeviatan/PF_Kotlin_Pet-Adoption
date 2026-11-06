package co.edu.uniquindio.petadoptionapp.domain.model.users.profile

import kotlinx.serialization.Serializable

/**
Motivo opcional que la persona da al eliminar su cuenta.
No guarda datos personales: solo sirve para estadísticas.
 */
@Serializable
data class AccountDeletionFeedback(
    val id: String = "",
    // Motivo elegido de una lista (ej. "No uso la app", "Encontré mascota").
    val reason: DeletionReason = DeletionReason.OTHER,
    // Comentario libre, opcional.
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)