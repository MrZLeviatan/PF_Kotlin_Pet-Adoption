package co.edu.uniquindio.petadoptionapp.core.component.header

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.component.button.BackButton
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Barra superior de pantallas secundarias: flecha de volver, título centrado
y un espacio opcional a la derecha para acciones (compartir, guardar...).
 */
@Composable
fun AppTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    actions: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        BackButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart))

        Text(
            text = title,
            style = titleStyle,
            color = AppTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            // Margen a ambos lados para no chocar con la flecha ni con las acciones.
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 56.dp),
        )

        Row(modifier = Modifier.align(Alignment.CenterEnd)) { actions() }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppTopBarPreview() {
    PetAdoptionAppTheme {
        AppTopBar(title = "Dashboard", onBack = {})
    }
}