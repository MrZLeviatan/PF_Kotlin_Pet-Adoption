package co.edu.uniquindio.petadoptionapp.domain.model.comment

/**
 Estado de un comentario.

 Se usa borrado lógico: un comentario eliminado NO se borra del todo,
 porque podría tener respuestas colgando. Se conserva el documento y la
 interfaz muestra "Comentario eliminado".
 */
enum class CommentStatus {
    /** Visible con normalidad. */
    ACTIVE,

    /** Eliminado por su autor o por un moderador. */
    DELETED
}