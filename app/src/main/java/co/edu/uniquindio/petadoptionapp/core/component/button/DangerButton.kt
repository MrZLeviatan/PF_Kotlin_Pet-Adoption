package co.edu.uniquindio.petadoptionapp.core.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Botón para acciones destructivas o de cancelación ("Eliminar cuenta", "Cancelar" en diálogos).
Usa borde y texto en el color de error del tema para advertir al usuario del riesgo de la acción.
Es delineado y no relleno, para que no compita visualmente con el botón principal de la pantalla.
Comparte forma y altura con PrimaryButton y SecondaryButton a través de AppButtonDefaults.
**/
@Composable
fun DangerButton(
    // Texto que muestra el botón.
    text: String,
    // Acción que se ejecuta al presionar el botón.
    onClick: () -> Unit,
    // Modificador opcional para personalizar el botón desde afuera.
    modifier: Modifier = Modifier,
    // Indica si el botón está habilitado (por defecto activo).
    enabled: Boolean = true,
    // Por defecto en mayúsculas ("ELIMINAR CUENTA"); en diálogos se usa normal ("Cancelar").
    uppercase: Boolean = true,
) {
    // Dibuja un botón delineado de Material 3 con el estilo de peligro.
    OutlinedButton(
        // Conecta la acción recibida con el clic del botón.
        onClick = onClick,
        // Controla si el botón responde a toques.
        enabled = enabled,
        // Ocupa todo el ancho disponible y respeta la altura mínima compartida.
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppButtonMinHeight),
        // Aplica la forma de píldora común a los botones de la app.
        shape = AppButtonShape,
        // Borde de 1 dp en color de error para resaltar que la acción es destructiva.
        border = BorderStroke(1.dp, AppTheme.colors.stateError),
        // Define fondo de superficie y contenido (texto) en color de error.
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AppTheme.colors.surface,
            contentColor = AppTheme.colors.stateError,
        ),
    ) {
        // Muestra el texto, convirtiéndolo a mayúsculas solo si así se solicita.
        Text(text = if (uppercase) text.uppercase() else text, style = MaterialTheme.typography.labelLarge)
    }
}

// Vista previa en Android Studio del botón de peligro con el tema de la app.
@Preview(showBackground = true)
@Composable
private fun DangerButtonPreview() {
    // Envuelve el botón en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Ejemplo de uso con un texto destructivo típico.
        DangerButton(text = "Eliminar cuenta", onClick = {})
    }
}
