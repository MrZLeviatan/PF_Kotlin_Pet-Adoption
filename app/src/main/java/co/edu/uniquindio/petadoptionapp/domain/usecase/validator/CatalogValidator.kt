package co.edu.uniquindio.petadoptionapp.domain.usecase.validator

import java.text.Normalizer

/**
Documenta el objeto que contiene las reglas de formato de los catálogos, sin red ni dependencias de Android.
Define un objeto singleton que valida nombres y genera el id estable a partir del nombre.
 */
object CatalogValidator {

    // Define el tamaño mínimo y máximo del nombre de una categoría o tipo de animal.
    const val MIN_NAME_LENGTH = 3
    const val MAX_NAME_LENGTH = 30

    // Define una función pública que verifica que el nombre esté dentro del rango de tamaño.
    fun isNameValid(name: String): Boolean =
        name.trim().length in MIN_NAME_LENGTH..MAX_NAME_LENGTH

    // Define una función pública que convierte un nombre en un id estable: minúsculas, sin tildes y con guion bajo.
    // Ejemplo: "Hogar de Paso" -> "hogar_de_paso". Así el id evita duplicados por mayúsculas o tildes.
    fun buildId(name: String): String =
        Normalizer.normalize(name.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
}