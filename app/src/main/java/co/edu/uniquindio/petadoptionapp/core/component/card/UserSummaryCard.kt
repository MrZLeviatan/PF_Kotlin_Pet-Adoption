package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.component.icon.InitialAvatar
import co.edu.uniquindio.petadoptionapp.core.component.icon.UserAvatar
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTextRow
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Tarjeta centrada con el avatar, el nombre y la ubicación del usuario. Se usa en la cabecera del
Perfil (propio o de otra persona). Está construida sobre [AppCard] para heredar su forma y borde.
El avatar sale de [name]; [displayName] permite mostrar otro texto ("Username: ana_77").
Con [location] en `null` no se muestra la línea de ubicación (ej. perfil de otra persona).
**/
@Composable
fun UserSummaryCard(
    // Nombre del usuario, usado para generar el avatar (iniciales) si no hay foto.
    name: String,
    // Ubicación del usuario; `null` para ocultar la línea.
    location: String?,
    // Modificador opcional para personalizar la tarjeta desde afuera.
    modifier: Modifier = Modifier,
    // Texto que se muestra como nombre; por defecto el mismo [name].
    displayName: String = name,
    // URL de la foto de perfil; `null` para mostrar las iniciales.
    photoUrl: String? = null,
    // Espaciado interno de la tarjeta, configurable según la pantalla.
    contentPadding: PaddingValues = PaddingValues(20.dp),
) {
    // Tarjeta base con el contenido centrado horizontalmente.
    AppCard(
        modifier = modifier,
        // Centra avatar, nombre y ubicación.
        horizontalAlignment = Alignment.CenterHorizontally,
        // Aplica el relleno interno recibido.
        contentPadding = contentPadding,
    ) {

        // Avatar del usuario de 88 dp (foto o iniciales).
        UserAvatar(name = name, photoUrl = photoUrl, size = 88.dp)

        // Separación entre el avatar y el nombre.
        Spacer(Modifier.height(12.dp))

        // Nombre visible del usuario, centrado.
        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineSmall,
            color = AppTheme.colors.textPrimary,
            // Centra el texto si ocupa varias líneas.
            textAlign = TextAlign.Center,
        )

        // Solo muestra la ubicación cuando se proporcionó.
        if (location != null) {
            // Separación entre el nombre y la ubicación.
            Spacer(Modifier.height(4.dp))

            // Fila con el icono de ubicación y el texto de la ciudad.
            IconTextRow(
                icon = Icons.Outlined.LocationOn,
                text = location,
                spacing = 4.dp,
            )
        }
    }
}

// Vista previa en Android Studio de la tarjeta de usuario con ubicación.
@Preview(showBackground = true)
@Composable
private fun UserSummaryCardPreview() {
    // Envuelve el componente en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Usuario de ejemplo con nombre y ciudad.
        UserSummaryCard(name = "Nicolás C.S", location = "Armenia, Colombia")
    }
}
