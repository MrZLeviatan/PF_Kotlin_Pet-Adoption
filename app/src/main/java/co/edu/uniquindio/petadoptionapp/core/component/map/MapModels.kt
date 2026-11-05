package co.edu.uniquindio.petadoptionapp.core.component.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location

/*
 Lo que dibuja AppMap (MapLibre). El resto de la app solo usa estos modelos, no la librería del mapa.
 */

/** Punto del mapa (grados). */
data class GeoPoint(val latitude: Double, val longitude: Double)

fun Location.toGeoPoint(): GeoPoint? =
    if (hasCoordinates()) GeoPoint(latitude!!, longitude!!) else null

/** Tipo de marcador: una publicación, el punto elegido (pin) o la persona. */
enum class MarkerKind { POST, PIN, USER }

/** Forma del marcador: círculo (la mayoría) o cuadrado redondeado (lugares, como en el diseño). */
enum class MarkerShape { CIRCLE, ROUNDED_SQUARE }

/**
 Marcador; [id] vuelve en onMarkerClick. Con [icon] se dibuja el ícono en blanco sobre el color
 (ej. huella en adopción, hospital en veterinaria); sin ícono, un círculo de color.
 */
data class MapMarker(
    val id: String,
    val point: GeoPoint,
    val color: Color,
    val kind: MarkerKind = MarkerKind.POST,
    val selected: Boolean = false,
    val icon: ImageVector? = null,
    val shape: MarkerShape = MarkerShape.CIRCLE,
)

/** Zona circular (ej. la ubicación aproximada de una publicación). */
data class MapArea(val center: GeoPoint, val radiusMeters: Double, val color: Color)
