package co.edu.uniquindio.petadoptionapp.domain.repository

/**
 Pide el análisis con IA de una publicación en segundo plano (no hace esperar a quien publica).
 Con Firebase esto sería una Cloud Function al crear o editar el documento.
 */
interface PostAnalysisScheduler {
    fun schedule(postId: String)
}
