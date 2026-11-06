package co.edu.uniquindio.petadoptionapp.domain.error

/**
Errores del dominio de comentarios: formato, permisos y estado.
Los lanzan CommentValidator y los casos de uso de usecase/comment (crear, editar,
borrar y dar "me gusta"). La UI traduce cada uno a un texto amigable usando strings.xml.
**/
// Jerarquía sellada con los errores posibles de los comentarios.
sealed class CommentException : Exception() {

    // Ocurre cuando el texto está vacío o supera el máximo de caracteres.
    object InvalidText : CommentException()

    // Ocurre cuando el comentario no existe.
    object CommentNotFound : CommentException()

    // Ocurre cuando se intenta responder a un comentario eliminado.
    object CannotReplyDeleted : CommentException()

    // Ocurre cuando la persona no es el autor (ni moderador) del comentario.
    object NotAllowed : CommentException()
}
