package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme

val AppCardShape = RoundedCornerShape(24.dp)

/**
 Tarjeta con fondo, borde y esquinas redondeadas del tema. Base de las demás tarjetas.
 Con [borderColor] en `null` se dibuja sin borde.
 Con [onClick] la tarjeta completa es tocable; [onClickLabel] es lo que TalkBack anuncia como acción.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    containerColor: Color = AppTheme.colors.surface,
    borderColor: Color? = AppTheme.colors.border,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            // El clip va antes del clic para que el efecto de toque respete las esquinas.
            .clip(AppCardShape)
            .background(containerColor, AppCardShape)
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, AppCardShape) else Modifier)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(contentPadding),
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}
