package co.edu.uniquindio.petadoptionapp.domain.model.post

import kotlinx.serialization.Serializable


/**
 Tipo de animal del catálogo (perro, gato, ave...). Un moderador puede
 agregar más. Las publicaciones lo referencian por [id].
 */
@Serializable
data class AnimalType(
    val id: String = "",
    val name: String = "",
    // Lo oculta para publicaciones nuevas.
    val active: Boolean = true
)