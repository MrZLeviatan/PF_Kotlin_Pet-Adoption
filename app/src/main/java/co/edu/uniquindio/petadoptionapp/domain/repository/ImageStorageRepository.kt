package co.edu.uniquindio.petadoptionapp.domain.repository

/**
Contrato (puerto) del dominio para el almacenamiento de imágenes (Cloudinary).
Define [ImageFolder], la carpeta destino según el uso de la imagen, y la interfaz
[ImageStorageRepository], que implementa la capa de datos. La consume UploadImagesUseCase
al publicar, editar el perfil o enviar fotos por chat.
**/
// Carpeta (dentro del folder de la app en el almacenamiento) según para qué es la imagen: publicaciones, perfiles o chats.
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
