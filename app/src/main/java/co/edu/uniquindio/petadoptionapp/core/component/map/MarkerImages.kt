package co.edu.uniquindio.petadoptionapp.core.component.map

import android.graphics.Bitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

/**
Imágenes de los marcadores del mapa, dibujadas con Compose a partir de los íconos de Material
(así no hacen falta imágenes aparte): forma de color con borde blanco y sombra, y el ícono en blanco encima.
La persona es un punto azul con el ícono de persona y un halo, para que se distinga de las publicaciones.

Cómo funciona: MapLibre solo sabe pintar imágenes (bitmaps) en su SymbolLayer, así que aquí se dibuja cada marcador
sobre un [ImageBitmap] usando un [CanvasDrawScope] de Compose y se convierte a [Bitmap] de Android. [AppMap] registra
esos bitmaps en el estilo del mapa con una clave única ([imageKey]) y cada punto GeoJSON indica qué clave usar.
**/

/**
Nombre único de la imagen de un marcador (el mapa guarda una por combinación de forma, color e ícono).
**/
internal fun MapMarker.imageKey(): String =
    // Combina tipo, forma, color, ícono y estado de selección para que marcadores iguales compartan imagen.
    "marker-${kind}-${shape}-${color.value}-${icon?.name}-${if (selected) "sel" else "n"}"

// Clave fija de la imagen del punto de la persona.
internal const val USER_IMAGE_KEY = "marker-user"

// Azul del punto de la persona.
private val UserBlue = Color(0xFF2F80ED)
// Color oscuro (café) para íconos sobre fondos claros.
private val DarkIcon = Color(0xFF3E2716)

// Sobre colores claros (amarillo) el ícono va oscuro, como en el diseño; sobre los demás, blanco.
private fun iconTintFor(background: Color): Color = if (background.luminance() > 0.5f) DarkIcon else Color.White

