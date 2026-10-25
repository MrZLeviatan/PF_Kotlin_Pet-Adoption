package co.edu.uniquindio.petadoptionapp.domain.error

/**
Errores del dominio de catálogos: categorías y tipos de animal.
La UI traduce cada uno a un texto amigable usando strings.xml.
 */
sealed class CatalogException : Exception() {

    // Ocurre cuando el nombre está vacío, es muy corto o muy largo.
    object InvalidName : CatalogException()

    // Ocurre cuando ya existe una categoría con ese nombre (mismo id).
    object CategoryAlreadyExists : CatalogException()

    // Ocurre cuando ya existe un tipo de animal con ese nombre (mismo id).
    object AnimalTypeAlreadyExists : CatalogException()

    // Ocurre cuando la categoría no existe.
    object CategoryNotFound : CatalogException()

    // Ocurre cuando el tipo de animal no existe.
    object AnimalTypeNotFound : CatalogException()

    // Ocurre cuando la categoría existe pero está desactivada para publicaciones nuevas.
    object CategoryInactive : CatalogException()

    // Ocurre cuando el tipo de animal existe pero está desactivado para publicaciones nuevas.
    object AnimalTypeInactive : CatalogException()
}