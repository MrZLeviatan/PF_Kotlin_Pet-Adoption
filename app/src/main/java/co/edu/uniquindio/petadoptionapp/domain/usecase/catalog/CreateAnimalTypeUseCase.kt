package co.edu.uniquindio.petadoptionapp.domain.usecase.catalog

import co.edu.uniquindio.petadoptionapp.domain.error.CatalogException
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalType
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.CatalogValidator

/**
Caso de uso con el que un moderador agrega un tipo de animal al catálogo.
El id se calcula desde el nombre, así dos nombres equivalentes ("Perro" y "perro") no se duplican.
Guarda en Firestore mediante CatalogRepository y refresca la caché de catálogos.
Lo usa el ViewModel de la pantalla de gestión de catálogos del moderador.
**/
// Declara el caso de uso de creación de tipos de animal.
class CreateAnimalTypeUseCase(
    // Inyecta el repositorio de catálogos para crear el tipo de animal.
    private val catalogRepository: CatalogRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    // Define la función que recibe el nombre del tipo de animal y devuelve el tipo creado.
    suspend operator fun invoke(name: String): Result<AnimalType> {

        // 1. Solo un moderador activo puede crear tipos de animal; si no lo es, devuelve el error.
        requireModerator().getOrElse { return Result.failure(it) }

        // 2. Formato del nombre, sin internet: quita espacios sobrantes.
        val cleanName = name.trim()
        // Falla si el nombre no cumple el formato.
        if (!CatalogValidator.isNameValid(cleanName)) return Result.failure(CatalogException.InvalidName)

        // 3. El id sale del nombre; si ya existe, es un duplicado.
        val id = CatalogValidator.buildId(cleanName)
        // Falla si el nombre no produce un id utilizable.
        if (id.isEmpty()) return Result.failure(CatalogException.InvalidName)
        // Comprueba si ya existe un tipo con ese id.
        if (catalogRepository.findAnimalTypeById(id) != null)
            // Falla con AnimalTypeAlreadyExists si es un duplicado.
            return Result.failure(CatalogException.AnimalTypeAlreadyExists)

        // 4. Guarda, refresca la caché y devuelve el tipo creado.
        val category = AnimalType(id = id, name = cleanName, active = true)
        // Guarda el nuevo tipo en Firestore.
        return catalogRepository.createAnimalType(category)
            // Si se guardó, vuelve a leer los catálogos para actualizar los flujos.
            .onSuccess { catalogRepository.refresh() }
            // Devuelve el tipo de animal creado.
            .map { category }
    }
}
