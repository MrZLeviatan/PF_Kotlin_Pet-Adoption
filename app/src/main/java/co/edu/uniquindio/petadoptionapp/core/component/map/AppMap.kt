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

/**
Componente de mapa reutilizable de toda la aplicación, construido sobre MapLibre (librería de código abierto que no
requiere tokens ni llaves de API). Se usa en el feed/mapa de publicaciones, en el detalle de una publicación y en el
selector de ubicación ([LocationPicker]).

Cómo funciona:
- El [MapView] nativo de MapLibre se crea una sola vez y se incrusta en Compose mediante [AndroidView].
- El estilo base (calles, nombres, colores) se descarga de OpenFreeMap ("liberty"), que usa datos de OpenStreetMap.
- Sobre ese estilo se agregan dos fuentes GeoJSON propias: una para las zonas (círculos en metros reales, dibujados
  con una [FillLayer] semitransparente) y otra para los marcadores (puntos dibujados con una [SymbolLayer]).
- Cada marcador se pinta como una imagen (bitmap) generada en [rememberMarkerImages]; la capa de símbolos solo
  indica qué imagen usar en cada punto mediante la propiedad "image" del Feature.
- Como el estilo llega por internet, todo lo que se pide dibujar se guarda en un [MapHolder] y se aplica en cuanto
  el estilo termina de cargar (y luego en cada recomposición).
- El MapView se sincroniza con el ciclo de vida de la pantalla para pausar/reanudar el renderizado y liberarse al salir.
**/

// Estilo de calles gratuito de OpenFreeMap (datos de OpenStreetMap): no necesita llaves.
private const val STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

// Identificador de la fuente GeoJSON que contiene los polígonos de las zonas.
private const val AREAS_SOURCE = "app-areas"
// Identificador de la fuente GeoJSON que contiene los puntos de los marcadores.
private const val MARKERS_SOURCE = "app-markers"
// Identificador de la capa de relleno que pinta las zonas.
private const val AREAS_LAYER = "app-areas-layer"
// Identificador de la capa de símbolos que pinta los marcadores (también se usa para detectar toques sobre ellos).
private const val MARKERS_LAYER = "app-markers-layer"

