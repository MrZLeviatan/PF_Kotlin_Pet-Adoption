package co.edu.uniquindio.petadoptionapp.domain.model.post

import kotlinx.serialization.Serializable

/**
 Revisión automática (IA) de una publicación, para que el moderador decida rápido.
 La IA no aprueba ni rechaza: solo sugiere la prioridad (en Verification), resume y señala riesgos.
 */
@Serializable
data class AiReview(
    // Resumen breve para el moderador.
    val summary: String = "",
    // Puntos clave de la publicación.
    val keyPoints: List<String> = emptyList(),
    // Relevancia estimada (0 a 100): qué tanto ayuda a un animal en necesidad.
    val relevance: Int = 0,
    // Contradicciones o datos que no cuadran (ej. "dice cachorro pero 8 años").
    val inconsistencies: List<String> = emptyList(),
    // ¿Las fotos muestran al animal descrito? null si no había fotos que revisar.
    val imagesRelevant: Boolean? = null,
    val imageNotes: String = "",
    // Contenido inapropiado (venta de animales, maltrato explícito, spam, datos ofensivos...).
    val inappropriate: Boolean = false,
    val inappropriateReasons: List<String> = emptyList(),
    // Publicación que parece repetida (id) y por qué.
    val duplicateOfPostId: String? = null,
    val duplicateReason: String = "",
    // Qué debería revisar o hacer el moderador.
    val recommendedActions: List<String> = emptyList(),
    // Modelo que hizo el análisis y cuándo (milisegundos epoch).
    val model: String = "",
    val analyzedAt: Long = System.currentTimeMillis(),
) {
    /** Nivel de relevancia según el puntaje. */
    val relevanceLevel: RelevanceLevel
        get() = when {
            relevance >= 70 -> RelevanceLevel.HIGH
            relevance >= 40 -> RelevanceLevel.MEDIUM
            else -> RelevanceLevel.LOW
        }

    /** ¿Hay algo que el moderador debe mirar con cuidado? */
    val hasWarnings: Boolean
        get() = inappropriate || duplicateOfPostId != null || imagesRelevant == false || inconsistencies.isNotEmpty()
}

enum class RelevanceLevel { HIGH, MEDIUM, LOW }

/** Lo que devuelve el análisis: la revisión y la prioridad sugerida. */
data class AiPostAnalysis(
    val review: AiReview,
    val priority: PriorityLevel,
    val priorityReason: String,
)
