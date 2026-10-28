package co.edu.uniquindio.petadoptionapp.domain.usecase.validator

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post

/**
Documenta el objeto que contiene las reglas de formato de las publicaciones, sin red ni dependencias de Android.
Define un objeto singleton que agrupa las validaciones de título, descripción, fotos, edad y motivo de rechazo.
 */
object PostValidator {

    // Define el tamaño mínimo y máximo del título.
    const val MIN_TITLE_LENGTH = 5
    const val MAX_TITLE_LENGTH = 80

    // Define el tamaño mínimo y máximo de la descripción.
    const val MIN_DESCRIPTION_LENGTH = 20
    const val MAX_DESCRIPTION_LENGTH = 1000

    // Define la cantidad máxima de fotos (cuida la cuota gratuita de Cloudinary).
    const val MAX_PHOTOS = 5

    // Define la edad máxima aproximada en meses (30 años).
    const val MAX_AGE_IN_MONTHS = 360

    // Define el tamaño mínimo del motivo de rechazo.
    const val MIN_REJECTION_REASON_LENGTH = 10

    // Define una función pública que verifica que el título esté dentro del rango de tamaño.
    fun isTitleValid(title: String): Boolean =
        title.trim().length in MIN_TITLE_LENGTH..MAX_TITLE_LENGTH

    // Define una función pública que verifica que la descripción esté dentro del rango de tamaño.
    fun isDescriptionValid(description: String): Boolean =
        description.trim().length in MIN_DESCRIPTION_LENGTH..MAX_DESCRIPTION_LENGTH

    // Define una función pública que verifica el motivo escrito por el moderador al rechazar.
    fun isRejectionReasonValid(reason: String): Boolean =
        reason.trim().length >= MIN_REJECTION_REASON_LENGTH

    // Define una función pública que revisa todas las reglas de una publicación y devuelve el primer error, o `null` si todo está bien.
    fun validate(post: Post): PostException? = when {
        // Título fuera del rango permitido.
        !isTitleValid(post.title) -> PostException.InvalidTitle
        // No se eligió categoría.
        post.categoryId.isBlank() -> PostException.InvalidCategory
        // Descripción fuera del rango permitido.
        !isDescriptionValid(post.description) -> PostException.InvalidDescription
        // Sin fotos: se exige al menos una.
        post.photoUrls.isEmpty() -> PostException.NoPhotos
        // Demasiadas fotos.
        post.photoUrls.size > MAX_PHOTOS -> PostException.TooManyPhotos
        // Edad negativa o exagerada (es opcional, solo se revisa si viene).
        post.ageInMonths != null && post.ageInMonths !in 0..MAX_AGE_IN_MONTHS -> PostException.InvalidAge
        // Sin coordenadas reales del mapa.
        !post.location.hasCoordinates() -> PostException.InvalidLocation
        // Todo correcto.
        else -> null
    }
}