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

/*
 Imágenes de los marcadores del mapa, dibujadas con Compose a partir de los íconos de Material
 (así no hacen falta imágenes aparte): forma de color con borde blanco y sombra, y el ícono en blanco encima.
 La persona es un punto azul con el ícono de persona y un halo, para que se distinga de las publicaciones.
 */

/** Nombre único de la imagen de un marcador (el mapa guarda una por combinación de forma, color e ícono). */
internal fun MapMarker.imageKey(): String =
    "marker-${kind}-${shape}-${color.value}-${icon?.name}-${if (selected) "sel" else "n"}"

internal const val USER_IMAGE_KEY = "marker-user"

private val UserBlue = Color(0xFF2F80ED)
private val DarkIcon = Color(0xFF3E2716)

// Sobre colores claros (amarillo) el ícono va oscuro, como en el diseño; sobre los demás, blanco.
private fun iconTintFor(background: Color): Color = if (background.luminance() > 0.5f) DarkIcon else Color.White

/** Bitmaps de todos los marcadores visibles (y de la persona), por su clave. */
@Composable
internal fun rememberMarkerImages(markers: List<MapMarker>, includeUser: Boolean): Map<String, Bitmap> {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    // Un painter por ícono distinto (se crean en la composición; luego se dibujan en bitmaps).
    val icons = (markers.mapNotNull { it.icon } + if (includeUser) listOf(Icons.Filled.Person) else emptyList()).distinctBy { it.name }
    val painters = icons.associate { it.name to rememberVectorPainter(it) }
    val keys = markers.map { it.imageKey() }.distinct() + if (includeUser) listOf(USER_IMAGE_KEY) else emptyList()

    return remember(keys, density) {
        val result = mutableMapOf<String, Bitmap>()
        markers.distinctBy { it.imageKey() }.forEach { marker ->
            val base = when {
                marker.kind == MarkerKind.PIN -> 34.dp
                marker.selected -> 52.dp
                else -> 40.dp
            }
            val sizePx = with(density) { (base + 8.dp).toPx() }
            val bitmap = ImageBitmap(sizePx.toInt(), sizePx.toInt())
            CanvasDrawScope().draw(density, layoutDirection, Canvas(bitmap), Size(sizePx, sizePx)) {
                val inset = 4.dp.toPx()
                val body = Size(size.width - inset * 2, size.height - inset * 2)
                val topLeft = Offset(inset, inset)
                val stroke = 3.dp.toPx()
                // Sombra suave debajo, con la misma forma del marcador.
                val shadow = Color.Black.copy(alpha = 0.18f)
                val shadowOffset = Offset(0f, 2.dp.toPx())
                when (marker.shape) {
                    MarkerShape.CIRCLE -> drawCircle(shadow, radius = body.width / 2 + 1.dp.toPx(), center = center + shadowOffset)
                    MarkerShape.ROUNDED_SQUARE -> drawRoundRect(shadow, topLeft + shadowOffset, body, CornerRadius(body.width * 0.28f))
                }
                when (marker.shape) {
                    MarkerShape.CIRCLE -> {
                        drawCircle(marker.color, radius = body.width / 2, center = center)
                        drawCircle(Color.White, radius = body.width / 2, center = center, style = Stroke(stroke))
                    }
                    MarkerShape.ROUNDED_SQUARE -> {
                        val corner = CornerRadius(body.width * 0.28f)
                        drawRoundRect(marker.color, topLeft, body, corner)
                        drawRoundRect(Color.White, topLeft, body, corner, style = Stroke(stroke))
                    }
                }
                marker.icon?.let { icon ->
                    val painter = painters.getValue(icon.name)
                    val iconSize = body.width * 0.55f
                    translate((size.width - iconSize) / 2, (size.height - iconSize) / 2) {
                        with(painter) { draw(Size(iconSize, iconSize), colorFilter = ColorFilter.tint(iconTintFor(marker.color))) }
                    }
                } ?: if (marker.kind == MarkerKind.PIN) drawCircle(Color.White, radius = body.width * 0.18f, center = center) else Unit
            }
            result[marker.imageKey()] = bitmap.asAndroidBitmap()
        }
        if (includeUser) {
            val sizePx = with(density) { 56.dp.toPx() }
            val bitmap = ImageBitmap(sizePx.toInt(), sizePx.toInt())
            CanvasDrawScope().draw(density, layoutDirection, Canvas(bitmap), Size(sizePx, sizePx)) {
                // Halo azul, punto azul con borde blanco y la silueta de persona.
                drawCircle(UserBlue.copy(alpha = 0.18f), radius = size.width / 2, center = center)
                val r = 15.dp.toPx()
                drawCircle(UserBlue, radius = r, center = center)
                drawCircle(Color.White, radius = r, center = center, style = Stroke(3.dp.toPx()))
                val painter = painters.getValue(Icons.Filled.Person.name)
                val iconSize = r * 1.2f
                translate((size.width - iconSize) / 2, (size.height - iconSize) / 2) {
                    with(painter) { draw(Size(iconSize, iconSize), colorFilter = ColorFilter.tint(Color.White)) }
                }
            }
            result[USER_IMAGE_KEY] = bitmap.asAndroidBitmap()
        }
        result
    }
}
