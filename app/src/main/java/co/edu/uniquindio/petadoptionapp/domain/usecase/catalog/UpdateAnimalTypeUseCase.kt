package co.edu.uniquindio.petadoptionapp.domain.usecase.catalog

import co.edu.uniquindio.petadoptionapp.domain.error.CatalogException
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.CatalogValidator

/**
Documenta el caso de uso con el que un moderador renombra, activa o desactiva un tipo de animal.
No existe "borrar": desactivar oculta la categoría para publicaciones nuevas sin romper las existentes.
 */
class UpdateAnimalTypeUseCase(
    // Inyecta el repositorio de catálogos para modificar la categoría.
    private val catalogRepository: CatalogRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    // Define la función que recibe el id de la categoría, su nuevo nombre y si queda activa.
    suspend operator fun invoke(id: String, name: String, active: Boolean): Result<Unit> {

        // 1. Solo un moderador activo puede modificar categorías.
        requireModerator().getOrElse { return Result.failure(it) }

        // 2. Formato del nombre, sin internet.
        val cleanName = name.trim()
        if (!CatalogValidator.isNameValid(cleanName)) return Result.failure(CatalogException.InvalidName)

        // 3. La categoría debe existir.
        catalogRepository.findAnimalTypeById(id)
            ?: return Result.failure(CatalogException.AnimalTypeNotFound)

        // 4. Guarda y refresca la memoria.
        return catalogRepository.updateAnimalType(id, cleanName, active)
            .onSuccess { catalogRepository.refresh() }
    }
}