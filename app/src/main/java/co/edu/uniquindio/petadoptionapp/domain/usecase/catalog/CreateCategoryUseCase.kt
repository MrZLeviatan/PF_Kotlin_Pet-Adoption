package co.edu.uniquindio.petadoptionapp.domain.usecase.catalog

import co.edu.uniquindio.petadoptionapp.domain.error.CatalogException
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.CatalogValidator

/**
Documenta el caso de uso con el que un moderador agrega una categoría nueva.
El id se calcula desde el nombre, así dos nombres equivalentes ("Hogar de paso" y "hogar de Paso") no se duplican.
 */
class CreateCategoryUseCase(
    // Inyecta el repositorio de catálogos para crear la categoría.
    private val catalogRepository: CatalogRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    // Define la función que recibe el nombre de la categoría y devuelve la categoría creada.
    suspend operator fun invoke(name: String): Result<Category> {

        // 1. Solo un moderador activo puede crear categorías.
        requireModerator().getOrElse { return Result.failure(it) }

        // 2. Formato del nombre, sin internet.
        val cleanName = name.trim()
        if (!CatalogValidator.isNameValid(cleanName)) return Result.failure(CatalogException.InvalidName)

        // 3. El id sale del nombre; si ya existe, es un duplicado.
        val id = CatalogValidator.buildId(cleanName)
        if (id.isEmpty()) return Result.failure(CatalogException.InvalidName)
        if (catalogRepository.findCategoryById(id) != null)
            return Result.failure(CatalogException.CategoryAlreadyExists)

        // 4. Guarda, refresca la memoria y devuelve la categoría creada.
        val category = Category(id = id, name = cleanName, active = true)
        return catalogRepository.createCategory(category)
            .onSuccess { catalogRepository.refresh() }
            .map { category }
    }
}