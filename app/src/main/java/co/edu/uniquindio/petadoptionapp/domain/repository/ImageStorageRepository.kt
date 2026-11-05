package co.edu.uniquindio.petadoptionapp.domain.repository

/** Carpeta (dentro del folder de la app en el almacenamiento) según para qué es la imagen. */
enum class ImageFolder(val path: String) { POSTS("posts"), PROFILES("profiles"), CHATS("chats") }

/**
Contrato del almacenamiento de imágenes (Cloudinary).
Las fotos se eligen en el teléfono y quedan como archivo local; aquí se suben y se obtiene su URL pública.
 */
interface ImageStorageRepository {
    /**
    Sube una imagen local.
    - Qué envía: La dirección local [localUri] ("file://…" o "content://…") y su [folder].
    - Qué recibe: Un [Result] con la URL pública (https) de la imagen subida.
    Falla con ImageException.NotConfigured si faltan las llaves, o UploadFailed si no se pudo subir.
     */
    suspend fun upload(localUri: String, folder: ImageFolder): Result<String>
}
