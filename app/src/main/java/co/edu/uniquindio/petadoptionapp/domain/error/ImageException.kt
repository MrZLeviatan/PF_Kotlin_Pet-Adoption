package co.edu.uniquindio.petadoptionapp.domain.error

/** Errores al subir imágenes. */
sealed class ImageException : Exception() {
    // Faltan las llaves del almacenamiento (CLOUDINARY_* en local.properties).
    object NotConfigured : ImageException()

    // No se pudo subir (sin internet, archivo ilegible o rechazo del servidor).
    object UploadFailed : ImageException()
}
