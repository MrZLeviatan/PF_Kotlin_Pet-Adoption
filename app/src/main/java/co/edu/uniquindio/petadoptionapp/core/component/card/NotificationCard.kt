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
Tarjeta de una notificación: icono, título, tiempo transcurrido y mensaje.
Con [isNew] muestra la etiqueta "Nuevo". No conoce el dominio: recibe todo ya resuelto.
El icono es decorativo: el título ya describe la notificación.
 */
@Composable
fun NotificationCard(
    icon: ImageVector,
    title: String,
    timeText: String,
    message: String,
    isNew: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.surface)
            .border(1.dp, AppTheme.colors.border, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTheme.colors.textPrimary,
                modifier = Modifier.size(24.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = AppTheme.colors.textPrimary,
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.textPrimary,
                )
            }

            if (isNew) {
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

        Spacer(Modifier.height(12.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.colors.textPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationCardPreview() {
    PetAdoptionAppTheme {
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