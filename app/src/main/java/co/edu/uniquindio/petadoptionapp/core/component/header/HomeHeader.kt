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
 Se usa en HomeUserScreen y ModeratorHomeScreen; toma tipografía y colores del
 tema para verse igual en modo claro y oscuro.
**/
@Composable
fun HomeHeader(
    // Nombre del usuario que se inserta en el saludo.
    userName: String,
    // Ciudad del usuario; si está vacía no se muestra la fila de ubicación.
    city: String,
    // Cantidad de notificaciones sin leer que se muestra en la insignia.
    notifications: Int,
    // Acción al tocar la campana (normalmente abrir la pantalla de notificaciones).
    onNotificationsClick: () -> Unit,
    // Modificador externo para ajustar la cabecera.
    modifier: Modifier = Modifier,
    // Margen lateral, configurable para alinearse con el contenido de cada pantalla.
    horizontalPadding: Dp = 16.dp,
) {
    // Fila que reparte el saludo a la izquierda y la campana a la derecha.
    Row(
        modifier = modifier
            // Ocupa todo el ancho disponible.
            .fillMaxWidth()
            // Aplica el margen lateral recibido y un margen vertical fijo.
            .padding(horizontal = horizontalPadding, vertical = 20.dp),
        // Empuja los dos bloques a los extremos.
        horizontalArrangement = Arrangement.SpaceBetween,
        // Centra ambos bloques verticalmente.
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bloque izquierdo: saludo y ubicación apilados.
        Column {
            // Saludo personalizado con el nombre del usuario.
            Text(
                text = stringResource(R.string.home_welcome, userName),
                style = MaterialTheme.typography.headlineSmall,
                color = AppTheme.colors.textPrimary
            )
            // Solo muestra la ubicación si el usuario tiene ciudad registrada.
            if (city.isNotBlank()) {
                // Fila con el nombre de la ciudad y el pin de ubicación.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Nombre de la ciudad.
                    Text(
                        text = city,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.textPrimary
                    )
                    // Pin de ubicación decorativo; se pinta con el color de error para que resalte en rojo.
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
                // Tamaño táctil del botón.
                .size(44.dp)
                // Recorta en círculo para que el efecto de pulsación respete la forma.
                .clip(CircleShape)
                // Ejecuta la acción de notificaciones al tocar.
                .clickable(onClick = onNotificationsClick)
                // Fondo circular igual al fondo de la pantalla.
                .background(AppTheme.colors.background, CircleShape)
                // Contorno circular con el color del texto principal.
                .border(1.dp, AppTheme.colors.textPrimary, CircleShape),
            // Centra la campana dentro del botón.
            contentAlignment = Alignment.Center
        ) {
            // Contenedor de Material que superpone una insignia sobre el icono.
            BadgedBox(
                // Pequeño ajuste horizontal para centrar ópticamente la campana con la insignia.
                modifier = Modifier.offset(x = (-1).dp),
                badge = {
                    // Sin notificaciones no se dibuja la insignia (evita mostrar un "0").
                    if (notifications > 0) {
                        // Insignia con colores invertidos respecto al botón para destacar el número.
                        Badge(
                            containerColor = AppTheme.colors.textPrimary,
                            contentColor = AppTheme.colors.background
                        ) { Text(text = "$notifications") }
                    }
                }
            ) {
                // Icono de campana con descripción accesible para TalkBack.
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = stringResource(R.string.home_notifications_description),
                    tint = AppTheme.colors.textPrimary
                )
            }
        }
    }
}

// Vista previa de la cabecera con ciudad y tres notificaciones.
@Preview(showBackground = true)
@Composable
private fun HomeHeaderPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Cabecera de ejemplo de un usuario con ubicación.
        HomeHeader(userName = "ana_77", city = "Armenia, Colombia", notifications = 3, onNotificationsClick = {})
    }
}
