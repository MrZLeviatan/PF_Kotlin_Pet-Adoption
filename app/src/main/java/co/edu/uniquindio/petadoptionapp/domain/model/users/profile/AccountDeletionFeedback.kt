package co.edu.uniquindio.petadoptionapp.domain.model.users.profile

import kotlinx.serialization.Serializable

/**
Modelo de dominio: motivo opcional que la persona da al eliminar su cuenta.
No guarda datos personales: solo sirve para estadísticas.
Lo crea DeleteAccountUseCase y lo guarda UserRepository.
**/
// Permite convertir el comentario de baja a/desde el formato de almacenamiento.
@Serializable
// Declara el motivo de baja como clase de datos inmutable.
data class AccountDeletionFeedback(
    // Identificador del registro.
    val id: String = "",
    // Motivo elegido de una lista (ej. "No uso la app", "Encontré mascota").
    val reason: DeletionReason = DeletionReason.OTHER,
    // Comentario libre, opcional.
    val comment: String = "",
    // Momento en que se registró, en milisegundos (epoch).
    val createdAt: Long = System.currentTimeMillis()
)
