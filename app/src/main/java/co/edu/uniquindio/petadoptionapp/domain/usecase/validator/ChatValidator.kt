package co.edu.uniquindio.petadoptionapp.domain.usecase.validator

import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequest

/**
Validador del dominio que contiene las reglas de formato de los mensajes, sin red ni dependencias de Android.
Define un objeto singleton que valida el texto de los mensajes y el mensaje opcional de una solicitud de adopción.
Lo usan SendMessageUseCase y SendAdoptionInterestUseCase.
**/
// Objeto único (singleton) con las reglas de formato de los mensajes.
object ChatValidator {

    // Define el tamaño máximo de un mensaje de chat.
    const val MAX_MESSAGE_LENGTH = 1000

    // Define una función pública que verifica que el mensaje no esté vacío ni supere el máximo.
    fun isMessageValid(text: String): Boolean =
        // Quita espacios y exige que no quede vacío ni supere el máximo.
        text.trim().let { it.isNotEmpty() && it.length <= MAX_MESSAGE_LENGTH }

    // El pie de una foto puede ir vacío, pero no pasar del máximo de un mensaje.
    fun isCaptionValid(text: String): Boolean = text.trim().length <= MAX_MESSAGE_LENGTH

    // Define una función pública que verifica el mensaje opcional de la solicitud (puede ir vacío).
    fun isAdoptionMessageValid(text: String): Boolean =
        // Quita espacios y comprueba que no supere el máximo definido en AdoptionRequest.
        text.trim().length <= AdoptionRequest.MAX_MESSAGE_LENGTH
}
