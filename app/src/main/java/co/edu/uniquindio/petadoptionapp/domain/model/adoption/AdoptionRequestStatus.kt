package co.edu.uniquindio.petadoptionapp.domain.model.adoption


/**
Estado de una solicitud de adopción.
 */
enum class AdoptionRequestStatus {
    // Enviada; el autor aún no responde.
    PENDING,

    // El autor la aceptó.
    ACCEPTED,

    // El autor la rechazó.
    DECLINED,

    // El solicitante la canceló antes de que se respondiera.
    CANCELLED
}