package co.edu.uniquindio.petadoptionapp.core.component.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.component.icon.PawCircle
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Componente de título con la identidad visual de la app: una huella dentro de un círculo ([PawCircle]) seguida del
texto. Se usa como encabezado en pantallas como Registro ("Crear cuenta") o en barras con el nombre de la app
("Huellas"). El tamaño de la huella, su color de fondo, la separación y el estilo del texto son configurables.
**/

/**
Título con la huella a la izquierda ("Huellas", "Crear cuenta"). La huella es decorativa.
**/
@Composable
fun PawTitle(
    // Texto del título.
    text: String,
    // Modificador para posicionar el título.
    modifier: Modifier = Modifier,
    // Estilo tipográfico del texto.
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    // Tamaño del círculo de la huella.
    pawSize: Dp = 40.dp,
    // Color de fondo del círculo de la huella.
    pawContainerColor: Color = AppTheme.colors.surfaceTag,
    // Separación entre la huella y el texto.
    spacing: Dp = 10.dp,
) {
    // Fila con la huella y el texto.
    Row(
        // Aplica el modificador recibido.
        modifier = modifier,
        // Centra verticalmente huella y texto.
        verticalAlignment = Alignment.CenterVertically,
        // Separación configurable.
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        // Círculo decorativo con la huella.
        PawCircle(size = pawSize, containerColor = pawContainerColor)
        // Texto del título.
        Text(
            // Texto recibido.
            text = text,
            // Estilo configurable.
            style = style,
            // Color de texto principal del tema.
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Vista previa en Android Studio con el título "Crear cuenta".
@Preview(showBackground = true)
@Composable
private fun PawTitlePreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Título de ejemplo.
        PawTitle(text = "Crear cuenta")
    }
}
