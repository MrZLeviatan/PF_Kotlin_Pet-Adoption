package co.edu.uniquindio.petadoptionapp.domain.error

/** Errores del análisis con IA. */
sealed class AiException : Exception() {
    // Falta OPENROUTER_API_KEY en local.properties.
    object NotConfigured : AiException()

    // No se pudo analizar (sin internet, límite del servicio o respuesta inválida).
    object AnalysisFailed : AiException()
}
