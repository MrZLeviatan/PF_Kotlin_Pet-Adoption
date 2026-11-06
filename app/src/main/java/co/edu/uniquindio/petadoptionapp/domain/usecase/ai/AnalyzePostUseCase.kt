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
Caso de uso que analiza una publicación con IA y guarda el resultado.
1. Junta publicaciones parecidas (del mismo autor, o de la misma categoría a menos de 10 km) para detectar duplicados.
2. Pide el análisis (prioridad, resumen, puntos clave, relevancia, inconsistencias, fotos, contenido inapropiado, duplicado).
3. Guarda la revisión y la prioridad sugerida; no cambia el estado de verificación (eso lo decide un moderador).
Lo ejecuta el trabajo en segundo plano que programa PostAnalysisScheduler al crear o editar
una publicación, y la pantalla del moderador cuando pide volver a analizar.
**/
// Declara el caso de uso con los repositorios que necesita.
class AnalyzePostUseCase(
    // Repositorio de publicaciones: lee la publicación, busca candidatas y guarda el análisis.
    private val postRepository: PostRepository,
    // Repositorio de catálogos: traduce ids de categoría y tipo de animal a nombres legibles.
    private val catalogRepository: CatalogRepository,
    // Repositorio de IA: hace el análisis en el servicio externo.
    private val aiRepository: AiAnalysisRepository
) {
    // Constantes de configuración del análisis.
    companion object {
        // Cuántas publicaciones parecidas se envían como candidatas a duplicado.
        const val MAX_CANDIDATES = 10
        // Radio en kilómetros para buscar publicaciones cercanas de la misma categoría.
        const val NEARBY_KM = 10.0
    }

    // Función principal: analiza la publicación indicada y devuelve la revisión guardada.
    suspend operator fun invoke(postId: String): Result<AiReview> {
        // Busca la publicación; si no existe, falla con PostNotFound.
        val post = postRepository.findById(postId) ?: return Result.failure(PostException.PostNotFound)
        // No analiza publicaciones eliminadas.
        if (post.status == PostStatus.DELETED) return Result.failure(PostException.PostNotActive)

        // Arma los datos que se envían a la IA.
        val input = AiPostInput(
            // La publicación completa.
            post = post,
            // Nombre de la categoría; si no se encuentra, usa el id como respaldo.
            categoryName = catalogRepository.findCategoryById(post.categoryId)?.name ?: post.categoryId,
            // Nombre del tipo de animal, solo si la publicación tiene uno.
            animalTypeName = post.animalTypeId?.let { catalogRepository.findAnimalTypeById(it)?.name },
            // Publicaciones parecidas para detectar duplicados.
            candidates = duplicateCandidates(post),
        )
        // Pide el análisis a la IA; si falla, devuelve el error.
        val analysis = aiRepository.analyze(input).getOrElse { return Result.failure(it) }

        // Solo vale un duplicado que esté entre las candidatas enviadas (la IA no puede inventar ids).
        val validIds = input.candidates.map { it.id }.toSet()
        // Si el duplicado sugerido no es una candidata válida, se descarta junto con su motivo.
        val review = analysis.review.let { if (it.duplicateOfPostId in validIds) it else it.copy(duplicateOfPostId = null, duplicateReason = "") }

        // Guarda revisión y prioridad en la publicación y, si sale bien, devuelve la revisión.
        return postRepository.updateAiAnalysis(postId, review, analysis.priority, analysis.priorityReason).map { review }
    }

    // Parecidas: primero las del mismo autor (lo más común al duplicar), luego las de la misma categoría cerca.
    private suspend fun duplicateCandidates(post: Post): List<Post> {
        // Publicaciones del mismo autor (lista vacía si la consulta falla).
        val sameAuthor = postRepository.getByAuthor(post.authorId).getOrDefault(emptyList())
        // Publicaciones cercanas de la misma categoría, solo si la publicación tiene coordenadas.
        val nearby = if (post.location.hasCoordinates()) {
            // Consulta el feed filtrando por categoría y radio alrededor de la publicación.
            postRepository.getFeed(
                PostFilter(categoryId = post.categoryId, center = Location(post.location.latitude, post.location.longitude), radiusKm = NEARBY_KM),
                limit = MAX_CANDIDATES,
            // Usa una lista vacía si la consulta falla.
            ).getOrDefault(emptyList())
        // Sin coordenadas no hay búsqueda por cercanía.
        } else emptyList()
        // Une ambas listas y las depura.
        return (sameAuthor + nearby)
            // Excluye la propia publicación y las eliminadas.
            .filter { it.id != post.id && it.status != PostStatus.DELETED }
            // Quita repetidas (una publicación puede estar en ambas listas).
            .distinctBy { it.id }
            // Limita la cantidad de candidatas enviadas a la IA.
            .take(MAX_CANDIDATES)
    }
}
