package co.edu.uniquindio.petadoptionapp.data.repository.remote

import co.edu.uniquindio.petadoptionapp.domain.repository.AiAnalysisRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostAnalysisScheduler
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.ai.AnalyzePostUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 Corre el análisis con IA en segundo plano, con un alcance propio de la app: sigue aunque la persona
 salga de la pantalla de publicar. Si falla (sin llave o sin internet) la publicación queda sin análisis
 y un moderador puede pedirlo después desde su inicio.
 */
@Singleton
class BackgroundPostAnalysisScheduler @Inject constructor(
    postRepository: PostRepository,
    catalogRepository: CatalogRepository,
    aiRepository: AiAnalysisRepository,
) : PostAnalysisScheduler {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val analyzePost = AnalyzePostUseCase(postRepository, catalogRepository, aiRepository)

    override fun schedule(postId: String) {
        scope.launch { analyzePost(postId) }
    }
}
