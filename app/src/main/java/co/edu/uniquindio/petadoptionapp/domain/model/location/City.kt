package co.edu.uniquindio.petadoptionapp.domain.model.location

/** Ciudad que se puede elegir en el mapa (registro, publicaciones, explorar): centra el mapa en ella. */
data class City(val name: String, val country: String, val latitude: Double, val longitude: Double) {
    // Como ubicación (sin dirección): sirve de centro para el mapa y los filtros de cercanía.
    fun toLocation(): Location = Location(latitude, longitude, name, country, "")
}

/** Ciudades disponibles en el selector. Armenia primero: es la ciudad de los datos de prueba. */
object CityCatalog {
    val all: List<City> = listOf(
        City("Armenia", "Colombia", 4.5339, -75.6811),
        City("Bogotá", "Colombia", 4.7110, -74.0721),
        City("Medellín", "Colombia", 6.2442, -75.5812),
        City("Cali", "Colombia", 3.4516, -76.5320),
        City("Barranquilla", "Colombia", 10.9685, -74.7813),
        City("Cartagena", "Colombia", 10.3910, -75.4794),
        City("Bucaramanga", "Colombia", 7.1193, -73.1227),
        City("Pereira", "Colombia", 4.8133, -75.6961),
        City("Manizales", "Colombia", 5.0703, -75.5138),
        City("Ibagué", "Colombia", 4.4389, -75.2322),
        City("Santa Marta", "Colombia", 11.2408, -74.1990),
        City("Cúcuta", "Colombia", 7.8939, -72.5078),
        City("Pasto", "Colombia", 1.2136, -77.2811),
        City("Villavicencio", "Colombia", 4.1420, -73.6266),
        City("Neiva", "Colombia", 2.9273, -75.2819),
    )

    // Busca por nombre sin importar mayúsculas ni tildes ("bogota" encuentra "Bogotá").
    fun find(name: String): City? {
        fun String.plain() = java.text.Normalizer.normalize(trim().lowercase(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        val target = name.plain()
        return all.firstOrNull { it.name.plain() == target }
    }
}
