package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Tarjeta de una notificación para la bandeja de notificaciones: icono, título, tiempo transcurrido y mensaje.
Con [isNew] muestra la etiqueta "Nuevo". No conoce el dominio: recibe todo ya resuelto,
lo que permite reutilizarla con cualquier tipo de notificación.
El icono es decorativo: el título ya describe la notificación.
**/
@Composable
fun NotificationCard(
    // Icono que representa el tipo de notificación.
    icon: ImageVector,
    // Título de la notificación.
    title: String,
    // Tiempo transcurrido ya formateado ("Hace 2 min").
    timeText: String,
    // Cuerpo o mensaje de la notificación.
    message: String,
    // Indica si la notificación aún no ha sido vista, para mostrar la etiqueta "Nuevo".
    isNew: Boolean,
    // Acción al tocar la notificación (normalmente abrir su detalle).
    onClick: () -> Unit,
    // Modificador opcional para personalizar la tarjeta desde afuera.
    modifier: Modifier = Modifier,
) {
    // Forma redondeada de 20 dp reutilizada por el recorte y el borde.
    val shape = RoundedCornerShape(20.dp)

    // Columna que forma la tarjeta: cabecera arriba y mensaje abajo.
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
            // Hace tocable toda la tarjeta.
            .clickable(onClick = onClick)
            // Relleno interno, aplicado después del clic para que el área tocable cubra toda la tarjeta.
            .padding(16.dp),
    ) {
        // Cabecera: icono, título con tiempo y etiqueta opcional "Nuevo".
        Row(
            // Centra verticalmente los elementos de la cabecera.
            verticalAlignment = Alignment.CenterVertically,
            // Separación de 12 dp entre elementos.
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Icono decorativo de la notificación.
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTheme.colors.textPrimary,
                modifier = Modifier.size(24.dp),
            )

            // Columna con título y tiempo que ocupa el espacio restante.
            Column(modifier = Modifier.weight(1f)) {
                // Título de la notificación.
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = AppTheme.colors.textPrimary,
                )
                // Tiempo transcurrido bajo el título.
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.textPrimary,
                )
            }

            // Solo las notificaciones no vistas muestran la etiqueta "Nuevo".
            if (isNew) {
                // Etiqueta "Nuevo" en forma de píldora con el color de acción principal.
                Text(
                    text = stringResource(R.string.notifications_new_tag),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.onActionPrimary,
                    modifier = Modifier
                        .background(AppTheme.colors.actionPrimary, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        // Separación vertical entre la cabecera y el mensaje.
        Spacer(Modifier.height(12.dp))

        // Mensaje completo de la notificación.
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Vista previa en Android Studio de una notificación nueva.
@Preview(showBackground = true)
@Composable
private fun NotificationCardPreview() {
    // Envuelve el componente en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Notificación de ejemplo marcada como nueva.
        NotificationCard(
            icon = Icons.Outlined.FavoriteBorder,
            title = "Nueva adopción disponible",
            timeText = "Hace 2 min",
            message = "Una nueva mascota fue publicada en tu zona. Revisa la ficha para ver fotos, edad y características.",
            isNew = true,
            onClick = {},
        )
    }
}