package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.usecase.image.UploadImagesUseCase
import co.edu.uniquindio.petadoptionapp.domain.repository.ImageFolder
import co.edu.uniquindio.petadoptionapp.domain.repository.PostAnalysisScheduler

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.Verification
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.CheckPostCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.PostValidator

/**
Caso de uso que edita una publicación propia.
Solo se edita mientras está en revisión (sin verificar); al editar, el resumen de IA se reinicia.
Una verificada no se edita (así nadie publica algo aprobado y lo cambia después): solo se finaliza o se elimina.
Una rechazada tampoco: el moderador ya dio su motivo y el autor solo puede eliminarla.
Lo usa el ViewModel de la pantalla de editar publicación.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class UpdatePostUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que revisa las categorías y tipos de animal.
    private val checkPostCatalog: CheckPostCatalogUseCase,
    // Inyecta el caso de uso que sube a Cloudinary las fotos nuevas.
    private val uploadImages: UploadImagesUseCase,
    // Vuelve a pedir el análisis con IA: el contenido cambió.
    private val analysisScheduler: PostAnalysisScheduler
) {
    // Define la función asíncrona que recibe el autor y la publicación a editar, y devuelve el resultado.
    suspend operator fun invoke(userId: String, edited: Post): Result<Unit> {

        // Busca la publicación original; si no existe, avisa.
        val current = postRepository.findById(edited.id)
            // Si no existe, falla con PostNotFound.
            ?: return Result.failure(PostException.PostNotFound)

        // Solo el autor puede editarla.
        if (current.authorId != userId) return Result.failure(PostException.NotAllowed)

        // Solo se edita mientras sigue en proceso.
        if (current.status != PostStatus.IN_PROGRESS) return Result.failure(PostException.PostNotActive)

        // Ya revisada por un moderador (verificada o rechazada): no se edita.
        if (current.verification.status != VerificationStatus.UNVERIFIED) return Result.failure(PostException.AlreadyReviewed)

        // Revisa las reglas de formato.
        PostValidator.validate(edited)?.let { return Result.failure(it) }

        // Verifica que la categoría y el tipo de animal existan y estén activos.
        checkPostCatalog(edited.categoryId, edited.animalTypeId).getOrElse { return Result.failure(it) }

        // Sube solo las fotos nuevas (las que ya están en Cloudinary quedan igual).
        val photoUrls = uploadImages(edited.photoUrls, ImageFolder.POSTS).getOrElse { return Result.failure(it) }

        // Reinicia la verificación (y el resumen de IA) y guarda solo el contenido.
        return postRepository.updateContent(
            // Copia lo editado con los campos que controla el sistema.
            edited.copy(
                // URLs públicas de las fotos.
                photoUrls = photoUrls,
                // Verificación reiniciada.
                verification = Verification(),
                // Resumen de IA borrado hasta el nuevo análisis.
                aiReview = null,
                // Fecha de última edición.
                updatedAt = System.currentTimeMillis()
            )
        // Si se guardó, programa de nuevo el análisis con IA.
        ).onSuccess { analysisScheduler.schedule(edited.id) }
    }
}
