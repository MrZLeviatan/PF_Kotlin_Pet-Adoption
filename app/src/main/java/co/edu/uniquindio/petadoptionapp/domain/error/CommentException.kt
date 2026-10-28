package co.edu.uniquindio.petadoptionapp.domain.error

/**
Errores del dominio de comentarios: formato, permisos y estado.
La UI traduce cada uno a un texto amigable usando strings.xml.
 */
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