package co.edu.uniquindio.petadoptionapp.domain.model.post


/**
Modelo de dominio: ciclo de vida de una publicación ([Post.status]).

Es independiente de la verificación: una publicación puede estar
verificada y luego finalizada.
Lo cambian los casos de uso FinishPost y DeletePost, y lo consultan el feed y los dashboards.
**/
// Enumera los estados del ciclo de vida.
enum class PostStatus {
    // Activa: el caso sigue abierto.
    IN_PROGRESS,

    // Resuelta: la mascota fue adoptada, encontrada, etc.
    FINISHED,

    // Borrado lógico: el documento se conserva, pero no se muestra.
    DELETED
}
