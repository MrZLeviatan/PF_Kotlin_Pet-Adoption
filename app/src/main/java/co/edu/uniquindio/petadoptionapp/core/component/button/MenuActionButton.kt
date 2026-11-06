package co.edu.uniquindio.petadoptionapp.core.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Botón de menú: icono a la izquierda, texto en el centro y flecha a la derecha.
Sirve para listas de accesos rápidos (perfil, configuración), donde cada fila lleva a otra pantalla.
Usa el color de acción principal y esquinas redondeadas (no píldora) para diferenciarse de los botones de acción.
Los iconos son decorativos: el texto ya describe la acción, por eso no tienen contentDescription.
**/
@Composable
fun MenuActionButton(
    // Texto que describe la opción del menú.
    text: String,
    // Icono decorativo que acompaña la opción.
    icon: ImageVector,
    // Acción que se ejecuta al presionar la opción (normalmente navegar).
    onClick: () -> Unit,
    // Modificador opcional para personalizar el botón desde afuera.
    modifier: Modifier = Modifier,
) {
    // Botón relleno de Material 3 que actúa como fila del menú.
    Button(
        // Conecta la acción recibida con el clic.
        onClick = onClick,
        // Ocupa todo el ancho y garantiza una altura mínima de 56 dp para un toque cómodo.
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        // Esquinas redondeadas de 16 dp, propias de las filas de menú.
        shape = RoundedCornerShape(16.dp),
        // Relleno horizontal interno para separar el contenido de los bordes.
        contentPadding = PaddingValues(horizontal = 16.dp),
        // Fondo en color de acción principal y contenido en su color de contraste.
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.actionPrimary,
            contentColor = AppTheme.colors.onActionPrimary,
        ),
    ) {
        // Icono decorativo de la opción, a la izquierda y de 20 dp.
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
        // Separación horizontal entre el icono y el texto.
        Spacer(Modifier.width(12.dp))
        // Texto de la opción.
        Text(
            text = text,
            // Estilo tipográfico estándar de etiquetas de botón.
            style = MaterialTheme.typography.labelLarge,
            // Ocupa el espacio sobrante para empujar la flecha hacia el extremo derecho.
            modifier = Modifier.weight(1f),
        )
        // Flecha decorativa que indica que la opción abre otra pantalla.
        Icon(
            // Variante "AutoMirrored" para invertirse en idiomas de derecha a izquierda.
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            // Sin descripción porque es puramente decorativa.
            contentDescription = null,
        )
    }
}

// Vista previa en Android Studio del botón de menú con el tema de la app.
@Preview(showBackground = true)
@Composable
private fun MenuActionButtonPreview() {
    // Envuelve el botón en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Ejemplo de opción "Configuración" con su icono.
        MenuActionButton(text = "Configuración", icon = Icons.Outlined.Settings, onClick = {})
    }
}