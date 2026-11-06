package co.edu.uniquindio.petadoptionapp.domain.model.post

import kotlinx.serialization.Serializable


/**
Modelo de dominio: tipo de animal del catálogo (perro, gato, ave...). Un moderador puede
agregar más. Las publicaciones lo referencian por [id].
Lo usan CatalogRepository y los casos de uso de usecase/catalog.
**/
// Permite convertir el tipo de animal a/desde el formato de almacenamiento.
@Serializable
// Declara el tipo de animal como clase de datos inmutable.
data class AnimalType(
    // Identificador del tipo (derivado de su nombre).
    val id: String = "",
    // Nombre visible del tipo de animal.
    val name: String = "",
    // Lo oculta para publicaciones nuevas.
    val active: Boolean = true
)
