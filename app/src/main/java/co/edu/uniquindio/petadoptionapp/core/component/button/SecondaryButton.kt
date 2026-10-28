package co.edu.uniquindio.petadoptionapp.core.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Botón secundario delineado, con icono opcional ("Cerrar sesión").
 Misma forma que [DangerButton] pero en el color del texto: es una acción normal, no destructiva.
 El icono es decorativo: el texto ya la describe.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppButtonMinHeight),
        shape = AppButtonShape,
        border = BorderStroke(1.dp, AppTheme.colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AppTheme.colors.surface,
            contentColor = AppTheme.colors.textPrimary,
        ),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondaryButtonPreview() {
    PetAdoptionAppTheme {
        SecondaryButton(text = "Cerrar sesión", icon = Icons.AutoMirrored.Outlined.Logout, onClick = {})
    }
}