/**
Mapa de la app con MapLibre (código abierto, sin tokens) y las calles de OpenFreeMap.
Dibuja publicaciones (forma de color con el ícono de su categoría; la elegida más grande), el punto elegido (pin),
la persona (punto azul con ícono de persona)
y zonas en metros reales (ej. la ubicación aproximada). Tocar un marcador de publicación avisa su id;
tocar el mapa avisa el punto. Al cambiar [center] o [zoom] la cámara se mueve allá.
Si [interactive] es falso, se desactivan los gestos y los toques (útil para mapas de solo vista previa).
**/
@Composable
fun AppMap(
    // Punto en el que se centra la cámara.
    center: GeoPoint,
    // Modificador para tamaño y posición del mapa dentro de la pantalla.
    modifier: Modifier = Modifier,
    // Nivel de acercamiento de la cámara (valores mayores = más cerca).
    zoom: Double = 14.0,
    // Marcadores a dibujar (publicaciones o pin elegido).
    markers: List<MapMarker> = emptyList(),
    // Zonas circulares a dibujar (por ejemplo, la ubicación aproximada de una publicación).
    areas: List<MapArea> = emptyList(),
    // Ubicación actual de la persona; si es nula no se dibuja su punto azul.
    userLocation: GeoPoint? = null,
    // Acción al tocar un punto del mapa (sin marcador); recibe la coordenada tocada.
    onMapClick: ((GeoPoint) -> Unit)? = null,
    // Acción al tocar un marcador de publicación; recibe el id del marcador.
    onMarkerClick: ((String) -> Unit)? = null,
    // Indica si el mapa responde a gestos y toques.
    interactive: Boolean = true,
) {
    // Contexto de Android necesario para crear el MapView nativo.
    val context = LocalContext.current
    // Densidad de pantalla para convertir dp a píxeles al calcular el área de toque.
    val density = LocalDensity.current.density
    // Ciclo de vida de la pantalla actual, al que se engancha el MapView.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    // Guarda siempre la última versión del callback de toque en el mapa, ya que el listener nativo se registra una sola vez.
    val currentOnMapClick by rememberUpdatedState(onMapClick)
    // Igual que el anterior, pero para el callback de toque sobre un marcador.
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    // Imagen de cada marcador (forma, color e ícono) y la de la persona.
    val images = rememberMarkerImages(markers, includeUser = userLocation != null)

    // El MapView y lo último que se pidió dibujar (el estilo llega por internet, así que se aplica al cargar).
    val holder = remember { MapHolder() }
    // Crea el MapView una sola vez y lo conserva entre recomposiciones.
    val mapView = remember {
        // Inicializa la librería MapLibre (obligatorio antes de crear cualquier MapView).
        MapLibre.getInstance(context)
        // Crea la vista nativa del mapa y la configura.
        MapView(context).apply {
            // Notifica la creación de la vista (MapView requiere recibir los eventos de ciclo de vida manualmente).
            onCreate(null)
            // Pide el objeto MapLibreMap de forma asíncrona; el bloque se ejecuta cuando el mapa está listo.
            getMapAsync { map ->
                // Guarda la referencia del mapa para usarla en las actualizaciones posteriores.
                holder.map = map
                // Posiciona la cámara inicial en el centro y zoom recibidos, sin animación.
                map.cameraPosition = CameraPosition.Builder()
                    // Coordenada a la que apunta la cámara.
                    .target(LatLng(center.latitude, center.longitude))
                    // Nivel de acercamiento inicial.
                    .zoom(zoom)
                    .build()
                // Recuerda qué centro y zoom ya se aplicaron, para no volver a mover la cámara sin necesidad.
                holder.cameraKey = center to zoom
                // Descarga y aplica el estilo de OpenFreeMap; el bloque se ejecuta cuando el estilo terminó de cargar.
                map.setStyle(Style.Builder().fromUri(STYLE_URL)) { style ->
                    // Agrega la fuente GeoJSON vacía de las zonas (se llena luego en render()).
                    style.addSource(GeoJsonSource(AREAS_SOURCE))
                    // Agrega la fuente GeoJSON vacía de los marcadores.
                    style.addSource(GeoJsonSource(MARKERS_SOURCE))
                    // Agrega la capa que rellena los polígonos de las zonas.
                    style.addLayer(
                        FillLayer(AREAS_LAYER, AREAS_SOURCE).withProperties(
                            // El color de relleno se lee de la propiedad "color" de cada Feature.
                            PropertyFactory.fillColor(Expression.toColor(Expression.get("color"))),
                            // Relleno semitransparente para que se sigan viendo las calles debajo.
                            PropertyFactory.fillOpacity(0.22f),
                            // El borde usa el mismo color de la zona.
                            PropertyFactory.fillOutlineColor(Expression.toColor(Expression.get("color"))),
                        )
                    )
                    // Cada marcador es una imagen; la elegida y la persona van encima de las demás.
                    style.addLayer(
                        SymbolLayer(MARKERS_LAYER, MARKERS_SOURCE).withProperties(
                            // La imagen a dibujar se toma de la propiedad "image" (clave registrada en el estilo).
                            PropertyFactory.iconImage(Expression.get("image")),
                            // Permite que los íconos se superpongan en vez de ocultarse entre sí.
                            PropertyFactory.iconAllowOverlap(true),
                            // Hace que los íconos no oculten otros elementos del mapa (como etiquetas).
                            PropertyFactory.iconIgnorePlacement(true),
                            // Ordena el dibujo según la propiedad "order": los de mayor valor quedan encima.
                            PropertyFactory.symbolSortKey(Expression.toNumber(Expression.get("order"))),
                        )
                    )
                    // Guarda el estilo cargado para que render() pueda actualizar las fuentes.
                    holder.style = style
                    // Dibuja de inmediato lo que se hubiera pedido mientras el estilo cargaba.
                    holder.render()
                }
                // Primero un marcador de publicación cerca del toque; si no hay, el punto del mapa.
                map.addOnMapClickListener { latLng ->
                    // Si el mapa no es interactivo, ignora el toque y deja que el evento siga su curso.
                    if (!holder.interactive) return@addOnMapClickListener false
                    // Convierte la coordenada tocada a píxeles de pantalla.
                    val screen = map.projection.toScreenLocation(latLng)
                    // Radio de tolerancia del toque (24 dp convertidos a píxeles) para que sea fácil acertar al marcador.
                    val r = 24 * density
                    // Busca los marcadores dibujados dentro del cuadro alrededor del toque y toma el primero que sea publicación.
                    val hit = map.queryRenderedFeatures(RectF(screen.x - r, screen.y - r, screen.x + r, screen.y + r), MARKERS_LAYER)
                        .firstOrNull { it.getStringProperty("kind") == MarkerKind.POST.name }
                    // Toma el callback vigente de toque sobre marcador.
                    val markerClick = currentOnMarkerClick
                    // Decide a quién avisar según lo que se tocó.
                    when {
                        // Si se tocó un marcador de publicación y hay quien lo escuche, avisa su id.
                        hit != null && markerClick != null -> markerClick(hit.getStringProperty("id"))
                        // En otro caso, avisa la coordenada tocada en el mapa.
                        else -> currentOnMapClick?.invoke(GeoPoint(latLng.latitude, latLng.longitude))
                    }
                    // Indica que el toque fue consumido.
                    true
                }
            }
        }
    }

    // El MapView necesita los eventos de ciclo de vida de la pantalla.
    DisposableEffect(lifecycle, mapView) {
        // Observador que reenvía cada evento del ciclo de vida al método equivalente del MapView.
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                // La pantalla se hace visible: el mapa empieza a funcionar.
                Lifecycle.Event.ON_START -> mapView.onStart()
                // La pantalla recibe el foco: el mapa reanuda el renderizado.
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                // La pantalla pierde el foco: el mapa pausa el renderizado.
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                // La pantalla deja de verse: el mapa detiene su trabajo.
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                // Los demás eventos no requieren acción.
                else -> Unit
            }
        }
        // Registra el observador en el ciclo de vida.
        lifecycle.addObserver(observer)
        // Al salir el mapa de la composición, se limpia todo.
        onDispose {
            // Deja de escuchar el ciclo de vida.
            lifecycle.removeObserver(observer)
            // Detiene el mapa.
            mapView.onStop()
            // Libera los recursos nativos del mapa (memoria, hilos de renderizado).
            mapView.onDestroy()
        }
    }

    // Incrusta el MapView nativo dentro de la jerarquía de Compose.
    AndroidView(
        // Devuelve la vista ya creada y recordada.
        factory = { mapView },
        // Aplica el modificador recibido (tamaño, posición, etc.).
        modifier = modifier,
        // Se ejecuta en cada recomposición para pasar los datos nuevos al mapa.
        update = {
            // Actualiza si el mapa debe responder a toques.
            holder.interactive = interactive
            // Guarda los marcadores actuales.
            holder.markers = markers
            // Guarda las zonas actuales.
            holder.areas = areas
            // Guarda la ubicación actual de la persona.
            holder.userLocation = userLocation
            // Guarda las imágenes de los marcadores.
            holder.images = images
            // Solo si el mapa ya está listo, se ajustan gestos y cámara.
            holder.map?.let { map ->
                // Activa o desactiva todos los gestos (arrastrar, zoom, rotar) según la interactividad.
                map.uiSettings.setAllGesturesEnabled(interactive)
                // La cámara solo se mueve si cambió el centro o el zoom pedido (no en cada redibujo).
                if (holder.cameraKey != center to zoom) {
                    // Registra el nuevo centro y zoom como aplicados.
                    holder.cameraKey = center to zoom
                    // Mueve la cámara con animación hacia el nuevo centro y zoom.
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(center.latitude, center.longitude), zoom))
                }
            }
            // Redibuja zonas y marcadores (si el estilo aún no carga, no hace nada).
            holder.render()
        },
    )
}

