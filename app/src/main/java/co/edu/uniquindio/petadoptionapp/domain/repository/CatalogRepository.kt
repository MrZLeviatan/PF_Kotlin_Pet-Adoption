package co.edu.uniquindio.petadoptionapp.domain.repository

import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalType
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import kotlinx.coroutines.flow.StateFlow

/**
Contrato de los catálogos (categorías y tipos de animal) guardados en Firestore.
Las validaciones y los permisos de moderador viven en los casos de uso, aquí solo se ejecuta.
 */
interface CatalogRepository {

    // Catálogos en memoria

    /**
    Flujo reactivo de todas las categorías (activas e inactivas).

    - Qué emite: Un [StateFlow] con la lista cargada en memoria.
    Los catálogos son pequeños y casi no cambian: se leen una vez y se reutilizan para ahorrar lecturas de la cuota gratuita.
     */
    val categories: StateFlow<List<Category>>

    /**
    Flujo reactivo de todos los tipos de animal (activos e inactivos).

    - Qué emite: Un [StateFlow] con la lista cargada en memoria.
     */
    val animalTypes: StateFlow<List<AnimalType>>

    /**
    Lee ambos catálogos de Firestore y actualiza los flujos.

    - Qué recibe: Un [Result<Unit>] que confirma si se cargaron.
    Se llama al abrir la app y después de cada cambio hecho por un moderador.
     */
    suspend fun refresh(): Result<Unit>


    // Consultar

    /**
    Busca una categoría.

    - Qué envía ([id]): El identificador de la categoría.
    - Qué recibe: La [Category] o `null` si no existe.
     */
    suspend fun findCategoryById(id: String): Category?

    /**
    Busca un tipo de animal.

    - Qué envía ([id]): El identificador del tipo de animal.
    - Qué recibe: El [AnimalType] o `null` si no existe.
     */
    suspend fun findAnimalTypeById(id: String): AnimalType?


    // Crear y modificar

    /**
    Guarda una categoría nueva.

    - Qué envía ([category]): La categoría con su id ya calculado.
    - Qué recibe: Un [Result<Unit>] que confirma si se guardó.
     */
    suspend fun createCategory(category: Category): Result<Unit>

    /**
    Guarda un tipo de animal nuevo.

    - Qué envía ([animalType]): El tipo con su id ya calculado.
    - Qué recibe: Un [Result<Unit>] que confirma si se guardó.
     */
    suspend fun createAnimalType(animalType: AnimalType): Result<Unit>

    /**
    Cambia el nombre y el estado de una categoría.

    - Qué envía: El [id], el nuevo [name] y si queda [active].
    - Qué recibe: Un [Result<Unit>] que confirma si se guardó.
    El id no cambia, así las publicaciones existentes siguen apuntando bien.
     */
    suspend fun updateCategory(id: String, name: String, active: Boolean): Result<Unit>

    /**
    Cambia el nombre y el estado de un tipo de animal.

    - Qué envía: El [id], el nuevo [name] y si queda [active].
    - Qué recibe: Un [Result<Unit>] que confirma si se guardó.
     */
    suspend fun updateAnimalType(id: String, name: String, active: Boolean): Result<Unit>
}