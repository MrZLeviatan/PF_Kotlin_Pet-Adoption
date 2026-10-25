package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.Verification
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.CheckPostCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.PostValidator

/**
Documenta el caso de uso que crea una publicación nueva.
Toda publicación nace sin verificar, en proceso y con los contadores en cero, sin importar lo que envíe la pantalla.
 */
class CreatePostUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que revisa las categorías y tipos de animal.
    private val checkPostCatalog: CheckPostCatalogUseCase
) {
    // Define la función asíncrona que recibe el autor y el borrador, y devuelve la publicación guardada.
    suspend operator fun invoke(authorId: String, draft: Post): Result<Post> {

        // Revisa todas las reglas de formato; si hay un error, lo retorna.
        PostValidator.validate(draft)?.let { return Result.failure(it) }

        // Verifica que la categoría y el tipo de animal existan y estén activos.
        checkPostCatalog(draft.categoryId, draft.animalTypeId).getOrElse { return Result.failure(it) }

        // Fuerza los valores iniciales para que nadie se salte la moderación ni infle contadores.
        val now = System.currentTimeMillis()
        val post = draft.copy(
            id = "",
            authorId = authorId,
            status = PostStatus.IN_PROGRESS,
            verification = Verification(),
            aiSummary = null,
            importantCount = 0,
            commentCount = 0,
            createdAt = now,
            updatedAt = now,
            finishedAt = null
        )

        // Guarda la publicación.
        return postRepository.create(post)
    }
}