/**
Estado del mapa nativo entre recomposiciones: el mapa, su estilo y lo que hay que dibujar.
Compose y MapLibre trabajan a ritmos distintos (el mapa y el estilo llegan de forma asíncrona), así que esta clase
guarda la última información recibida y la vuelca en las fuentes GeoJSON cada vez que se llama a [render].
**/
private class MapHolder {
    // Referencia al mapa de MapLibre; es nula hasta que getMapAsync termina.
    var map: MapLibreMap? = null
    // Estilo cargado; es nulo mientras se descarga de internet.
    var style: Style? = null
    // Último centro y zoom aplicados a la cámara, para detectar cambios reales.
    var cameraKey: Pair<GeoPoint, Double>? = null
    // Indica si el mapa acepta toques.
    var interactive = true
    // Marcadores pendientes o actuales a dibujar.
    var markers: List<MapMarker> = emptyList()
    // Zonas pendientes o actuales a dibujar.
    var areas: List<MapArea> = emptyList()
    // Ubicación actual de la persona, si se conoce.
    var userLocation: GeoPoint? = null
    // Bitmaps de los marcadores, indexados por su clave de imagen.
    var images: Map<String, android.graphics.Bitmap> = emptyMap()

    /**
    Vuelca los datos guardados en el estilo del mapa: convierte las zonas en polígonos y los marcadores en puntos
    GeoJSON con sus propiedades (id, tipo, imagen y orden), y registra las imágenes que falten.
    **/
    fun render() {
        // Si el estilo todavía no está cargado no hay dónde dibujar; se sale y se reintentará al cargar.
        val style = style ?: return
        // Reemplaza el contenido de la fuente de zonas con un polígono por cada zona.
        style.getSourceAs<GeoJsonSource>(AREAS_SOURCE)?.setGeoJson(
            FeatureCollection.fromFeatures(areas.map { area ->
                // Aproxima el círculo de la zona como un polígono de coordenadas reales.
                Feature.fromGeometry(Polygon.fromLngLats(listOf(circle(area.center, area.radiusMeters)))).apply {
                    // Guarda el color en hexadecimal para que la FillLayer lo lea.
                    addStringProperty("color", area.color.hex())
                }
            })
        )
        // Las imágenes nuevas se agregan al estilo (las que ya están se reutilizan).
        images.forEach { (key, bitmap) -> if (style.getImage(key) == null) style.addImage(key, bitmap) }
        // Convierte cada marcador en un punto GeoJSON con sus propiedades.
        val features = markers.map { marker ->
            // GeoJSON usa el orden longitud, latitud.
            Feature.fromGeometry(Point.fromLngLat(marker.point.longitude, marker.point.latitude)).apply {
                // Id del marcador, que se devuelve al tocarlo.
                addStringProperty("id", marker.id)
                // Tipo del marcador, usado para saber si es una publicación tocable.
                addStringProperty("kind", marker.kind.name)
                // Clave de la imagen que la SymbolLayer debe pintar.
                addStringProperty("image", marker.imageKey())
                // Orden de dibujo: el marcador elegido queda por encima de todos.
                addNumberProperty("order", if (marker.selected) 2 else 0)
            }
            // Se suma, si existe, el punto de la persona.
        } + listOfNotNull(userLocation?.let { user ->
            // Punto en la ubicación de la persona.
            Feature.fromGeometry(Point.fromLngLat(user.longitude, user.latitude)).apply {
                // Id fijo para el punto de la persona.
                addStringProperty("id", "user")
                // Tipo USER: no se trata como publicación al tocarlo.
                addStringProperty("kind", MarkerKind.USER.name)
                // Usa la imagen del punto azul con ícono de persona.
                addStringProperty("image", USER_IMAGE_KEY)
                // Queda encima de las publicaciones normales pero debajo de la elegida.
                addNumberProperty("order", 1)
            }
        })
        // Reemplaza el contenido de la fuente de marcadores con todos los puntos.
        style.getSourceAs<GeoJsonSource>(MARKERS_SOURCE)?.setGeoJson(FeatureCollection.fromFeatures(features))
    }
}

