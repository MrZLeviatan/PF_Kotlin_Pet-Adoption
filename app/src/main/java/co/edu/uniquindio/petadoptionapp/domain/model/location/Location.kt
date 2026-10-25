package co.edu.uniquindio.petadoptionapp.domain.model.location

/**
 Ubicación geográfica reutilizable.

 Se usa tanto en el perfil de [User] (su dirección) como en las
 publicaciones y reportes (dónde está o se perdió la mascota). Las
 coordenadas se obtienen desde el mapa (Google Maps o Mapbox) y se
 guardan junto con los datos legibles (ciudad, país, dirección).

 Latitud y longitud son nulas mientras no se haya elegido un punto en el
 mapa. Para saber si la ubicación ya es utilizable, use [hasCoordinates].
 */
data class Location(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val city: String = "",
    val country: String = "",
    val address: String = ""
) {

    /**
     Indica si hay coordenadas válidas (no nulas y dentro del rango real).
     Las publicaciones deben validar esto antes de guardarse.
     */
    fun hasCoordinates(): Boolean =
        latitude != null && longitude != null &&
                latitude in -90.0..90.0 &&
                longitude in -180.0..180.0
}