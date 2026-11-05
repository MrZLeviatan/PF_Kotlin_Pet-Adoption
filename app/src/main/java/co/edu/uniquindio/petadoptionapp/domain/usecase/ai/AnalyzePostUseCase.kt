package co.edu.uniquindio.petadoptionapp.domain.usecase.ai

import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.model.post.AiReview
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.filter.PostFilter
import co.edu.uniquindio.petadoptionapp.domain.repository.AiAnalysisRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.AiPostInput
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository

/**
Documenta el caso de uso que analiza una publicación con IA y guarda el resultado.
1. Junta publicaciones parecidas (del mismo autor, o de la misma categoría a menos de 10 km) para detectar duplicados.
2. Pide el análisis (prioridad, resumen, puntos clave, relevancia, inconsistencias, fotos, contenido inapropiado, duplicado).
3. Guarda la revisión y la prioridad sugerida; no cambia el estado de verificación (eso lo decide un moderador).
 */
class AnalyzePostUseCase(
    private val postRepository: PostRepository,
    private val catalogRepository: CatalogRepository,
    private val aiRepository: AiAnalysisRepository
) {
    companion object {
        // Cuántas publicaciones parecidas se envían como candidatas a duplicado.
        const val MAX_CANDIDATES = 10
        const val NEARBY_KM = 10.0
    }

    suspend operator fun invoke(postId: String): Result<AiReview> {
        val post = postRepository.findById(postId) ?: return Result.failure(PostException.PostNotFound)
        if (post.status == PostStatus.DELETED) return Result.failure(PostException.PostNotActive)

        val input = AiPostInput(
            post = post,
            categoryName = catalogRepository.findCategoryById(post.categoryId)?.name ?: post.categoryId,
            animalTypeName = post.animalTypeId?.let { catalogRepository.findAnimalTypeById(it)?.name },
            candidates = duplicateCandidates(post),
        )
        val analysis = aiRepository.analyze(input).getOrElse { return Result.failure(it) }

        // Solo vale un duplicado que esté entre las candidatas enviadas (la IA no puede inventar ids).
        val validIds = input.candidates.map { it.id }.toSet()
        val review = analysis.review.let { if (it.duplicateOfPostId in validIds) it else it.copy(duplicateOfPostId = null, duplicateReason = "") }

        return postRepository.updateAiAnalysis(postId, review, analysis.priority, analysis.priorityReason).map { review }
    }

    // Parecidas: primero las del mismo autor (lo más común al duplicar), luego las de la misma categoría cerca.
    private suspend fun duplicateCandidates(post: Post): List<Post> {
        val sameAuthor = postRepository.getByAuthor(post.authorId).getOrDefault(emptyList())
        val nearby = if (post.location.hasCoordinates()) {
            postRepository.getFeed(
                PostFilter(categoryId = post.categoryId, center = Location(post.location.latitude, post.location.longitude), radiusKm = NEARBY_KM),
                limit = MAX_CANDIDATES,
            ).getOrDefault(emptyList())
        } else emptyList()
        return (sameAuthor + nearby)
            .filter { it.id != post.id && it.status != PostStatus.DELETED }
            .distinctBy { it.id }
            .take(MAX_CANDIDATES)
    }
}
