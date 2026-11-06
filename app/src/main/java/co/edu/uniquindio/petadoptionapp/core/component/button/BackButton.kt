package co.edu.uniquindio.petadoptionapp.core.component.button

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme

/**
Botón de "volver" con una flecha hacia atrás. Se usa en las barras superiores de las pantallas
secundarias para regresar a la pantalla anterior. Forma parte del sistema de diseño para que todas
las pantallas compartan el mismo icono, color y comportamiento de accesibilidad: el IconButton
aporta un área táctil de 48 dp y el icono incluye una descripción leída por TalkBack.
**/
@Composable
fun BackButton(
    // Acción que se ejecuta al presionar la flecha (normalmente navegar hacia atrás).
    onClick: () -> Unit,
    // Modificador opcional para ajustar posición o tamaño desde la pantalla que lo usa.
    modifier: Modifier = Modifier,
) {
    // Botón de icono de Material 3 que gestiona el clic y el área táctil mínima.
    IconButton(onClick = onClick, modifier = modifier) {
        // Dibuja la flecha de retroceso dentro del botón.
        Icon(
            // Usa la variante "AutoMirrored" para que la flecha se invierta en idiomas de derecha a izquierda.
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            // Texto accesible ("Volver") que anuncia TalkBack al enfocar el botón.
            contentDescription = stringResource(R.string.common_back),
            // Colorea la flecha con el color de texto principal del tema.
            tint = AppTheme.colors.textPrimary,
        )
    }
}