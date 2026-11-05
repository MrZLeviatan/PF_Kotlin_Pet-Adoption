package co.edu.uniquindio.petadoptionapp.core.component.map

import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.cos
import kotlin.math.sin
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

// Estilo de calles gratuito de OpenFreeMap (datos de OpenStreetMap): no necesita llaves.
private const val STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

private const val AREAS_SOURCE = "app-areas"
private const val MARKERS_SOURCE = "app-markers"
private const val AREAS_LAYER = "app-areas-layer"
private const val MARKERS_LAYER = "app-markers-layer"

/**
 Mapa de la app con MapLibre (código abierto, sin tokens) y las calles de OpenFreeMap.
 Dibuja publicaciones (forma de color con el ícono de su categoría; la elegida más grande), el punto elegido (pin),
 la persona (punto azul con ícono de persona)
 y zonas en metros reales (ej. la ubicación aproximada). Tocar un marcador de publicación avisa su id;
 tocar el mapa avisa el punto. Al cambiar [center] o [zoom] la cámara se mueve allá.
 */
@Composable
fun AppMap(
    center: GeoPoint,
    modifier: Modifier = Modifier,
    zoom: Double = 14.0,
    markers: List<MapMarker> = emptyList(),
    areas: List<MapArea> = emptyList(),
    userLocation: GeoPoint? = null,
    onMapClick: ((GeoPoint) -> Unit)? = null,
    onMarkerClick: ((String) -> Unit)? = null,
    interactive: Boolean = true,
) {
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnMapClick by rememberUpdatedState(onMapClick)
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    // Imagen de cada marcador (forma, color e ícono) y la de la persona.
    val images = rememberMarkerImages(markers, includeUser = userLocation != null)

    // El MapView y lo último que se pidió dibujar (el estilo llega por internet, así que se aplica al cargar).
    val holder = remember { MapHolder() }
    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                holder.map = map
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(center.latitude, center.longitude))
                    .zoom(zoom)
                    .build()
                holder.cameraKey = center to zoom
                map.setStyle(Style.Builder().fromUri(STYLE_URL)) { style ->
                    style.addSource(GeoJsonSource(AREAS_SOURCE))
                    style.addSource(GeoJsonSource(MARKERS_SOURCE))
                    style.addLayer(
                        FillLayer(AREAS_LAYER, AREAS_SOURCE).withProperties(
                            PropertyFactory.fillColor(Expression.toColor(Expression.get("color"))),
                            PropertyFactory.fillOpacity(0.22f),
                            PropertyFactory.fillOutlineColor(Expression.toColor(Expression.get("color"))),
                        )
                    )
                    // Cada marcador es una imagen; la elegida y la persona van encima de las demás.
                    style.addLayer(
                        SymbolLayer(MARKERS_LAYER, MARKERS_SOURCE).withProperties(
                            PropertyFactory.iconImage(Expression.get("image")),
                            PropertyFactory.iconAllowOverlap(true),
                            PropertyFactory.iconIgnorePlacement(true),
                            PropertyFactory.symbolSortKey(Expression.toNumber(Expression.get("order"))),
                        )
                    )
                    holder.style = style
                    holder.render()
                }
                // Primero un marcador de publicación cerca del toque; si no hay, el punto del mapa.
                map.addOnMapClickListener { latLng ->
                    if (!holder.interactive) return@addOnMapClickListener false
                    val screen = map.projection.toScreenLocation(latLng)
                    val r = 24 * density
                    val hit = map.queryRenderedFeatures(RectF(screen.x - r, screen.y - r, screen.x + r, screen.y + r), MARKERS_LAYER)
                        .firstOrNull { it.getStringProperty("kind") == MarkerKind.POST.name }
                    val markerClick = currentOnMarkerClick
                    when {
                        hit != null && markerClick != null -> markerClick(hit.getStringProperty("id"))
                        else -> currentOnMapClick?.invoke(GeoPoint(latLng.latitude, latLng.longitude))
                    }
                    true
                }
            }
        }
    }

    // El MapView necesita los eventos de ciclo de vida de la pantalla.
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = {
            holder.interactive = interactive
            holder.markers = markers
            holder.areas = areas
            holder.userLocation = userLocation
            holder.images = images
            holder.map?.let { map ->
                map.uiSettings.setAllGesturesEnabled(interactive)
                // La cámara solo se mueve si cambió el centro o el zoom pedido (no en cada redibujo).
                if (holder.cameraKey != center to zoom) {
                    holder.cameraKey = center to zoom
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(center.latitude, center.longitude), zoom))
                }
            }
            holder.render()
        },
    )
}

// Estado del mapa nativo entre recomposiciones: el mapa, su estilo y lo que hay que dibujar.
private class MapHolder {
    var map: MapLibreMap? = null
    var style: Style? = null
    var cameraKey: Pair<GeoPoint, Double>? = null
    var interactive = true
    var markers: List<MapMarker> = emptyList()
    var areas: List<MapArea> = emptyList()
    var userLocation: GeoPoint? = null
    var images: Map<String, android.graphics.Bitmap> = emptyMap()

    fun render() {
        val style = style ?: return
        style.getSourceAs<GeoJsonSource>(AREAS_SOURCE)?.setGeoJson(
            FeatureCollection.fromFeatures(areas.map { area ->
                Feature.fromGeometry(Polygon.fromLngLats(listOf(circle(area.center, area.radiusMeters)))).apply {
                    addStringProperty("color", area.color.hex())
                }
            })
        )
        // Las imágenes nuevas se agregan al estilo (las que ya están se reutilizan).
        images.forEach { (key, bitmap) -> if (style.getImage(key) == null) style.addImage(key, bitmap) }
        val features = markers.map { marker ->
            Feature.fromGeometry(Point.fromLngLat(marker.point.longitude, marker.point.latitude)).apply {
                addStringProperty("id", marker.id)
                addStringProperty("kind", marker.kind.name)
                addStringProperty("image", marker.imageKey())
                addNumberProperty("order", if (marker.selected) 2 else 0)
            }
        } + listOfNotNull(userLocation?.let { user ->
            Feature.fromGeometry(Point.fromLngLat(user.longitude, user.latitude)).apply {
                addStringProperty("id", "user")
                addStringProperty("kind", MarkerKind.USER.name)
                addStringProperty("image", USER_IMAGE_KEY)
                addNumberProperty("order", 1)
            }
        })
        style.getSourceAs<GeoJsonSource>(MARKERS_SOURCE)?.setGeoJson(FeatureCollection.fromFeatures(features))
    }
}

private fun Color.hex(): String = String.format("#%06X", toArgb() and 0xFFFFFF)

// Círculo en metros reales como polígono de 48 lados.
private fun circle(center: GeoPoint, radiusMeters: Double): List<Point> {
    val latRad = Math.toRadians(center.latitude)
    return (0..48).map { i ->
        val angle = 2 * Math.PI * i / 48
        val dLat = radiusMeters * cos(angle) / 111_320.0
        val dLng = radiusMeters * sin(angle) / (111_320.0 * cos(latRad))
        Point.fromLngLat(center.longitude + dLng, center.latitude + dLat)
    }
}
