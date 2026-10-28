package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/** Aviso informativo con icono y mensaje. El icono es decorativo: el texto ya dice todo. */
@Composable
fun NoticeBanner(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.WarningAmber,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.chipSelected, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.colors.textPrimary,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoticeBannerPreview() {
    PetAdoptionAppTheme {
        NoticeBanner(message = "La publicación rechazada no aparecerá en el feed público.")
    }
}