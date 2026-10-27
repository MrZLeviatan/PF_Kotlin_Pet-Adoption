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
 Fila de ajuste con interruptor: icono, texto y switch a la derecha.
 Toda la fila es tocable (no solo el switch) y TalkBack la anuncia como un interruptor.
 */
@Composable
fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    SettingRowLayout(
        icon = icon,
        title = title,
        modifier = modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        ),
    ) {
        Switch(
            checked = checked,
            // null: el clic lo maneja la fila; así no hay dos objetivos táctiles.
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppTheme.colors.surface,
                checkedTrackColor = AppTheme.colors.actionPrimary,
                checkedBorderColor = Color.Transparent,
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
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingRowLayout(
        icon = icon,
        title = title,
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
    icon: ImageVector,
    title: String,
    modifier: Modifier,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.colors.textPrimary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingRowsPreview() {
    PetAdoptionAppTheme {
        Column {
            SettingActionRow(icon = Icons.Outlined.Lock, title = "Cambiar contraseña", onClick = {})
            SettingSwitchRow(icon = Icons.Outlined.Visibility, title = "Perfil público", checked = true, onCheckedChange = {})
            SettingSwitchRow(icon = Icons.Outlined.Lock, title = "Verificación en dos pasos", checked = false, onCheckedChange = {})
        }
    }
}
