package co.edu.uniquindio.petadoptionapp.domain.usecase.catalog

import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.StateFlow

/**
Caso de uso que expone las categorías para filtros y formularios.
Los filtros del feed muestran todas; el formulario de publicar solo las activas (`active == true`), lo decide la pantalla.
Lo usan los ViewModels del feed, del mapa y del formulario de publicación.
**/
// Declara el caso de uso que observa las categorías.
class ObserveCategoriesUseCase(
    // Inyecta el repositorio de catálogos para leer las categorías.
    private val catalogRepository: CatalogRepository
) {
    // Define la función que devuelve un flujo de categorías.
    operator fun invoke(): StateFlow<List<Category>> = catalogRepository.categories
}
