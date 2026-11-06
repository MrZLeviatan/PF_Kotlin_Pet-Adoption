package co.edu.uniquindio.petadoptionapp.domain.repository

/**
Contrato (puerto) del dominio para pedir el análisis con IA de una publicación en segundo plano
(no hace esperar a quien publica). Con Firebase esto sería una Cloud Function al crear o editar el documento.
Lo implementa la capa de datos y lo llaman CreatePostUseCase y UpdatePostUseCase; el trabajo
en segundo plano termina ejecutando AnalyzePostUseCase.
**/
interface PostAnalysisScheduler {
    // Programa el análisis de la publicación indicada sin bloquear a quien llama.
    fun schedule(postId: String)
}
