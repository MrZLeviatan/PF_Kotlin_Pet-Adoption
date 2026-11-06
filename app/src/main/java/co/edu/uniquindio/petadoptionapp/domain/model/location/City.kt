package co.edu.uniquindio.petadoptionapp.domain.model.location

/**
Modelo de dominio: ciudades que se pueden elegir en el mapa (registro, publicaciones, explorar).
[City] representa una ciudad con sus coordenadas centrales y [CityCatalog] es la lista
fija de ciudades disponibles en el selector. Al elegir una, el mapa se centra en ella.
Lo usan los selectores de ubicación de la UI y los filtros de cercanía.
**/
// Ciudad con nombre, país y coordenadas de su centro.
data class City(val name: String, val country: String, val latitude: Double, val longitude: Double) {
    // Como ubicación (sin dirección): sirve de centro para el mapa y los filtros de cercanía.
    fun toLocation(): Location = Location(latitude, longitude, name, country, "")
}

/** Ciudades disponibles en el selector. Armenia primero: es la ciudad de los datos de prueba. */
object CityCatalog {
    // Lista inmutable de ciudades ofrecidas en el selector.
    val all: List<City> = listOf(
        // Armenia (ciudad por defecto de los datos de prueba).
        City("Armenia", "Colombia", 4.5339, -75.6811),
        // Bogotá.
        City("Bogotá", "Colombia", 4.7110, -74.0721),
        // Medellín.
        City("Medellín", "Colombia", 6.2442, -75.5812),
        // Cali.
        City("Cali", "Colombia", 3.4516, -76.5320),
        // Barranquilla.
        City("Barranquilla", "Colombia", 10.9685, -74.7813),
        // Cartagena.
        City("Cartagena", "Colombia", 10.3910, -75.4794),
        // Bucaramanga.
        City("Bucaramanga", "Colombia", 7.1193, -73.1227),
        // Pereira.
        City("Pereira", "Colombia", 4.8133, -75.6961),
        // Manizales.
        City("Manizales", "Colombia", 5.0703, -75.5138),
        // Ibagué.
        City("Ibagué", "Colombia", 4.4389, -75.2322),
        // Santa Marta.
        City("Santa Marta", "Colombia", 11.2408, -74.1990),
        // Cúcuta.
        City("Cúcuta", "Colombia", 7.8939, -72.5078),
        // Pasto.
        City("Pasto", "Colombia", 1.2136, -77.2811),
        // Villavicencio.
        City("Villavicencio", "Colombia", 4.1420, -73.6266),
        // Neiva.
        City("Neiva", "Colombia", 2.9273, -75.2819),
    )

    // Busca por nombre sin importar mayúsculas ni tildes ("bogota" encuentra "Bogotá").
    fun find(name: String): City? {
        // Función local que pasa el texto a minúsculas y lo descompone para separar las tildes.
        fun String.plain() = java.text.Normalizer.normalize(trim().lowercase(), java.text.Normalizer.Form.NFD)
            // Elimina las marcas diacríticas (tildes) que quedaron separadas.
            .replace(Regex("\\p{Mn}+"), "")
        // Normaliza el nombre buscado.
        val target = name.plain()
        // Devuelve la primera ciudad cuyo nombre normalizado coincida, o nulo.
        return all.firstOrNull { it.name.plain() == target }
    }
}
