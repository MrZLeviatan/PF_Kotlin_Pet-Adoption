package co.edu.uniquindio.petadoptionapp.domain.model.adoption


/**
Modelo de dominio: estado de una solicitud de adopción ([AdoptionRequest]).
Lo consultan los casos de uso de chat/adoption para saber si una solicitud
todavía puede responderse o cancelarse, y la UI para mostrar su etiqueta.
**/
// Enumera los estados posibles de una solicitud.
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
