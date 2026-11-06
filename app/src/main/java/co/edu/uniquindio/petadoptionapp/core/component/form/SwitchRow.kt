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
 Se usa en el paso de datos de la mascota al crear una publicación y en el
 diálogo de filtros de exploración.
**/
@Composable
fun SwitchRow(
    // Texto principal de la fila.
    title: String,
    // Estado actual del interruptor.
    checked: Boolean,
    // Se invoca con el nuevo estado al tocar la fila.
    onCheckedChange: (Boolean) -> Unit,
    // Modificador externo para posicionar la fila.
    modifier: Modifier = Modifier,
    // Texto secundario opcional bajo el título.
    subtitle: String? = null,
) {
    // Fila completa que actúa como interruptor.
    Row(
        modifier = modifier
            // Ocupa todo el ancho.
            .fillMaxWidth()
            // Toda la fila alterna el valor y se anuncia como interruptor.
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        // Centra verticalmente textos e interruptor.
        verticalAlignment = Alignment.CenterVertically,
        // Separa los textos del interruptor hacia los extremos.
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Columna de textos que ocupa el espacio restante.
        Column(modifier = Modifier.weight(1f)) {
            // Título en semi negrita.
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
            // Solo dibuja el subtítulo si existe.
            if (subtitle != null) {
                // Subtítulo pequeño y atenuado (70 % de opacidad) para marcar jerarquía.
                Text(subtitle, fontSize = 11.sp, color = AppTheme.colors.textPrimary.copy(alpha = 0.7f))
            }
        }
        // Interruptor visual a la derecha.
        Switch(
            checked = checked,
            // null: el clic lo maneja la fila; así no hay dos objetivos táctiles.
            onCheckedChange = null,
            // Colores del interruptor: pulgar blanco fijo, pista canela activada y gris desactivada.
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

// Vista previa de la fila activada con subtítulo.
@Preview(showBackground = true)
@Composable
private fun SwitchRowPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Fila de ejemplo sobre vacunas.
        SwitchRow(title = "Vacunas al día", checked = true, onCheckedChange = {}, subtitle = "Se verificará con certificado veterinario")
    }
}
