package co.edu.uniquindio.petadoptionapp.domain.usecase.catalog

import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalType
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.StateFlow

/**
Caso de uso que expone los tipos de animal para filtros y formularios.
Devuelve el flujo en caché de CatalogRepository, así la UI se actualiza sola cuando el catálogo cambia.
Lo usan los ViewModels del feed, del mapa y del formulario de publicación.
**/
// Declara el caso de uso que observa los tipos de animal.
class ObserveAnimalTypesUseCase(
    // Inyecta el repositorio de catálogos para leer los tipos de animal.
    private val catalogRepository: CatalogRepository
)   {
    // Define la función que devuelve un flujo de tipos de animal.
    operator fun invoke(): StateFlow<List<AnimalType>> = catalogRepository.animalTypes
}
