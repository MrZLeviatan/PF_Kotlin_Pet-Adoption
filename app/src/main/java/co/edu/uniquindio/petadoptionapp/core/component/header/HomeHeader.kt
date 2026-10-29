package co.edu.uniquindio.petadoptionapp.core.component.header

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Cabecera del inicio (usuario y moderador): saludo y ciudad a la izquierda, campana con contador a la derecha.
 Sin [city] (los moderadores no tienen ubicación) solo se ve el saludo.
 */
@Composable
fun HomeHeader(
    userName: String,
    city: String,
    notifications: Int,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.home_welcome, userName),
                style = MaterialTheme.typography.headlineSmall,
                color = AppTheme.colors.textPrimary
            )
            if (city.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = city,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.textPrimary
                    )
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = AppTheme.colors.stateError,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Botón circular con la campana; el clip va antes del clickable para que el efecto sea circular.
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onNotificationsClick)
                .background(AppTheme.colors.background, CircleShape)
                .border(1.dp, AppTheme.colors.textPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                modifier = Modifier.offset(x = (-1).dp),
                badge = {
                    // Sin notificaciones no se dibuja la insignia (evita mostrar un "0").
                    if (notifications > 0) {
                        Badge(
                            containerColor = AppTheme.colors.textPrimary,
                            contentColor = AppTheme.colors.background
                        ) { Text(text = "$notifications") }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = stringResource(R.string.home_notifications_description),
                    tint = AppTheme.colors.textPrimary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeHeaderPreview() {
    PetAdoptionAppTheme {
        HomeHeader(userName = "ana_77", city = "Armenia, Colombia", notifications = 3, onNotificationsClick = {})
    }
}
