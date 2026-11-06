package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTextRow
import co.edu.uniquindio.petadoptionapp.core.component.indicator.StatusIndicator
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Tarjeta resumen de una publicación (adopción, mascota perdida, etc.) usada en listas como
"Mis publicaciones" o el panel de moderación. Muestra una miniatura, el tipo, un punto de estado,
el título y varias líneas de detalle, y admite un pie opcional con acciones según el estado.
Es un componente de presentación puro: no conoce los modelos del dominio.
**/

/** Línea de detalle de la tarjeta: icono pequeño y texto ("Publicado hace 3 días"). */
data class PostMetaLine(val icon: ImageVector, val text: String)

/**
Tarjeta resumen de una publicación. No conoce los modelos del dominio: recibe todo ya
resuelto (imagen, textos, color de estado). El [footer] es opcional y cambia según el estado.
Con [onClick] la tarjeta completa es tocable.
**/
@Composable
fun PostSummaryCard(
    // Imagen de la mascota ya cargada como Painter.
    image: Painter,
    // Etiqueta del tipo de publicación ("Adopción").
    typeLabel: String,
    // Color del punto que indica el estado de la publicación.
    statusColor: Color,
    // Título de la publicación.
    title: String,
    // Líneas de detalle (fecha, interesados, etc.) mostradas bajo el título.
    metaLines: List<PostMetaLine>,
    // Modificador opcional para personalizar la tarjeta desde afuera.
    modifier: Modifier = Modifier,
    // Acción al tocar la tarjeta; `null` si no es tocable.
    onClick: (() -> Unit)? = null,
    // Contenido opcional del pie (botones o avisos según el estado).
    footer: (@Composable () -> Unit)? = null,
) {
    // Forma redondeada de 20 dp reutilizada por el recorte y el borde.
    val shape = RoundedCornerShape(20.dp)

    // Columna que forma la tarjeta: cuerpo arriba y pie opcional abajo.
    Column(
        modifier = modifier
            // Ocupa todo el ancho disponible.
            .fillMaxWidth()
            // Recorta antes del clic para que el efecto de toque respete las esquinas.
            .clip(shape)
            // Fondo de superficie del tema.
            .background(AppTheme.colors.surface)
            // Borde neutro de 1 dp.
            .border(1.dp, AppTheme.colors.border, shape)
            // Solo agrega el clic cuando se recibió una acción.
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            // Relleno interno de la tarjeta.
            .padding(12.dp),
    ) {
        // Cuerpo: miniatura a la izquierda y textos a la derecha.
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Decorativa: el título ya describe la publicación.
            Image(
                painter = image,
                contentDescription = null,
                // Recorta la imagen para llenar el cuadro sin deformarla.
                contentScale = ContentScale.Crop,
                // Miniatura cuadrada de 96 dp con esquinas redondeadas.
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )

            // Columna de textos que ocupa el espacio restante.
            Column(
                modifier = Modifier.weight(1f),
                // Separación vertical uniforme entre los textos.
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Fila superior: etiqueta de tipo a la izquierda y punto de estado a la derecha.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Etiqueta del tipo de publicación con fondo de píldora.
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier
                            .background(AppTheme.colors.surfaceTag, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                    // Punto de color que representa el estado de la publicación.
                    StatusIndicator(color = statusColor)
                }

                // Título de la publicación, limitado a dos líneas.
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 2,
                    // Agrega "..." si el título no cabe en dos líneas.
                    overflow = TextOverflow.Ellipsis,
                )

                // Dibuja cada línea de detalle con su icono.
                metaLines.forEach { line ->
                    // Fila de icono y texto con tamaños compactos.
                    IconTextRow(
                        icon = line.icon,
                        text = line.text,
                        iconSize = 14.dp,
                        spacing = 6.dp,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        // Solo dibuja el pie cuando se proporcionó uno.
        if (footer != null) {
            // Separación entre el cuerpo y el divisor.
            Spacer(Modifier.height(8.dp))
            // Línea divisoria que separa el cuerpo del pie.
            HorizontalDivider(color = AppTheme.colors.border)
            // Contenido del pie proporcionado por la pantalla.
            footer()
        }
    }
}

// Vista previa en Android Studio de una publicación de adopción.
@Preview(showBackground = true)
@Composable
private fun PostSummaryCardPreview() {
    // Envuelve el componente en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Publicación de ejemplo con dos líneas de detalle y sin pie.
        PostSummaryCard(
            image = painterResource(R.drawable.hero_image),
            typeLabel = "Adopción",
            statusColor = AppTheme.colors.stateSuccess,
            title = "Luna busca un hogar lleno de amor",
            metaLines = listOf(
                PostMetaLine(Icons.Outlined.CalendarToday, "Publicado hace 3 días"),
                PostMetaLine(Icons.Outlined.FavoriteBorder, "12 personas interesadas"),
            ),
        )
    }
}