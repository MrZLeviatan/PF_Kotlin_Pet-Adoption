package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Filas reutilizables de la pantalla de Ajustes (SettingsScreen): una con
 interruptor y otra que abre un diálogo o pantalla. Ambas comparten la misma
 estructura privada (SettingRowLayout) para que todas las filas tengan igual
 altura, icono y tipografía labelLarge del tema.
**/

/**
 Fila de ajuste con interruptor: icono, texto y switch a la derecha.
 Toda la fila es tocable (no solo el switch) y TalkBack la anuncia como un interruptor.
**/
@Composable
fun SettingSwitchRow(
    // Icono decorativo del ajuste.
    icon: ImageVector,
    // Nombre del ajuste.
    title: String,
    // Estado actual del interruptor.
    checked: Boolean,
    // Se invoca con el nuevo estado al tocar la fila.
    onCheckedChange: (Boolean) -> Unit,
    // Modificador externo para posicionar la fila.
    modifier: Modifier = Modifier,
    // Permite deshabilitar el ajuste (por ejemplo, mientras se guarda).
    enabled: Boolean = true,
) {
    // Usa la estructura común con la fila completa como interruptor.
    SettingRowLayout(
        icon = icon,
        title = title,
        // toggleable convierte toda la fila en el objetivo táctil del interruptor.
        modifier = modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        ),
    ) {
        // Interruptor visual a la derecha.
        Switch(
            checked = checked,
            // null: el clic lo maneja la fila; así no hay dos objetivos táctiles.
            onCheckedChange = null,
            enabled = enabled,
            // Colores del interruptor tomados del tema, sin borde.
            colors = SwitchDefaults.colors(
                // Activado: pulgar de superficie sobre pista amarilla de acción.
                checkedThumbColor = AppTheme.colors.surface,
                checkedTrackColor = AppTheme.colors.actionPrimary,
                checkedBorderColor = Color.Transparent,
                // Desactivado: pulgar de superficie sobre pista de etiqueta.
                uncheckedThumbColor = AppTheme.colors.surface,
                uncheckedTrackColor = AppTheme.colors.surfaceTag,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

/** Fila de ajuste que abre algo (diálogo o pantalla): icono, texto y flecha a la derecha. */
@Composable
fun SettingActionRow(
    // Icono decorativo del ajuste.
    icon: ImageVector,
    // Nombre de la acción.
    title: String,
    // Acción al tocar la fila.
    onClick: () -> Unit,
    // Modificador externo para posicionar la fila.
    modifier: Modifier = Modifier,
) {
    // Usa la estructura común con la fila completa como botón.
    SettingRowLayout(
        icon = icon,
        title = title,
        // Toda la fila es tocable y se anuncia como botón.
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
    ) {
        // Decorativa: la fila ya se anuncia como botón.
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.colors.textPrimary,
        )
    }
}

// Estructura común: alto mínimo de 48 dp (accesibilidad), icono decorativo y texto que ocupa el resto.
@Composable
private fun SettingRowLayout(
    // Icono del ajuste.
    icon: ImageVector,
    // Texto del ajuste.
    title: String,
    // Modificador que trae el comportamiento táctil (toggleable o clickable).
    modifier: Modifier,
    // Contenido a la derecha (switch o flecha).
    trailing: @Composable () -> Unit,
) {
    // Fila con icono, texto y contenido final.
    Row(
        modifier = modifier
            // Ocupa todo el ancho.
            .fillMaxWidth()
            // Altura táctil mínima recomendada.
            .heightIn(min = 48.dp),
        // Centra los elementos verticalmente.
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icono decorativo a la izquierda.
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.colors.textPrimary,
            modifier = Modifier.size(20.dp),
        )
        // Separación entre icono y texto.
        Spacer(Modifier.width(12.dp))
        // Texto del ajuste que ocupa el espacio restante y empuja el contenido final a la derecha.
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        // Dibuja el contenido final recibido.
        trailing()
    }
}

// Vista previa de una fila de acción y dos con interruptor.
@Preview(showBackground = true)
@Composable
private fun SettingRowsPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Apila las filas de ejemplo.
        Column {
            // Fila que abriría el cambio de contraseña.
            SettingActionRow(icon = Icons.Outlined.Lock, title = "Cambiar contraseña", onClick = {})
            // Interruptor activado.
            SettingSwitchRow(icon = Icons.Outlined.Visibility, title = "Perfil público", checked = true, onCheckedChange = {})
            // Interruptor desactivado.
            SettingSwitchRow(icon = Icons.Outlined.Lock, title = "Verificación en dos pasos", checked = false, onCheckedChange = {})
        }
    }
}
