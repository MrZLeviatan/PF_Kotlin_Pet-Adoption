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
 */
@Singleton
class FirestoreCatalogRepository @Inject constructor(
    private val db: FirebaseFirestore,
) : CatalogRepository {

    private val categoryCollection = db.collection(CATEGORIES)
    private val animalTypeCollection = db.collection(ANIMAL_TYPES)

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    override val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _animalTypes = MutableStateFlow<List<AnimalType>>(emptyList())
    override val animalTypes: StateFlow<List<AnimalType>> = _animalTypes.asStateFlow()

    // Los catálogos se pueden leer sin sesión (el registro y el login no los necesitan, pero no hacen daño).
    init {
        categoryCollection.addSnapshotListener { snapshot, _ ->
            snapshot?.let { _categories.value = it.toModels() }
        }
        animalTypeCollection.addSnapshotListener { snapshot, _ ->
            snapshot?.let { _animalTypes.value = it.toModels() }
        }
    }

    override suspend fun refresh(): Result<Unit> = runCatching {
        _categories.value = categoryCollection.get().await().toModels()
        _animalTypes.value = animalTypeCollection.get().await().toModels()
    }

    override suspend fun findCategoryById(id: String): Category? =
        _categories.value.firstOrNull { it.id == id }
            ?: runCatching { categoryCollection.document(id).get().await().toModel<Category>() }.getOrNull()

    override suspend fun findAnimalTypeById(id: String): AnimalType? =
        _animalTypes.value.firstOrNull { it.id == id }
            ?: runCatching { animalTypeCollection.document(id).get().await().toModel<AnimalType>() }.getOrNull()

    override suspend fun createCategory(category: Category): Result<Unit> = runCatching {
        // Id fijo: si ya existe, no se duplica.
        val ref = categoryCollection.document(category.id)
        if (ref.get().await().exists()) throw CatalogException.CategoryAlreadyExists
        ref.set(category.toFirestore()).await()
        Unit
    }

    override suspend fun createAnimalType(animalType: AnimalType): Result<Unit> = runCatching {
        val ref = animalTypeCollection.document(animalType.id)
        if (ref.get().await().exists()) throw CatalogException.AnimalTypeAlreadyExists
        ref.set(animalType.toFirestore()).await()
        Unit
    }

    // El id no cambia: las publicaciones existentes siguen apuntando bien.
    override suspend fun updateCategory(id: String, name: String, active: Boolean): Result<Unit> = runCatching {
        val ref = categoryCollection.document(id)
        if (!ref.get().await().exists()) throw CatalogException.CategoryNotFound
        ref.update(mapOf("name" to name, "active" to active)).await()
        Unit
    }

    override suspend fun updateAnimalType(id: String, name: String, active: Boolean): Result<Unit> = runCatching {
        val ref = animalTypeCollection.document(id)
        if (!ref.get().await().exists()) throw CatalogException.AnimalTypeNotFound
        ref.update(mapOf("name" to name, "active" to active)).await()
        Unit
    }
}
