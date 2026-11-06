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
Se reutiliza en pantallas como ajustes, editar perfil, comentarios, métricas o
crear publicación, de modo que todas compartan la misma altura (56 dp), la
tipografía de título del tema y el color de texto principal.
**/
@Composable
fun AppTopBar(
    // Texto del título que se muestra centrado.
    title: String,
    // Acción al pulsar la flecha de volver (normalmente navegar hacia atrás).
    onBack: () -> Unit,
    // Modificador externo para ajustar la barra desde la pantalla.
    modifier: Modifier = Modifier,
    // Estilo del título; por defecto titleMedium del tema.
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    // Contenido opcional a la derecha (iconos de acción); vacío por defecto.
    actions: @Composable () -> Unit = {},
) {
    // Box permite superponer flecha, título y acciones alineándolos de forma independiente.
    Box(
        modifier = modifier
            // Ocupa todo el ancho de la pantalla.
            .fillMaxWidth()
            // Altura estándar de una barra superior.
            .height(56.dp),
    ) {
        // Botón de volver anclado al borde izquierdo y centrado verticalmente.
        BackButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart))

        // Título de la pantalla.
        Text(
            text = title,
            style = titleStyle,
            color = AppTheme.colors.textPrimary,
            // Limita el título a una sola línea.
            maxLines = 1,
            // Si no cabe, se corta con puntos suspensivos.
            overflow = TextOverflow.Ellipsis,
            // Margen a ambos lados para no chocar con la flecha ni con las acciones.
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 56.dp),
        )

        // Fila de acciones anclada al borde derecho.
        Row(modifier = Modifier.align(Alignment.CenterEnd)) { actions() }
    }
}

// Vista previa de la barra con un título de ejemplo.
@Preview(showBackground = true)
@Composable
private fun AppTopBarPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Barra de ejemplo con acción de volver vacía.
        AppTopBar(title = "Dashboard", onBack = {})
    }
}
