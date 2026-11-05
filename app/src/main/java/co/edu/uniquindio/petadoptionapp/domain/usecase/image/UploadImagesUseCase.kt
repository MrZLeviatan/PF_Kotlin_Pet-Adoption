package co.edu.uniquindio.petadoptionapp.domain.usecase.image

import co.edu.uniquindio.petadoptionapp.domain.error.ImageException
import co.edu.uniquindio.petadoptionapp.domain.repository.ImageFolder
import co.edu.uniquindio.petadoptionapp.domain.repository.ImageStorageRepository

/**
Documenta el caso de uso que sube a Cloudinary las fotos que aún están en el teléfono.
Las que ya son URL públicas (https) se dejan igual, así editar sin cambiar fotos no vuelve a subirlas.
Si el almacenamiento no está configurado (sin llaves), deja la foto local para que la app siga funcionando en desarrollo.
 */
class UploadImagesUseCase(
    // Inyecta el repositorio del almacenamiento de imágenes.
    private val imageStorage: ImageStorageRepository
) {
    suspend operator fun invoke(urls: List<String>, folder: ImageFolder): Result<List<String>> {
        val uploaded = urls.map { url ->
            if (!url.isLocal()) return@map url
            imageStorage.upload(url, folder).getOrElse { error ->
                if (error === ImageException.NotConfigured) url else return Result.failure(ImageException.UploadFailed)
            }
        }
        return Result.success(uploaded)
    }

    // Una sola foto (perfil, chat); null queda null.
    suspend operator fun invoke(url: String?, folder: ImageFolder): Result<String?> =
        if (url == null) Result.success(null) else invoke(listOf(url), folder).map { it.first() }

    private fun String.isLocal(): Boolean = startsWith("file://") || startsWith("content://")
}
