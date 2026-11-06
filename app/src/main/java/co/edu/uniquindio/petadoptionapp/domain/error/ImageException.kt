package co.edu.uniquindio.petadoptionapp.domain.error

/**
Errores al subir imágenes al almacenamiento externo (Cloudinary).
Los produce la implementación de ImageStorageRepository y los propaga UploadImagesUseCase;
la UI los traduce a textos amigables usando strings.xml.
**/
// Jerarquía sellada con los fallos posibles de la subida de imágenes.
sealed class ImageException : Exception() {
    // Faltan las llaves del almacenamiento (CLOUDINARY_* en local.properties).
    object NotConfigured : ImageException()

    // No se pudo subir (sin internet, archivo ilegible o rechazo del servidor).
    object UploadFailed : ImageException()
}
