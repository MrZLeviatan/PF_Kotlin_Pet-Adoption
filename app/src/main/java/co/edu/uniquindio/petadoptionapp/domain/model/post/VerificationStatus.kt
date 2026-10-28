package co.edu.uniquindio.petadoptionapp.domain.model.post


/**
 Estado del proceso de verificación hecho por un moderador.
 */
enum class VerificationStatus {
    // Recién creada; nadie la ha revisado.
    UNVERIFIED,

    //Un moderador la está revisando.
    IN_PROCESS,

    // Aprobada: aparece en el feed público.
    VERIFIED,

    // Rechazada: exige motivo y no aparece en el feed.
    REJECTED
}