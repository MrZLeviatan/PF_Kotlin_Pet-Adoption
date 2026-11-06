package co.edu.uniquindio.petadoptionapp.domain.error

/**
Errores del dominio de solicitudes de adopción.
Los lanzan los casos de uso de chat/adoption (enviar, responder y cancelar solicitudes)
y viajan dentro de un Result.failure hasta la capa de presentación.
La UI traduce cada uno a un texto amigable usando strings.xml.
**/
// Jerarquía sellada: el compilador conoce todos los errores posibles de adopción.
sealed class AdoptionException : Exception() {

    // Ocurre cuando alguien intenta adoptar su propia publicación.
    object CannotRequestOwnPost : AdoptionException()

    // Ocurre cuando la categoría de la publicación no admite solicitudes de adopción.
    object NotAdoptable : AdoptionException()

    // Ocurre cuando la persona ya tiene una solicitud vigente para esta publicación.
    object AlreadyRequested : AdoptionException()

    // Ocurre cuando la solicitud no existe o la persona no tiene relación con ella.
    object RequestNotFound : AdoptionException()

    // Ocurre cuando la solicitud ya fue aceptada, rechazada o cancelada.
    object AlreadyResponded : AdoptionException()

    // Ocurre cuando el mensaje opcional de la solicitud supera el máximo.
    object InvalidMessage : AdoptionException()
}
