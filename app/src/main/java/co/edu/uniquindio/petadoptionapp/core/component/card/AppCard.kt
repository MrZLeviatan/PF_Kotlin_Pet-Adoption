package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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

/**
Tarjeta base del sistema de diseño. Define el fondo, el borde y las esquinas redondeadas (24 dp)
que comparten casi todas las tarjetas de la app (LevelProgressCard, StatCard, SectionCard,
PostSummaryCard, NotificationCard, etc.), de modo que todas se vean coherentes entre pantallas.
Además resuelve en un solo lugar si la tarjeta es tocable, si responde a pulsación larga y qué
anuncia TalkBack, para que las tarjetas específicas solo se ocupen de su contenido.
**/

// Forma pública de las tarjetas (esquinas de 24 dp); se expone para que otros componentes (p. ej. SwipeToDeleteBox) recorten igual.
val AppCardShape = RoundedCornerShape(24.dp)

/**
Tarjeta con fondo, borde y esquinas redondeadas del tema. Base de las demás tarjetas.
Con [borderColor] en `null` se dibuja sin borde.
Con [onClick] la tarjeta completa es tocable; [onClickLabel] es lo que TalkBack anuncia como acción.
Con [onLongClick] además responde a mantener presionado (ej. opciones de un chat).
El contenido se organiza en una columna, por eso [content] recibe un ColumnScope.
**/
@Composable
fun AppCard(
    // Modificador opcional para personalizar la tarjeta desde afuera.
    modifier: Modifier = Modifier,
    // Alineación horizontal de los hijos dentro de la columna (por defecto al inicio).
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    // Color de fondo de la tarjeta (por defecto la superficie del tema).
    containerColor: Color = AppTheme.colors.surface,
    // Color del borde; `null` para dibujar la tarjeta sin borde.
    borderColor: Color? = AppTheme.colors.border,
    // Espaciado interno entre el borde de la tarjeta y su contenido.
    contentPadding: PaddingValues = PaddingValues(20.dp),
    // Acción al tocar la tarjeta; `null` si la tarjeta es solo informativa.
    onClick: (() -> Unit)? = null,
    // Etiqueta de la acción de clic que anuncia TalkBack (p. ej. "abrir detalle").
    onClickLabel: String? = null,
    // Acción al mantener presionada la tarjeta; `null` si no aplica.
    onLongClick: (() -> Unit)? = null,
    // Contenido de la tarjeta, dibujado dentro de una columna.
    content: @Composable ColumnScope.() -> Unit,
) {
    // Columna que actúa como contenedor visual y táctil de la tarjeta.
    Column(
        // Cadena de modificadores: el orden importa (recorte, fondo, borde, clic y relleno).
        modifier = modifier
            // Ocupa todo el ancho disponible.
            .fillMaxWidth()
            // El clip va antes del clic para que el efecto de toque respete las esquinas.
            .clip(AppCardShape)
            // Pinta el fondo con la misma forma redondeada.
            .background(containerColor, AppCardShape)
            // Dibuja el borde de 1 dp solo si se indicó un color; si no, no agrega nada.
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, AppCardShape) else Modifier)
            // Agrega el comportamiento de clic según los callbacks recibidos.
            .then(
                // Si hay pulsación larga se usa combinedClickable, que soporta clic y clic largo.
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        // Etiqueta accesible de la acción principal.
                        onClickLabel = onClickLabel,
                        // Indica a los servicios de accesibilidad que se comporta como un botón.
                        role = Role.Button,
                        // Acción al mantener presionado.
                        onLongClick = onLongClick,
                        // Acción del clic normal; si no hay, se usa una lambda vacía.
                        onClick = onClick ?: {},
                    )
                // Si solo hay clic normal se usa clickable con rol de botón.
                } else if (onClick != null) {
                    Modifier.clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                // Sin callbacks la tarjeta no es interactiva.
                } else {
                    Modifier
                }
            )
            // El relleno va al final para que el área tocable abarque toda la tarjeta.
            .padding(contentPadding),
        // Aplica la alineación horizontal recibida a los hijos.
        horizontalAlignment = horizontalAlignment,
        // Dibuja el contenido proporcionado por quien usa la tarjeta.
        content = content,
    )
}
