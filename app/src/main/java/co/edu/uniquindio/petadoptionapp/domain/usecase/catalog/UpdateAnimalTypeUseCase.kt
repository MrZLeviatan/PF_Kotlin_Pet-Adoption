package co.edu.uniquindio.petadoptionapp.domain.usecase.catalog

import co.edu.uniquindio.petadoptionapp.domain.error.CatalogException
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.CatalogValidator

/**
Caso de uso con el que un moderador renombra, activa o desactiva un tipo de animal.
No existe "borrar": desactivar oculta el tipo para publicaciones nuevas sin romper las existentes.
Lo usa el ViewModel de la pantalla de gestión de catálogos del moderador.
**/
// Declara el caso de uso de edición de tipos de animal.
class UpdateAnimalTypeUseCase(
    // Inyecta el repositorio de catálogos para modificar el tipo de animal.
    private val catalogRepository: CatalogRepository,
    // Inyecta el caso de uso que confirma que quien pide es un moderador activo.
    private val requireModerator: RequireModeratorUseCase
) {
    // Define la función que recibe el id del tipo, su nuevo nombre y si queda activo.
    suspend operator fun invoke(id: String, name: String, active: Boolean): Result<Unit> {

        // 1. Solo un moderador activo puede modificar tipos de animal; si no lo es, devuelve el error.
        requireModerator().getOrElse { return Result.failure(it) }

        // 2. Formato del nombre, sin internet: quita espacios sobrantes.
        val cleanName = name.trim()
        // Falla si el nombre no cumple el formato.
        if (!CatalogValidator.isNameValid(cleanName)) return Result.failure(CatalogException.InvalidName)

        // 3. El tipo de animal debe existir.
        catalogRepository.findAnimalTypeById(id)
            // Si no existe, falla con AnimalTypeNotFound.
            ?: return Result.failure(CatalogException.AnimalTypeNotFound)

        // 4. Guarda en Firestore y refresca la caché.
        return catalogRepository.updateAnimalType(id, cleanName, active)
            // Si se guardó, vuelve a leer los catálogos para actualizar los flujos.
            .onSuccess { catalogRepository.refresh() }
    }
}
