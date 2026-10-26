package co.edu.uniquindio.petadoptionapp.data.repository.memory

import co.edu.uniquindio.petadoptionapp.data.repository.send.SeedData
import co.edu.uniquindio.petadoptionapp.domain.error.CatalogException
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalType
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
Documenta la implementación en memoria del repositorio de catálogos.
Simula Firestore: categorías y tipos de animal viven en listas temporales.
Solo ejecuta: los permisos de moderador y las validaciones viven en los casos de uso.
 */
// Anota la clase como un singleton para que Hilt gestione una única instancia compartida.
@Singleton
class CatalogRepositoryImpl @Inject constructor() : CatalogRepository {

    // Guarda las categorías en un flujo de estado privado; ya vienen cargadas porque en memoria no hay lectura remota.
    private val _categories = MutableStateFlow(SeedData.categories)
    // Expone un flujo de estado inmutable de categorías para su observación en la interfaz o dominio.
    override val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    // Guarda los tipos de animal en un flujo de estado privado.
    private val _animalTypes = MutableStateFlow(SeedData.animalTypes)
    // Expone un flujo de estado inmutable de tipos de animal.
    override val animalTypes: StateFlow<List<AnimalType>> = _animalTypes.asStateFlow()

    // En memoria los flujos ya están al día; en Firestore aquí se leerían las colecciones.
    // Define la función asíncrona para refrescar datos, retornando un resultado exitoso por defecto.
    override suspend fun refresh(): Result<Unit> = Result.success(Unit)

    // Busca y retorna una categoría por su ID, o nulo si no se encuentra en el flujo actual.
    override suspend fun findCategoryById(id: String): Category? =
        _categories.value.firstOrNull { it.id == id }

    // Busca y retorna un tipo de animal por su ID, o nulo si no se encuentra.
    override suspend fun findAnimalTypeById(id: String): AnimalType? =
        _animalTypes.value.firstOrNull { it.id == id }

    // Define la función asíncrona para crear una nueva categoría en el catálogo.
    override suspend fun createCategory(category: Category): Result<Unit> {
        // Simula el id fijo de Firestore: no se duplica validando si ya existe una categoría con el mismo ID.
        if (_categories.value.any { it.id == category.id })
            return Result.failure(CatalogException.CategoryAlreadyExists)
        // Actualiza el flujo agregando la nueva categoría a la lista existente.
        _categories.update { it + category }
        return Result.success(Unit)
    }

    // Define la función asíncrona para crear un nuevo tipo de animal en el catálogo.
    override suspend fun createAnimalType(animalType: AnimalType): Result<Unit> {
        // Valida si ya existe un tipo de animal con el mismo ID para evitar duplicados.
        if (_animalTypes.value.any { it.id == animalType.id })
            return Result.failure(CatalogException.AnimalTypeAlreadyExists)
        // Actualiza el flujo agregando el nuevo tipo de animal.
        _animalTypes.update { it + animalType }
        return Result.success(Unit)
    }

    // Define la función asíncrona para actualizar el nombre y el estado de una categoría existente.
    override suspend fun updateCategory(id: String, name: String, active: Boolean): Result<Unit> {
        // Valida que la categoría exista en el flujo o retorna un fallo si no se encuentra.
        if (_categories.value.none { it.id == id })
            return Result.failure(CatalogException.CategoryNotFound)
        // El id no cambia: las publicaciones existentes siguen apuntando bien. Actualiza los campos manteniendo el ID.
        _categories.update { list -> list.map { if (it.id == id) it.copy(name = name, active = active) else it } }
        return Result.success(Unit)
    }

    // Define la función asíncrona para actualizar el nombre y el estado de un tipo de animal existente.
    override suspend fun updateAnimalType(id: String, name: String, active: Boolean): Result<Unit> {
        // Valida que el tipo de animal exista o retorna un fallo en caso contrario.
        if (_animalTypes.value.none { it.id == id })
            return Result.failure(CatalogException.AnimalTypeNotFound)
        // Actualiza el tipo de animal correspondiente en la lista manteniendo su ID inalterado.
        _animalTypes.update { list -> list.map { if (it.id == id) it.copy(name = name, active = active) else it } }
        return Result.success(Unit)
    }
}