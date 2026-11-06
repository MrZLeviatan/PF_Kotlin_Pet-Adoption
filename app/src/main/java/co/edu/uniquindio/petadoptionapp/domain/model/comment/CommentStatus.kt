package co.edu.uniquindio.petadoptionapp.domain.model.comment

/**
Modelo de dominio: estado de un comentario ([Comment]).

Se usa borrado lógico: un comentario eliminado NO se borra del todo,
porque podría tener respuestas colgando. Se conserva el documento y la
interfaz muestra "Comentario eliminado".
Lo consultan los casos de uso de comment y la lista de comentarios de la UI.
**/
// Enumera los estados posibles de un comentario.
enum class CommentStatus {
    // Visible con normalidad.
    ACTIVE,

    // Eliminado por su autor o por un moderador.
    DELETED
}
