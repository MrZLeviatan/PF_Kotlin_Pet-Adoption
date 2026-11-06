package co.edu.uniquindio.petadoptionapp.data.repository.firebase

import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.ANIMAL_TYPES
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.CATEGORIES
import co.edu.uniquindio.petadoptionapp.domain.error.CatalogException
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalType
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
Catálogos en Firestore: `categories/{id}` y `animalTypes/{id}`.
Son pequeños: se escuchan en vivo desde que abre la app (y se guardan en la copia local),
así cualquier cambio de un moderador llega solo a todas las pantallas.

Implementa CatalogRepository del dominio y se enlaza en RepositoryModule. Expone los catálogos
como StateFlow para que los ViewModels (crear publicación, filtros, panel del moderador) los
observen, y ofrece operaciones de creación y edición que usan los casos de uso del moderador.
**/
// Una sola instancia: los listeners en vivo se registran una sola vez para toda la app.
@Singleton
// Hilt inyecta la instancia de Firestore provista por FirebaseModule.
class FirestoreCatalogRepository @Inject constructor(
    // Base de datos Firestore.
    private val db: FirebaseFirestore,
) : CatalogRepository {

    // Referencia a la colección de categorías.
    private val categoryCollection = db.collection(CATEGORIES)
    // Referencia a la colección de tipos de animal.
    private val animalTypeCollection = db.collection(ANIMAL_TYPES)

    // Estado interno mutable con la lista actual de categorías.
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    // Vista de solo lectura de las categorías que observan las pantallas.
    override val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    // Estado interno mutable con la lista actual de tipos de animal.
    private val _animalTypes = MutableStateFlow<List<AnimalType>>(emptyList())
    // Vista de solo lectura de los tipos de animal.
    override val animalTypes: StateFlow<List<AnimalType>> = _animalTypes.asStateFlow()

    // Los catálogos se pueden leer sin sesión (el registro y el login no los necesitan, pero no hacen daño).
    init {
        // Escucha en vivo la colección de categorías y actualiza el estado en cada cambio.
        categoryCollection.addSnapshotListener { snapshot, _ ->
            // Si llegó una instantánea (no un error), convierte sus documentos en modelos y los publica.
            snapshot?.let { _categories.value = it.toModels() }
        }
        // Escucha en vivo la colección de tipos de animal.
        animalTypeCollection.addSnapshotListener { snapshot, _ ->
            // Publica los tipos de animal leídos.
            snapshot?.let { _animalTypes.value = it.toModels() }
        }
    }

    // Fuerza una lectura completa de ambos catálogos (por ejemplo, al reintentar tras un error).
    override suspend fun refresh(): Result<Unit> = runCatching {
        // Lee todas las categorías y actualiza el estado.
        _categories.value = categoryCollection.get().await().toModels()
        // Lee todos los tipos de animal y actualiza el estado.
        _animalTypes.value = animalTypeCollection.get().await().toModels()
    }

    // Busca una categoría por id: primero en memoria y, si no está, directamente en Firestore.
    override suspend fun findCategoryById(id: String): Category? =
        // Intenta encontrarla en la lista ya cargada.
        _categories.value.firstOrNull { it.id == id }
            // Si no está, lee el documento; ante cualquier error devuelve null.
            ?: runCatching { categoryCollection.document(id).get().await().toModel<Category>() }.getOrNull()

    // Busca un tipo de animal por id: primero en memoria y, si no está, en Firestore.
    override suspend fun findAnimalTypeById(id: String): AnimalType? =
        // Intenta encontrarlo en la lista ya cargada.
        _animalTypes.value.firstOrNull { it.id == id }
            // Si no está, lee el documento; ante cualquier error devuelve null.
            ?: runCatching { animalTypeCollection.document(id).get().await().toModel<AnimalType>() }.getOrNull()

    // Crea una nueva categoría (acción de moderador).
    override suspend fun createCategory(category: Category): Result<Unit> = runCatching {
        // Id fijo: si ya existe, no se duplica.
        val ref = categoryCollection.document(category.id)
        // Si el documento ya existe, informa que la categoría está repetida.
        if (ref.get().await().exists()) throw CatalogException.CategoryAlreadyExists
        // Guarda la categoría convertida a mapa de Firestore.
        ref.set(category.toFirestore()).await()
        // Devuelve Unit explícitamente como resultado exitoso.
        Unit
    }

    // Crea un nuevo tipo de animal (acción de moderador).
    override suspend fun createAnimalType(animalType: AnimalType): Result<Unit> = runCatching {
        // Referencia al documento con el id fijo del tipo de animal.
        val ref = animalTypeCollection.document(animalType.id)
        // Si ya existe, informa que está repetido.
        if (ref.get().await().exists()) throw CatalogException.AnimalTypeAlreadyExists
        // Guarda el tipo de animal en Firestore.
        ref.set(animalType.toFirestore()).await()
        // Resultado exitoso.
        Unit
    }

    // El id no cambia: las publicaciones existentes siguen apuntando bien.
    override suspend fun updateCategory(id: String, name: String, active: Boolean): Result<Unit> = runCatching {
        // Referencia a la categoría a editar.
        val ref = categoryCollection.document(id)
        // Si no existe, informa que no se encontró.
        if (!ref.get().await().exists()) throw CatalogException.CategoryNotFound
        // Actualiza solo el nombre y el estado activo, sin tocar el resto del documento.
        ref.update(mapOf("name" to name, "active" to active)).await()
        // Resultado exitoso.
        Unit
    }

    // Edita un tipo de animal conservando su id.
    override suspend fun updateAnimalType(id: String, name: String, active: Boolean): Result<Unit> = runCatching {
        // Referencia al tipo de animal a editar.
        val ref = animalTypeCollection.document(id)
        // Si no existe, informa que no se encontró.
        if (!ref.get().await().exists()) throw CatalogException.AnimalTypeNotFound
        // Actualiza solo el nombre y el estado activo.
        ref.update(mapOf("name" to name, "active" to active)).await()
        // Resultado exitoso.
        Unit
    }
}
