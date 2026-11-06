package co.edu.uniquindio.petadoptionapp.domain.repository

import co.edu.uniquindio.petadoptionapp.domain.model.post.AiPostAnalysis
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post

/**
Contrato (puerto) del dominio para analizar publicaciones con inteligencia artificial.
Define [AiPostInput], los datos que se le envían a la IA, y la interfaz [AiAnalysisRepository],
que implementa la capa de datos usando OpenRouter. La consume AnalyzePostUseCase, que guarda
el resultado en la publicación para ayudar al moderador.
**/
// Datos de una publicación que se le envían a la IA (con los nombres del catálogo ya resueltos).
data class AiPostInput(
    // Publicación a analizar.
    val post: Post,
    // Nombre legible de la categoría (no solo su id).
    val categoryName: String,
    // Nombre legible del tipo de animal; nulo si no aplica.
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
    - Qué envía ([input]): La publicación con sus nombres de catálogo y candidatas a duplicado.
    - Qué recibe: Un [Result] con el [AiPostAnalysis]; falla con AiException.NotConfigured si falta la llave, o AnalysisFailed.
     */
    suspend fun analyze(input: AiPostInput): Result<AiPostAnalysis>
}
