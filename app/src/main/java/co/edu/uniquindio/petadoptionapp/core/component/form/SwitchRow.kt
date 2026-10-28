package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Fila con texto (y subtítulo opcional) a la izquierda y un interruptor a la derecha ("Vacunas al día").
 Toda la fila es tocable y TalkBack la anuncia como interruptor. Sin icono: para ajustes con
 icono está [SettingSwitchRow].
 */
@Composable
fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
            if (subtitle != null) {
                Text(subtitle, fontSize = 11.sp, color = AppTheme.colors.textPrimary.copy(alpha = 0.7f))
            }
        }
        Switch(
            checked = checked,
            // null: el clic lo maneja la fila; así no hay dos objetivos táctiles.
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppTheme.colors.chipSelected,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = AppTheme.colors.stateDisabled,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwitchRowPreview() {
    PetAdoptionAppTheme {
        SwitchRow(title = "Vacunas al día", checked = true, onCheckedChange = {}, subtitle = "Se verificará con certificado veterinario")
    }
}
