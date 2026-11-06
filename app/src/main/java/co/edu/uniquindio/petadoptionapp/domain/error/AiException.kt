package co.edu.uniquindio.petadoptionapp.domain.error

/**
Errores del análisis de publicaciones con inteligencia artificial.
Los produce la implementación de AiAnalysisRepository y los propaga AnalyzePostUseCase;
la UI los traduce a textos amigables usando strings.xml.
**/
// Jerarquía sellada con los fallos posibles del servicio de IA.
sealed class AiException : Exception() {
    // Falta OPENROUTER_API_KEY en local.properties.
    object NotConfigured : AiException()

    // No se pudo analizar (sin internet, límite del servicio o respuesta inválida).
    object AnalysisFailed : AiException()
}
