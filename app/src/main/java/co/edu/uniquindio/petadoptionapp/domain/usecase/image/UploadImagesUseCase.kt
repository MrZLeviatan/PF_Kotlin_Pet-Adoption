package co.edu.uniquindio.petadoptionapp.domain.usecase.image

import co.edu.uniquindio.petadoptionapp.domain.error.ImageException
import co.edu.uniquindio.petadoptionapp.domain.repository.ImageFolder
import co.edu.uniquindio.petadoptionapp.domain.repository.ImageStorageRepository

/**
Caso de uso que sube a Cloudinary las fotos que aún están en el teléfono.
Las que ya son URL públicas (https) se dejan igual, así editar sin cambiar fotos no vuelve a subirlas.
Si el almacenamiento no está configurado (sin llaves), deja la foto local para que la app siga funcionando en desarrollo.
Lo usan CreatePost, UpdatePost, UpdateProfile y SendMessage antes de guardar las URLs.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class UploadImagesUseCase(
    // Inyecta el repositorio del almacenamiento de imágenes.
    private val imageStorage: ImageStorageRepository
) {
    // Varias fotos (publicaciones): devuelve la lista de URLs finales en el mismo orden.
    suspend operator fun invoke(urls: List<String>, folder: ImageFolder): Result<List<String>> {
        // Recorre cada URL y la transforma en su versión pública.
        val uploaded = urls.map { url ->
            // Si ya es una URL pública, la deja igual.
            if (!url.isLocal()) return@map url
            // Sube la foto local a la carpeta indicada y toma su URL pública.
            imageStorage.upload(url, folder).getOrElse { error ->
                // Sin llaves configuradas conserva la URL local; cualquier otro error corta todo con UploadFailed.
                if (error === ImageException.NotConfigured) url else return Result.failure(ImageException.UploadFailed)
            }
        }
        // Devuelve la lista de URLs finales.
        return Result.success(uploaded)
    }

    // Una sola foto (perfil, chat); null queda null.
    suspend operator fun invoke(url: String?, folder: ImageFolder): Result<String?> =
        // Si no hay foto devuelve null; si la hay, reutiliza la versión de lista y toma el único elemento.
        if (url == null) Result.success(null) else invoke(listOf(url), folder).map { it.first() }

    // Indica si la dirección apunta a un archivo del teléfono (aún no subido).
    private fun String.isLocal(): Boolean = startsWith("file://") || startsWith("content://")
}