// Convierte un Color de Compose a texto hexadecimal "#RRGGBB" (sin canal alfa), formato que entienden las expresiones de MapLibre.
private fun Color.hex(): String = String.format("#%06X", toArgb() and 0xFFFFFF)

/**
Círculo en metros reales como polígono de 48 lados.
Calcula cada vértice desplazándose desde el centro: un grado de latitud mide unos 111.320 m, y un grado de longitud
mide eso multiplicado por el coseno de la latitud (los meridianos se juntan hacia los polos).
**/
private fun circle(center: GeoPoint, radiusMeters: Double): List<Point> {
    // Latitud del centro en radianes, necesaria para corregir la longitud.
    val latRad = Math.toRadians(center.latitude)
    // Genera 49 puntos (0..48) para que el último cierre el polígono sobre el primero.
    return (0..48).map { i ->
        // Ángulo del vértice actual alrededor del círculo.
        val angle = 2 * Math.PI * i / 48
        // Desplazamiento en grados de latitud (eje norte-sur).
        val dLat = radiusMeters * cos(angle) / 111_320.0
        // Desplazamiento en grados de longitud (eje este-oeste), corregido por la latitud.
        val dLng = radiusMeters * sin(angle) / (111_320.0 * cos(latRad))
        // Crea el vértice en orden longitud, latitud.
        Point.fromLngLat(center.longitude + dLng, center.latitude + dLat)
    }
}