/**
Bitmaps de todos los marcadores visibles (y de la persona), por su clave.
Los bitmaps se recalculan solo cuando cambia el conjunto de claves o la densidad de pantalla.
**/
@Composable
internal fun rememberMarkerImages(markers: List<MapMarker>, includeUser: Boolean): Map<String, Bitmap> {
    // Densidad de pantalla para convertir dp a píxeles al dibujar.
    val density = LocalDensity.current
    // Dirección de diseño (izquierda a derecha o al revés), requerida por el lienzo de Compose.
    val layoutDirection = LocalLayoutDirection.current
    // Un painter por ícono distinto (se crean en la composición; luego se dibujan en bitmaps).
    val icons = (markers.mapNotNull { it.icon } + if (includeUser) listOf(Icons.Filled.Person) else emptyList()).distinctBy { it.name }
    // Crea y recuerda un painter vectorial por cada ícono, indexado por su nombre.
    val painters = icons.associate { it.name to rememberVectorPainter(it) }
    // Claves de todas las imágenes necesarias; sirven como llave del remember para regenerar solo si cambian.
    val keys = markers.map { it.imageKey() }.distinct() + if (includeUser) listOf(USER_IMAGE_KEY) else emptyList()

    // Genera los bitmaps una vez por combinación de claves y densidad.
    return remember(keys, density) {
        // Mapa resultado: clave de imagen -> bitmap.
        val result = mutableMapOf<String, Bitmap>()
        // Dibuja una imagen por cada combinación distinta de marcador.
        markers.distinctBy { it.imageKey() }.forEach { marker ->
            // Tamaño base según el tipo: pin pequeño, seleccionado grande y normal intermedio.
            val base = when {
                marker.kind == MarkerKind.PIN -> 34.dp
                marker.selected -> 52.dp
                else -> 40.dp
            }
            // Tamaño total en píxeles, con 8 dp extra de margen para la sombra y el borde.
            val sizePx = with(density) { (base + 8.dp).toPx() }
            // Crea el bitmap cuadrado vacío donde se dibujará.
            val bitmap = ImageBitmap(sizePx.toInt(), sizePx.toInt())
            // Dibuja sobre el bitmap usando las primitivas de dibujo de Compose.
            CanvasDrawScope().draw(density, layoutDirection, Canvas(bitmap), Size(sizePx, sizePx)) {
                // Margen interno que deja espacio a la sombra.
                val inset = 4.dp.toPx()
                // Tamaño del cuerpo del marcador sin el margen.
                val body = Size(size.width - inset * 2, size.height - inset * 2)
                // Esquina superior izquierda del cuerpo.
                val topLeft = Offset(inset, inset)
                // Grosor del borde blanco.
                val stroke = 3.dp.toPx()
                // Sombra suave debajo, con la misma forma del marcador.
                val shadow = Color.Black.copy(alpha = 0.18f)
                // La sombra se desplaza un poco hacia abajo.
                val shadowOffset = Offset(0f, 2.dp.toPx())
                // Dibuja la sombra según la forma.
                when (marker.shape) {
                    // Sombra circular, un poco más grande que el cuerpo.
                    MarkerShape.CIRCLE -> drawCircle(shadow, radius = body.width / 2 + 1.dp.toPx(), center = center + shadowOffset)
                    // Sombra de cuadrado redondeado.
                    MarkerShape.ROUNDED_SQUARE -> drawRoundRect(shadow, topLeft + shadowOffset, body, CornerRadius(body.width * 0.28f))
                }
                // Dibuja el cuerpo de color y su borde blanco según la forma.
                when (marker.shape) {
                    MarkerShape.CIRCLE -> {
                        // Círculo relleno con el color del marcador.
                        drawCircle(marker.color, radius = body.width / 2, center = center)
                        // Borde blanco alrededor del círculo.
                        drawCircle(Color.White, radius = body.width / 2, center = center, style = Stroke(stroke))
                    }
                    MarkerShape.ROUNDED_SQUARE -> {
                        // Radio de las esquinas proporcional al tamaño.
                        val corner = CornerRadius(body.width * 0.28f)
                        // Cuadrado redondeado relleno con el color del marcador.
                        drawRoundRect(marker.color, topLeft, body, corner)
                        // Borde blanco alrededor del cuadrado.
                        drawRoundRect(Color.White, topLeft, body, corner, style = Stroke(stroke))
                    }
                }
                // Si hay ícono, se dibuja centrado; si no hay y es un pin, se dibuja un punto blanco en el centro.
                marker.icon?.let { icon ->
                    // Painter del ícono creado previamente en la composición.
                    val painter = painters.getValue(icon.name)
                    // El ícono ocupa el 55 % del cuerpo.
                    val iconSize = body.width * 0.55f
                    // Desplaza el lienzo para que el ícono quede centrado.
                    translate((size.width - iconSize) / 2, (size.height - iconSize) / 2) {
                        // Dibuja el ícono teñido de blanco u oscuro según la claridad del fondo.
                        with(painter) { draw(Size(iconSize, iconSize), colorFilter = ColorFilter.tint(iconTintFor(marker.color))) }
                    }
                } ?: if (marker.kind == MarkerKind.PIN) drawCircle(Color.White, radius = body.width * 0.18f, center = center) else Unit
            }
            // Guarda el bitmap convertido a formato de Android con su clave.
            result[marker.imageKey()] = bitmap.asAndroidBitmap()
        }
        // Si se pidió, dibuja también la imagen de la persona.
        if (includeUser) {
            // Tamaño total de la imagen de la persona, incluyendo el halo.
            val sizePx = with(density) { 56.dp.toPx() }
            // Crea el bitmap vacío.
            val bitmap = ImageBitmap(sizePx.toInt(), sizePx.toInt())
            // Dibuja sobre el bitmap.
            CanvasDrawScope().draw(density, layoutDirection, Canvas(bitmap), Size(sizePx, sizePx)) {
                // Halo azul, punto azul con borde blanco y la silueta de persona.
                drawCircle(UserBlue.copy(alpha = 0.18f), radius = size.width / 2, center = center)
                // Radio del punto azul central.
                val r = 15.dp.toPx()
                // Punto azul relleno.
                drawCircle(UserBlue, radius = r, center = center)
                // Borde blanco del punto.
                drawCircle(Color.White, radius = r, center = center, style = Stroke(3.dp.toPx()))
                // Painter del ícono de persona.
                val painter = painters.getValue(Icons.Filled.Person.name)
                // Tamaño del ícono proporcional al radio.
                val iconSize = r * 1.2f
                // Centra el ícono en el lienzo.
                translate((size.width - iconSize) / 2, (size.height - iconSize) / 2) {
                    // Dibuja la silueta de persona en blanco.
                    with(painter) { draw(Size(iconSize, iconSize), colorFilter = ColorFilter.tint(Color.White)) }
                }
            }
            // Guarda la imagen de la persona con su clave fija.
            result[USER_IMAGE_KEY] = bitmap.asAndroidBitmap()
        }
        // Devuelve todas las imágenes generadas.
        result
    }
}
