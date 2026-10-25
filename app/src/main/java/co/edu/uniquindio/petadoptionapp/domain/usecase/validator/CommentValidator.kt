package co.edu.uniquindio.petadoptionapp.domain.usecase.validator

import co.edu.uniquindio.petadoptionapp.domain.model.comment.Comment

/**
Documenta el objeto que contiene las reglas de formato de los comentarios, sin red ni dependencias de Android.
Define un objeto singleton que válida el texto usando el máximo definido en el modelo [Comment].
 */
object CommentValidator {

    // Define una función pública que verifica que el texto no esté vacío ni supere el máximo.
    // Se valida aquí antes de crear el modelo, porque el modelo lanza excepciones si el texto es inválido.
    fun isTextValid(text: String): Boolean =
        text.trim().let { it.isNotEmpty() && it.length <= Comment.MAX_LENGTH }
}