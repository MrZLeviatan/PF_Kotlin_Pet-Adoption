package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.usecase.image.UploadImagesUseCase
import co.edu.uniquindio.petadoptionapp.domain.repository.ImageFolder
import co.edu.uniquindio.petadoptionapp.domain.repository.PostAnalysisScheduler

import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.Verification
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.CheckPostCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.PostValidator

/**
Caso de uso que crea una publicación nueva.
Toda publicación nace sin verificar, en proceso y con los contadores en cero, sin importar lo que envíe la pantalla.
Valida el borrador, revisa el catálogo, sube las fotos a Cloudinary, guarda en Firestore y programa el análisis con IA.
Lo usa el ViewModel de la pantalla de crear publicación.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class CreatePostUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que revisa las categorías y tipos de animal.
    private val checkPostCatalog: CheckPostCatalogUseCase,
    // Inyecta el caso de uso que sube las fotos a Cloudinary.
    private val uploadImages: UploadImagesUseCase,
    // Pide el análisis con IA (prioridad, resumen, riesgos) en segundo plano.
    private val analysisScheduler: PostAnalysisScheduler
) {
    // Define la función asíncrona que recibe el autor y el borrador, y devuelve la publicación guardada.
    suspend operator fun invoke(authorId: String, draft: Post): Result<Post> {

        // Revisa todas las reglas de formato; si hay un error, lo retorna.
        PostValidator.validate(draft)?.let { return Result.failure(it) }

        // Verifica que la categoría y el tipo de animal existan y estén activos.
        checkPostCatalog(draft.categoryId, draft.animalTypeId).getOrElse { return Result.failure(it) }

        // Sube las fotos (después de validar, para no subir nada si la publicación no es válida).
        val photoUrls = uploadImages(draft.photoUrls, ImageFolder.POSTS).getOrElse { return Result.failure(it) }

        // Fuerza los valores iniciales para que nadie se salte la moderación ni infle contadores.
        val now = System.currentTimeMillis()
        // Copia el borrador sobrescribiendo los campos que controla el sistema.
        val post = draft.copy(
            // URLs públicas de las fotos ya subidas.
            photoUrls = photoUrls,
            // Id vacío: lo genera Firestore al guardar.
            id = "",
            // Autor tomado del parámetro, no del borrador.
            authorId = authorId,
            // Nace en proceso.
            status = PostStatus.IN_PROGRESS,
            // Nace sin verificar.
            verification = Verification(),
            // Sin análisis de IA todavía.
            aiReview = null,
            // Contador de "Es importante" en cero.
            importantCount = 0,
            // Contador de comentarios en cero.
            commentCount = 0,
            // Fecha de creación.
            createdAt = now,
            // Fecha de última edición.
            updatedAt = now,
            // Aún no se ha finalizado.
            finishedAt = null
        )

        // Guarda la publicación y pide su análisis con IA (no hace esperar a quien publica).
        return postRepository.create(post).onSuccess { saved -> analysisScheduler.schedule(saved.id) }
    }
}
