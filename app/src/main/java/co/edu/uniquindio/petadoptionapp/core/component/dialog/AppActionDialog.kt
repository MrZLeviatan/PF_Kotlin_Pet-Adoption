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
Diálogo de resultado que se muestra al terminar un flujo importante (por ejemplo al enviar una
publicación) y obliga al usuario a elegir hacia dónde continuar. Forma parte de la familia de
diálogos del sistema de diseño junto a AppInfoDialog, AppConfirmDialog y AppFormDialog.
**/

/**
Diálogo de resultado con dos caminos ("Publicación enviada": Ver mi publicación / Volver al inicio).
Icono de huella, título, mensaje, una etiqueta opcional destacada ([highlight]) y dos botones:
el principal amarillo y el secundario naranja. No se cierra al tocar fuera: hay que elegir.
**/
@Composable
fun AppActionDialog(
    // Título del diálogo.
    title: String,
    // Mensaje explicativo bajo el título.
    message: String,
    // Texto del botón principal.
    primaryText: String,
    // Acción del botón principal.
    onPrimary: () -> Unit,
    // Texto del botón secundario.
    secondaryText: String,
    // Acción del botón secundario.
    onSecondary: () -> Unit,
    // Modificador opcional para personalizar la tarjeta del diálogo.
    modifier: Modifier = Modifier,
    // Etiqueta opcional destacada (p. ej. "Tiempo estimado: 24-48 horas").
    highlight: String? = null,
) {
    // Ventana de diálogo que se muestra sobre la pantalla actual.
    Dialog(
        // Lambda vacía: el diálogo no se cierra al tocar fuera ni con "atrás"; el usuario debe elegir un botón.
        onDismissRequest = {},
        // Desactiva el ancho por defecto de la plataforma para controlar el ancho con márgenes propios.
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Tarjeta que contiene todo el diálogo.
        Card(
            // Ocupa el ancho de la pantalla dejando 24 dp de margen a cada lado.
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            // Esquinas muy redondeadas (28 dp) propias de los diálogos.
            shape = RoundedCornerShape(28.dp),
            // Fondo con el color de fondo general del tema.
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.background),
        ) {
            // Columna centrada con relleno de 24 dp que apila icono, textos y botones.
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Círculo de 56 dp con fondo de acento que contiene la huella.
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(AppTheme.colors.chipSelected, CircleShape),
                    // Centra el icono dentro del círculo.
                    contentAlignment = Alignment.Center,
                ) {
                    // Huella decorativa; el título ya comunica el resultado.
                    Icon(Icons.Filled.Pets, contentDescription = null, tint = AppTheme.colors.textPrimary)
                }
                // Separación entre el icono y el título.
                Spacer(modifier = Modifier.height(16.dp))
                // Título grande y en negrita extra, centrado.
                Text(title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = AppTheme.colors.textPrimary, textAlign = TextAlign.Center)
                // Separación entre el título y el mensaje.
                Spacer(modifier = Modifier.height(12.dp))
                // Mensaje explicativo centrado.
                Text(text = message, textAlign = TextAlign.Center, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                // Solo muestra la etiqueta destacada si se proporcionó.
                if (highlight != null) {
                    // Separación entre el mensaje y la etiqueta.
                    Spacer(modifier = Modifier.height(16.dp))
                    // Píldora con fondo de aviso que resalta el dato.
                    Surface(shape = RoundedCornerShape(50), color = AppTheme.colors.surfaceNotice) {
                        // Texto de la etiqueta destacada.
                        Text(
                            text = highlight,
                            // Relleno interno de la píldora.
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = AppTheme.colors.textPrimary,
                        )
                    }
                }
                // Separación entre el contenido y los botones.
                Spacer(modifier = Modifier.height(24.dp))
                // Botón principal (amarillo) del sistema de diseño.
                PrimaryButton(
                    text = primaryText,
                    onClick = onPrimary,
                    // Altura fija de 52 dp para igualarlo con el botón secundario.
                    modifier = Modifier.height(52.dp),
                    // Texto algo más grande y en negrita para este diálogo.
                    textStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                )
                // Separación entre los dos botones.
                Spacer(modifier = Modifier.height(12.dp))
                // Botón secundario relleno en color naranja (acción secundaria del tema).
                Button(
                    onClick = onSecondary,
                    // Ancho completo y misma altura que el botón principal.
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    // Forma de píldora (50 % de redondeo).
                    shape = RoundedCornerShape(50),
                    // Fondo naranja con texto blanco.
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.actionSecondary, contentColor = Color.White),
                ) {
                    // Texto del botón secundario en negrita.
                    Text(secondaryText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// Vista previa en Android Studio del diálogo de publicación enviada.
@Preview
@Composable
private fun AppActionDialogPreview() {
    // Envuelve el diálogo en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Diálogo de ejemplo con etiqueta destacada y dos acciones.
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
