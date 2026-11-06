package co.edu.uniquindio.petadoptionapp.domain.usecase.catalog

import co.edu.uniquindio.petadoptionapp.domain.error.CatalogException
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.CatalogValidator

/**
Caso de uso con el que un moderador renombra, activa o desactiva una categoría.
No existe "borrar": desactivar oculta la categoría para publicaciones nuevas sin romper las existentes.
Lo usa el ViewModel de la pantalla de gestión de catálogos del moderador.
**/
// Declara el caso de uso de edición de categorías.
class UpdateCategoryUseCase(
    // Inyecta el repositorio de catálogos para modificar la categoría.
    private val catalogRepository: CatalogRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    // Define la función que recibe el id de la categoría, su nuevo nombre y si queda activa.
    suspend operator fun invoke(id: String, name: String, active: Boolean): Result<Unit> {

        // 1. Solo un moderador activo puede modificar categorías; si no lo es, devuelve el error.
        requireModerator().getOrElse { return Result.failure(it) }

        // 2. Formato del nombre, sin internet: quita espacios sobrantes.
        val cleanName = name.trim()
        // Falla si el nombre no cumple el formato.
        if (!CatalogValidator.isNameValid(cleanName)) return Result.failure(CatalogException.InvalidName)

        // 3. La categoría debe existir.
        catalogRepository.findCategoryById(id)
            // Si no existe, falla con CategoryNotFound.
            ?: return Result.failure(CatalogException.CategoryNotFound)

        // 4. Guarda en Firestore y refresca la caché.
        return catalogRepository.updateCategory(id, cleanName, active)
            // Si se guardó, vuelve a leer los catálogos para actualizar los flujos.
            .onSuccess { catalogRepository.refresh() }
    }
}
