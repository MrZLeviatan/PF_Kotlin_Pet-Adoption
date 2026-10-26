package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.component.avatar.InitialAvatar
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/** Tarjeta con el avatar, el nombre y la ubicación del usuario. */
@Composable
fun UserSummaryCard(
    name: String,
    location: String,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        InitialAvatar(name = name, size = 88.dp)

        Spacer(Modifier.height(12.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = AppTheme.colors.textPrimary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = location,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.textPrimary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserSummaryCardPreview() {
    PetAdoptionAppTheme {
        UserSummaryCard(name = "Nicolás C.S", location = "Armenia, Colombia")
    }
}