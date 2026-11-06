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

 Implementa la interfaz PostAnalysisScheduler del dominio y se enlaza en RepositoryModule.
 Los casos de uso de crear/editar publicaciones solo llaman a schedule(postId) y siguen su camino;
 esta clase arma internamente el AnalyzePostUseCase con los repositorios inyectados y lo ejecuta.
**/
// Una sola instancia: el alcance de corrutinas vive mientras viva la aplicación.
@Singleton
// Hilt inyecta los repositorios que necesita el caso de uso de análisis.
class BackgroundPostAnalysisScheduler @Inject constructor(
    // Repositorio de publicaciones: para leer la publicación y guardar el resultado del análisis.
    postRepository: PostRepository,
    // Repositorio de catálogos: aporta categorías y tipos de animal que usa el análisis.
    catalogRepository: CatalogRepository,
    // Repositorio de IA (OpenRouter) que realiza el análisis.
    aiRepository: AiAnalysisRepository,
) : PostAnalysisScheduler {

    // Alcance propio en el hilo IO; SupervisorJob evita que el fallo de un análisis cancele los demás.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    // Caso de uso del dominio que realiza el análisis completo de una publicación.
    private val analyzePost = AnalyzePostUseCase(postRepository, catalogRepository, aiRepository)

    // Programa el análisis de una publicación sin bloquear a quien lo llama.
    override fun schedule(postId: String) {
        // Lanza una corrutina independiente que ejecuta el análisis en segundo plano.
        scope.launch { analyzePost(postId) }
    }
}
