package co.edu.uniquindio.petadoptionapp.domain.usecase.validator

import java.text.Normalizer

/**
Validador del dominio que contiene las reglas de formato de los catálogos, sin red ni dependencias de Android.
Define un objeto singleton que valida nombres y genera el id estable a partir del nombre.
Lo usan los casos de uso de usecase/catalog al crear o editar categorías y tipos de animal.
**/
// Objeto único (singleton) con las reglas de formato de los catálogos.
object CatalogValidator {

    // Define el tamaño mínimo y máximo del nombre de una categoría o tipo de animal.
    const val MIN_NAME_LENGTH = 3
    // Tamaño máximo del nombre.
    const val MAX_NAME_LENGTH = 30

    // Define una función pública que verifica que el nombre esté dentro del rango de tamaño.
    fun isNameValid(name: String): Boolean =
        // Quita espacios y comprueba que la longitud quede dentro del rango.
        name.trim().length in MIN_NAME_LENGTH..MAX_NAME_LENGTH

    // Define una función pública que convierte un nombre en un id estable: minúsculas, sin tildes y con guion bajo.
    // Ejemplo: "Hogar de Paso" -> "hogar_de_paso". Así el id evita duplicados por mayúsculas o tildes.
    fun buildId(name: String): String =
        // Pasa a minúsculas y descompone los caracteres para separar las tildes.
        Normalizer.normalize(name.trim().lowercase(), Normalizer.Form.NFD)
            // Elimina las marcas diacríticas (tildes).
            .replace(Regex("\\p{M}"), "")
            // Reemplaza cualquier grupo de caracteres no alfanuméricos por un guion bajo.
            .replace(Regex("[^a-z0-9]+"), "_")
            // Quita los guiones bajos sobrantes al inicio y al final.
            .trim('_')
}
