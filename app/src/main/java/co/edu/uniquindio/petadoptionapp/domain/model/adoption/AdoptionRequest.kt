package co.edu.uniquindio.petadoptionapp.domain.model.adoption

import kotlinx.serialization.Serializable


/**
Modelo de dominio: solicitud de una persona interesada en adoptar la mascota de una publicación.

Se guarda en la colección `adoptionRequests` de Firestore. El id se construye con
[buildId] ("postId_requesterId"), así una persona solo puede enviar una
solicitud por publicación.

Es inmutable: los cambios de estado devuelven una copia nueva.
La usan los casos de uso de chat/adoption (SendAdoptionInterest, RespondAdoptionRequest,
CancelAdoptionRequest, GetPostAdoptionRequests) y la pantalla de solicitudes.
**/
// Permite convertir la solicitud a/desde el formato de almacenamiento.
@Serializable
// Declara la solicitud de adopción como clase de datos inmutable.
data class AdoptionRequest(
    // Identificador del documento ("postId_requesterId").
    val id: String = "",
    // Publicación a la que se solicita.
    val postId: String = "",
    // Id del autor, quien recibe la solicitud. Se copia para consultar "solicitudes que me llegaron" sin leer cada post.
    val postAuthorId: String = "",
    // Id de quien quiere adoptar.
    val requesterId: String = "",
    // Alias del solicitante, copiado para listar rápido.
    val requesterName: String = "",
    // Mensaje del solicitante al autor.
    val message: String = "",
    // Chat donde se envió el mensaje automático.
    val chatId: String = "",
    // Estado actual.
    val status: AdoptionRequestStatus = AdoptionRequestStatus.PENDING,
    // Momento de envío, en milisegundos (epoch).
    val createdAt: Long = System.currentTimeMillis(),
    // Momento de la respuesta; nulo mientras esté pendiente.
    val respondedAt: Long? = null
) {

    // Indica si la solicitud sigue esperando respuesta (estado pendiente).
    fun awaitsResponse(): Boolean = status == AdoptionRequestStatus.PENDING

    // El autor acepta. Solo es válido si está pendiente.
    fun accept(): AdoptionRequest {
        // Lanza una excepción si la solicitud ya había sido respondida.
        require(awaitsResponse()) { "La solicitud ya fue respondida" }
        // Devuelve una copia aceptada con la fecha de respuesta actual.
        return copy(status = AdoptionRequestStatus.ACCEPTED, respondedAt = System.currentTimeMillis())
    }

    // El autor rechaza. Solo es válido si está pendiente.
    fun decline(): AdoptionRequest {
        // Lanza una excepción si la solicitud ya había sido respondida.
        require(awaitsResponse()) { "La solicitud ya fue respondida" }
        // Devuelve una copia rechazada con la fecha de respuesta actual.
        return copy(status = AdoptionRequestStatus.DECLINED, respondedAt = System.currentTimeMillis())
    }

    // El solicitante cancela. Solo es válido si está pendiente.
    fun cancel(): AdoptionRequest {
        // Lanza una excepción si la solicitud ya había sido respondida.
        require(awaitsResponse()) { "La solicitud ya fue respondida" }
        // Devuelve una copia cancelada con la fecha de respuesta actual.
        return copy(status = AdoptionRequestStatus.CANCELLED, respondedAt = System.currentTimeMillis())
    }

    // Constantes y utilidades compartidas sin necesidad de instanciar la clase.
    companion object {
        // Longitud máxima del mensaje opcional que acompaña la solicitud.
        const val MAX_MESSAGE_LENGTH = 300

        // Id determinista: una solicitud por persona y publicación.
        fun buildId(postId: String, requesterId: String): String = "${postId}_$requesterId"
    }
}
