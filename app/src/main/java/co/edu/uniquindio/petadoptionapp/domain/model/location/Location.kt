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


    /**
    Calcula la distancia en kilómetros hasta otra ubicación.
    Devuelve `null` si alguna de las dos no tiene coordenadas.
     */
    fun distanceKmTo(other: Location): Double? {
        // Obtiene la latitud actual o retorna nulo si no está disponible.
        val lat1 = latitude ?: return null
        // Obtiene la longitud actual o retorna nulo si no está disponible.
        val lon1 = longitude ?: return null
        // Obtiene la latitud de la otra ubicación o retorna nulo si no está disponible.
        val lat2 = other.latitude ?: return null
        // Obtiene la longitud de la otra ubicación o retorna nulo si no está disponible.
        val lon2 = other.longitude ?: return null

        // Convierte la diferencia de latitud a radianes.
        val dLat = Math.toRadians(lat2 - lat1)
        // Convierte la diferencia de longitud a radianes.
        val dLon = Math.toRadians(lon2 - lon1)
        // Calcula el valor parcial 'a' utilizando la fórmula de Haversine con las latitudes y longitudes.
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        // Multiplica el resultado del arco tangente por el radio medio de la Tierra en kilómetros (6371.0) para obtener la distancia final.
        return 6371.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }
}