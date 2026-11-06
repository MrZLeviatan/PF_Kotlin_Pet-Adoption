package co.edu.uniquindio.petadoptionapp.domain.model.post

import kotlinx.serialization.Serializable

/**
Modelo de dominio: revisión automática (IA) de una publicación, para que el moderador decida rápido.
La IA no aprueba ni rechaza: solo sugiere la prioridad (en Verification), resume y señala riesgos.
Este archivo también define [RelevanceLevel] (nivel de relevancia) y [AiPostAnalysis]
(resultado completo del análisis). Los produce AiAnalysisRepository, los guarda
AnalyzePostUseCase dentro de [Post.aiReview] y los muestra la pantalla de revisión del moderador.
**/
// Permite convertir la revisión a/desde el formato de almacenamiento.
@Serializable
// Declara la revisión de la IA como clase de datos inmutable.
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
    // Observaciones de la IA sobre las fotos.
    val imageNotes: String = "",
    // Contenido inapropiado (venta de animales, maltrato explícito, spam, datos ofensivos...).
    val inappropriate: Boolean = false,
    // Motivos por los que se consideró inapropiado.
    val inappropriateReasons: List<String> = emptyList(),
    // Publicación que parece repetida (id) y por qué.
    val duplicateOfPostId: String? = null,
    // Explicación de por qué parece un duplicado.
    val duplicateReason: String = "",
    // Qué debería revisar o hacer el moderador.
    val recommendedActions: List<String> = emptyList(),
    // Modelo que hizo el análisis y cuándo (milisegundos epoch).
    val model: String = "",
    // Momento en que se hizo el análisis, en milisegundos (epoch).
    val analyzedAt: Long = System.currentTimeMillis(),
) {
    /** Nivel de relevancia según el puntaje. */
    val relevanceLevel: RelevanceLevel
        // Calcula el nivel cada vez que se consulta, según el puntaje.
        get() = when {
            // 70 o más se considera relevancia alta.
            relevance >= 70 -> RelevanceLevel.HIGH
            // Entre 40 y 69 se considera relevancia media.
            relevance >= 40 -> RelevanceLevel.MEDIUM
            // Menos de 40 se considera relevancia baja.
            else -> RelevanceLevel.LOW
        }

    /** ¿Hay algo que el moderador debe mirar con cuidado? */
    val hasWarnings: Boolean
        // Hay alerta si es inapropiada, parece duplicada, las fotos no cuadran o hay inconsistencias.
        get() = inappropriate || duplicateOfPostId != null || imagesRelevant == false || inconsistencies.isNotEmpty()
}

// Niveles de relevancia derivados del puntaje: alto, medio y bajo.
enum class RelevanceLevel { HIGH, MEDIUM, LOW }

/** Lo que devuelve el análisis: la revisión y la prioridad sugerida. */
data class AiPostAnalysis(
    // Revisión detallada generada por la IA.
    val review: AiReview,
    // Prioridad sugerida para la cola de moderación.
    val priority: PriorityLevel,
    // Explicación de la IA sobre esa prioridad.
    val priorityReason: String,
)
