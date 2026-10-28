package co.edu.uniquindio.petadoptionapp.domain.model.post


/**
 Ciclo de vida de una publicación.

 Es independiente de la verificación: una publicación puede estar
 verificada y luego finalizada.
 */
enum class PostStatus {
    // Activa: el caso sigue abierto.
    IN_PROGRESS,

    // Resuelta: la mascota fue adoptada, encontrada, etc.
    FINISHED,

    // Borrado lógico: el documento se conserva, pero no se muestra.
    DELETED
}