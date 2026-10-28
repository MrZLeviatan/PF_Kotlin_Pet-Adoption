package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/** Métrica individual: etiqueta pequeña en mayúsculas y el valor grande debajo. */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, shape)
            .border(1.dp, AppTheme.colors.border, shape)
            .padding(12.dp),
    ) {
        Text(
            // Se guarda normal y se pasa a mayúsculas aquí, para que TalkBack lo lea como palabra.
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.textPrimary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = AppTheme.colors.textPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatCardPreview() {
    PetAdoptionAppTheme {
        StatCard(label = "Activas", value = "12")
    }
}