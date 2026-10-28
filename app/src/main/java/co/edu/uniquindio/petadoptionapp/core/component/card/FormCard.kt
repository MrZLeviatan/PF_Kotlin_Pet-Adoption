package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/** Tarjeta blanca de un formulario por pasos, con el título centrado arriba ("Información de la Mascota"). */
@Composable
fun FormCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppTheme.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FormCardPreview() {
    PetAdoptionAppTheme {
        FormCard(title = "Información de la Mascota") {
            Text("Contenido", color = AppTheme.colors.textPrimary)
        }
    }
}
