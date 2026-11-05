package co.edu.uniquindio.petadoptionapp.core.util

import co.edu.uniquindio.petadoptionapp.core.component.map.GeoPoint
import kotlin.math.cos
import kotlin.math.sin

/*
 Ubicación aproximada de una publicación: el punto se corre entre 150 y 300 m en una dirección fija
 (calculada con el id), así siempre cae en el mismo sitio pero nunca muestra la dirección exacta.
 Se dibuja como un círculo de APPROXIMATE_RADIUS_METERS alrededor.
 */

const val APPROXIMATE_RADIUS_METERS = 400.0

fun approximatePoint(latitude: Double, longitude: Double, seed: String): GeoPoint {
    val hash = seed.hashCode().toLong() and 0xffffffffL
    val angle = (hash % 360) * Math.PI / 180
    val distance = 150.0 + (hash / 360 % 150)   // metros
    val dLat = distance * cos(angle) / 111_320.0
    val dLng = distance * sin(angle) / (111_320.0 * cos(Math.toRadians(latitude)).coerceAtLeast(0.01))
    return GeoPoint(latitude + dLat, longitude + dLng)
}
