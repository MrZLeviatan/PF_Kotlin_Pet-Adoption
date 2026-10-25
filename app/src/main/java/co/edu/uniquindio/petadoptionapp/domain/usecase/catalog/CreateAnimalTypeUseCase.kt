package co.edu.uniquindio.petadoptionapp.domain.usecase.catalog

import co.edu.uniquindio.petadoptionapp.domain.error.CatalogException
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalType
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.CatalogValidator

/**
Documenta el caso de uso con el que un moderador agrega un tipo de animal.
El id se calcula desde el nombre, así dos nombres equivalentes
 */
class CreateAnimalTypeUseCase(
    // Inyecta el repositorio de catálogos para crear el tipo de animal.
    private val catalogRepository: CatalogRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    // Define la función que recibe el nombre del tipo de animal y devuelve la categoría creada.
    suspend operator fun invoke(name: String): Result<AnimalType> {

        // 1. Solo un moderador activo puede crear categorías.
        requireModerator().getOrElse { return Result.failure(it) }

        // 2. Formato del nombre, sin internet.
        val cleanName = name.trim()
        if (!CatalogValidator.isNameValid(cleanName)) return Result.failure(CatalogException.InvalidName)

        // 3. El id sale del nombre; si ya existe, es un duplicado.
        val id = CatalogValidator.buildId(cleanName)
        if (id.isEmpty()) return Result.failure(CatalogException.InvalidName)
        if (catalogRepository.findAnimalTypeById(id) != null)
            return Result.failure(CatalogException.AnimalTypeAlreadyExists)

        // 4. Guarda, refresca la memoria y devuelve la categoría creada.
        val category = AnimalType(id = id, name = cleanName, active = true)
        return catalogRepository.createAnimalType(category)
            .onSuccess { catalogRepository.refresh() }
            .map { category }
    }
}