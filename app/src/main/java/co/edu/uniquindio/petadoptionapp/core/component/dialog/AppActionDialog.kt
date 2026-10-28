package co.edu.uniquindio.petadoptionapp.core.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Diálogo de resultado con dos caminos ("Publicación enviada": Ver mi publicación / Volver al inicio).
 Icono de huella, título, mensaje, una etiqueta opcional destacada ([highlight]) y dos botones:
 el principal amarillo y el secundario naranja. No se cierra al tocar fuera: hay que elegir.
 */
@Composable
fun AppActionDialog(
    title: String,
    message: String,
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier,
    highlight: String? = null,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.background),
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(AppTheme.colors.chipSelected, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Pets, contentDescription = null, tint = AppTheme.colors.textPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = AppTheme.colors.textPrimary, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = message, textAlign = TextAlign.Center, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                if (highlight != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(shape = RoundedCornerShape(50), color = AppTheme.colors.surfaceNotice) {
                        Text(
                            text = highlight,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = AppTheme.colors.textPrimary,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                PrimaryButton(
                    text = primaryText,
                    onClick = onPrimary,
                    modifier = Modifier.height(52.dp),
                    textStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.actionSecondary, contentColor = Color.White),
                ) {
                    Text(secondaryText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Preview
@Composable
private fun AppActionDialogPreview() {
    PetAdoptionAppTheme {
        AppActionDialog(
            title = "Publicación enviada",
            message = "Tu publicación está pendiente de verificación.",
            highlight = "Tiempo estimado: 24-48 horas",
            primaryText = "Ver mi publicación",
            onPrimary = {},
            secondaryText = "Volver al inicio",
            onSecondary = {},
        )
    }
}
