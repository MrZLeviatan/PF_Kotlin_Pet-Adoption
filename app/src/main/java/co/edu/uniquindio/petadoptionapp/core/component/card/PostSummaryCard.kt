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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/** Línea de detalle de la tarjeta: icono pequeño y texto ("Publicado hace 3 días"). */
data class PostMetaLine(val icon: ImageVector, val text: String)

/**
Tarjeta resumen de una publicación. No conoce los modelos del dominio: recibe todo ya
resuelto (imagen, textos, color de estado). El [footer] es opcional y cambia según el estado.
Con [onClick] la tarjeta completa es tocable.
 */
@Composable
fun PostSummaryCard(
    image: Painter,
    typeLabel: String,
    statusColor: Color,
    title: String,
    metaLines: List<PostMetaLine>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.surface)
            .border(1.dp, AppTheme.colors.border, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Decorativa: el título ya describe la publicación.
            Image(
                painter = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier
                            .background(AppTheme.colors.surfaceTag, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                    StatusIndicator(color = statusColor)
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                metaLines.forEach { line ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = line.icon,
                            contentDescription = null,
                            tint = AppTheme.colors.textPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = line.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.textPrimary,
                        )
                    }
                }
            }
        }

        if (footer != null) {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = AppTheme.colors.border)
            footer()
        }
    }
}

// Píldora con un punto de color. Decorativa: el estado también lo dice el encabezado de la sección.
@Composable
private fun StatusIndicator(color: Color) {
    Box(
        modifier = Modifier
            .size(width = 24.dp, height = 14.dp)
            .border(1.dp, AppTheme.colors.border, CircleShape)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PostSummaryCardPreview() {
    PetAdoptionAppTheme {
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