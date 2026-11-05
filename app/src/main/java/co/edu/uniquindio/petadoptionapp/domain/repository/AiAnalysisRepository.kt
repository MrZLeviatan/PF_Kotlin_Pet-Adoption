package co.edu.uniquindio.petadoptionapp.domain.repository

import co.edu.uniquindio.petadoptionapp.domain.model.post.AiPostAnalysis
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post

/** Datos de una publicación que se le envían a la IA (con los nombres del catálogo ya resueltos). */
data class AiPostInput(
    val post: Post,
    val categoryName: String,
    val animalTypeName: String?,
    // Publicaciones parecidas (mismo autor, categoría o zona) para detectar duplicados.
    val candidates: List<Post>,
)

/**
Contrato del análisis con IA (OpenRouter).
 */
interface AiAnalysisRepository {
    /**
    Analiza una publicación: prioridad, resumen, puntos clave, relevancia, inconsistencias,
    relevancia de las fotos, contenido inapropiado, posible duplicado y acciones recomendadas.
    - Qué recibe: Un [Result]; falla con AiException.NotConfigured si falta la llave, o AnalysisFailed.
     */
    suspend fun analyze(input: AiPostInput): Result<AiPostAnalysis>
}
