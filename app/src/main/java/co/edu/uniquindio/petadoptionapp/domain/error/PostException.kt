package co.edu.uniquindio.petadoptionapp.domain.error

/**
Errores del dominio de publicaciones: formato, permisos y estado.
La UI traduce cada uno a un texto amigable usando strings.xml.
 */
sealed class PostException : Exception() {

    // Errores de formato

    // Ocurre cuando el título está vacío, es muy corto o muy largo.
    object InvalidTitle : PostException()

    // Ocurre cuando la descripción está vacía, es muy corta o muy larga.
    object InvalidDescription : PostException()

    // Ocurre cuando no se eligió una categoría.
    object InvalidCategory : PostException()

    // Ocurre cuando la publicación no tiene ninguna foto.
    object NoPhotos : PostException()

    // Ocurre cuando se superó el máximo de fotos permitido.
    object TooManyPhotos : PostException()

    // Ocurre cuando la edad aproximada está fuera del rango permitido.
    object InvalidAge : PostException()

    // Ocurre cuando no se eligió un punto válido en el mapa (o el filtro de cercanía está incompleto).
    object InvalidLocation : PostException()


    // Errores de existencia y permisos

    // Ocurre cuando la publicación no existe o la persona no tiene derecho a verla.
    object PostNotFound : PostException()

    // Ocurre cuando la persona no tiene permiso para hacer esa acción sobre la publicación.
    object NotAllowed : PostException()

    // Ocurre cuando una acción exclusiva de moderadores la intenta otra persona.
    object NotModerator : PostException()


    // Errores de estado

    // Ocurre cuando la publicación ya no está en proceso (finalizada o eliminada).
    object PostNotActive : PostException()

    // Ocurre cuando la publicación ya fue verificada o rechazada.
    object AlreadyReviewed : PostException()

    // Ocurre cuando el moderador rechaza sin escribir un motivo válido.
    object RejectionReasonRequired : PostException()


    // Ocurre cuando otro moderador tiene la publicación en revisión.
    object ReviewedByAnotherModerator : PostException()
}