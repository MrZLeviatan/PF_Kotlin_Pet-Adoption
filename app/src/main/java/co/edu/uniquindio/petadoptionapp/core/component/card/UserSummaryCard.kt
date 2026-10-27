package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.component.icon.InitialAvatar
import co.edu.uniquindio.petadoptionapp.core.component.icon.UserAvatar
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTextRow
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Tarjeta con el avatar, el nombre y la ubicación del usuario.
 El avatar sale de [name]; [displayName] permite mostrar otro texto ("Username: ana_77").
 */
@Composable
fun UserSummaryCard(
    name: String,
    location: String,
    modifier: Modifier = Modifier,
    displayName: String = name,
    photoUrl: String? = null,
    contentPadding: PaddingValues = PaddingValues(20.dp),
) {
    AppCard(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = contentPadding,
    ) {

        UserAvatar(name = name, photoUrl = photoUrl, size = 88.dp)

        Spacer(Modifier.height(12.dp))

        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineSmall,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(4.dp))

        IconTextRow(
            icon = Icons.Outlined.LocationOn,
            text = location,
            spacing = 4.dp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UserSummaryCardPreview() {
    PetAdoptionAppTheme {
        UserSummaryCard(name = "Nicolás C.S", location = "Armenia, Colombia")
    }
}
