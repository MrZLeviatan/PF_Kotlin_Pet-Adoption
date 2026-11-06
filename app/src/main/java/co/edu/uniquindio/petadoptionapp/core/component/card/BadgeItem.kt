package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.component.icon.IconCircle
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Fila que representa una insignia conseguida por el usuario: icono en círculo, título, descripción
y una etiqueta a la derecha ("LVL 5", "Verificado"). Se usa en las secciones de logros/gamificación
del perfil. Es más compacta que AppCard (esquinas de 16 dp y relleno de 12 dp) porque suele
aparecer en listas de varias insignias seguidas.
**/
@Composable
fun BadgeItem(
    // Icono representativo de la insignia.
    icon: ImageVector,
    // Nombre de la insignia.
    title: String,
    // Descripción breve de cómo se obtuvo.
    description: String,
    // Etiqueta corta mostrada a la derecha (nivel o estado).
    tag: String,
    // Modificador opcional para personalizar el elemento desde afuera.
    modifier: Modifier = Modifier,
) {
    // Forma redondeada de 16 dp reutilizada por el fondo y el borde.
    val shape = RoundedCornerShape(16.dp)

    // Fila horizontal que distribuye icono, textos y etiqueta.
    Row(
        modifier = modifier
            // Ocupa todo el ancho disponible.
            .fillMaxWidth()
            // Fondo de superficie con esquinas redondeadas.
            .background(AppTheme.colors.surface, shape)
            // Borde neutro de 1 dp con la misma forma.
            .border(1.dp, AppTheme.colors.border, shape)
            // Relleno interno de la fila.
            .padding(12.dp),
        // Centra verticalmente todos los elementos de la fila.
        verticalAlignment = Alignment.CenterVertically,
        // Separación uniforme de 12 dp entre los elementos.
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Icono de la insignia dentro de un círculo de 40 dp con fondo de etiqueta.
        IconCircle(icon = icon, size = 40.dp, containerColor = AppTheme.colors.surfaceTag)

        // Columna de textos que ocupa el espacio restante entre el icono y la etiqueta.
        Column(modifier = Modifier.weight(1f)) {
            // Título de la insignia en estilo de etiqueta destacada.
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = AppTheme.colors.textPrimary,
            )
            // Descripción en texto pequeño debajo del título.
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.textPrimary,
            )
        }

        // Etiqueta tipo "píldora" alineada a la derecha.
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.textPrimary,
            // Fondo circular y relleno para que el texto luzca como una píldora.
            modifier = Modifier
                .background(AppTheme.colors.surfaceTag, CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

// Vista previa en Android Studio de una insignia de ejemplo.
@Preview(showBackground = true)
@Composable
private fun BadgeItemPreview() {
    // Envuelve el componente en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Insignia de ejemplo con datos ficticios.
        BadgeItem(
            icon = Icons.Outlined.WorkspacePremium,
            title = "Guardián de Huellas",
            description = "Nivel 5 alcanzado",
            tag = "LVL 5",
        )
    }
}