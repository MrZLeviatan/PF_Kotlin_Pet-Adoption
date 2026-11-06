package co.edu.uniquindio.petadoptionapp.core.component.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location

/**
Modelos de lo que dibuja AppMap (MapLibre). El resto de la app solo usa estos modelos, no la librería del mapa,
de modo que si se cambiara de proveedor de mapas solo habría que tocar el paquete map.
Incluye el punto geográfico, los marcadores (publicaciones, pin elegido y persona) y las zonas circulares.
**/

// Punto del mapa expresado en grados de latitud y longitud.
data class GeoPoint(val latitude: Double, val longitude: Double)

// Convierte una ubicación del dominio en un punto del mapa; devuelve null si la ubicación no tiene coordenadas.
fun Location.toGeoPoint(): GeoPoint? =
    // Solo convierte si ambas coordenadas existen (por eso es seguro usar !!).
    if (hasCoordinates()) GeoPoint(latitude!!, longitude!!) else null

// Tipo de marcador: una publicación, el punto elegido (pin) o la persona.
enum class MarkerKind { POST, PIN, USER }

// Forma del marcador: círculo (la mayoría) o cuadrado redondeado (lugares, como en el diseño).
enum class MarkerShape { CIRCLE, ROUNDED_SQUARE }

/**
Marcador; [id] vuelve en onMarkerClick. Con [icon] se dibuja el ícono en blanco sobre el color
(ej. huella en adopción, hospital en veterinaria); sin ícono, un círculo de color.
**/
data class MapMarker(
    // Identificador que se devuelve al tocar el marcador (normalmente el id de la publicación).
    val id: String,
    // Posición del marcador en el mapa.
    val point: GeoPoint,
    // Color de fondo de la forma del marcador.
    val color: Color,
    // Tipo de marcador; por defecto, una publicación.
    val kind: MarkerKind = MarkerKind.POST,
    // Si está seleccionado se dibuja más grande y por encima de los demás.
    val selected: Boolean = false,
    // Ícono opcional dibujado dentro de la forma.
    val icon: ImageVector? = null,
    // Forma del marcador; por defecto, círculo.
    val shape: MarkerShape = MarkerShape.CIRCLE,
)

// Zona circular (ej. la ubicación aproximada de una publicación), con su centro, radio en metros y color.
data class MapArea(val center: GeoPoint, val radiusMeters: Double, val color: Color)
