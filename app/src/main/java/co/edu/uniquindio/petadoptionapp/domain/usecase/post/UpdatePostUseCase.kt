package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.Verification
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.CheckPostCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.PostValidator

/**
Documenta el caso de uso que edita una publicación propia.
Solo se edita mientras está en revisión (sin verificar); al editar, el resumen de IA se reinicia.
Una verificada no se edita (así nadie publica algo aprobado y lo cambia después): solo se finaliza o se elimina.
Una rechazada tampoco: el moderador ya dio su motivo y el autor solo puede eliminarla.
 */
class UpdatePostUseCase(
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso que revisa las categorías y tipos de animal.
    private val checkPostCatalog: CheckPostCatalogUseCase
) {
    // Define la función asíncrona que recibe el autor y la publicación a editar, y devuelve el resultado.
    suspend operator fun invoke(userId: String, edited: Post): Result<Unit> {

        // Busca la publicación original; si no existe, avisa.
        val current = postRepository.findById(edited.id)
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

        // Reinicia la verificación (y el resumen de IA) y guarda solo el contenido.
        return postRepository.updateContent(
            edited.copy(
                verification = Verification(),
                aiSummary = null,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}