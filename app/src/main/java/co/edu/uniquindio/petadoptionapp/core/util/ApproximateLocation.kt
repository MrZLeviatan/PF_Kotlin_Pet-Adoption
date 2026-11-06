package co.edu.uniquindio.petadoptionapp.core.util

import co.edu.uniquindio.petadoptionapp.core.component.map.GeoPoint
import kotlin.math.cos
import kotlin.math.sin

/**
Ubicación aproximada de una publicación: el punto se corre entre 150 y 300 m en una dirección fija
(calculada con el id), así siempre cae en el mismo sitio pero nunca muestra la dirección exacta.
Se dibuja como un círculo de APPROXIMATE_RADIUS_METERS alrededor.
Se usa en el mapa y el detalle de las publicaciones para proteger la privacidad de quien publica: el desplazamiento
es determinista (depende del hash del id), de modo que el círculo no "salta" cada vez que se abre la pantalla.
**/

// Radio, en metros, del círculo que se dibuja alrededor del punto aproximado.
const val APPROXIMATE_RADIUS_METERS = 400.0

// Calcula el punto desplazado a partir de las coordenadas reales y una semilla (normalmente el id de la publicación).
fun approximatePoint(latitude: Double, longitude: Double, seed: String): GeoPoint {
    // Convierte el hash de la semilla a un número positivo de 32 bits.
    val hash = seed.hashCode().toLong() and 0xffffffffL
    // Dirección del desplazamiento: un ángulo entre 0 y 359 grados, convertido a radianes.
    val angle = (hash % 360) * Math.PI / 180
    // Distancia del desplazamiento en metros: entre 150 y 299.
    val distance = 150.0 + (hash / 360 % 150)
    // Desplazamiento en grados de latitud (un grado ≈ 111.320 m).
    val dLat = distance * cos(angle) / 111_320.0
    // Desplazamiento en grados de longitud, corregido por la latitud (con un mínimo para evitar dividir por cero en los polos).
    val dLng = distance * sin(angle) / (111_320.0 * cos(Math.toRadians(latitude)).coerceAtLeast(0.01))
    // Devuelve el punto desplazado.
    return GeoPoint(latitude + dLat, longitude + dLng)
}
