package co.edu.uniquindio.petadoptionapp.features.user.post.create.step

// Importación general de materiales y componentes visuales (androidx)
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import co.edu.uniquindio.petadoptionapp.core.component.*
import co.edu.uniquindio.petadoptionapp.core.theme.TextoMarron
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostUiState
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostViewModel

// Componente visual correspondiente al primer paso del formulario (Información general de la publicación).
@Composable
fun StepInfoContent(state: CreatePostUiState, viewModel: CreatePostViewModel) {
    // Tarjeta contenedora blanca con título centrado para agrupar los campos del paso 1.
    FormCard(title = "Información de la Publicación") {

        // Campo de texto reutilizable para ingresar el título de la publicación.
        FormTextField(
            value = state.title,
            onValueChange = viewModel::onTitleChange,
            label = "Titulo de la Publicación",
            errorText = state.titleError          // Muestra el mensaje de error correspondiente si existe.
        )

        // Inserta un espacio vertical de separación de 16dp.
        Spacer(modifier = Modifier.height(16.dp))

        // Componente desplegable (Dropdown) para seleccionar la categoría de la publicación.
        FormDropdown(
            value = state.category,
            label = "Categoría",
            options = listOf("Adopción", "Mascota perdida", "Mascota encontrada"),
            onSelect = viewModel::onCategoryChange
        )

        // Inserta un espacio vertical de separación de 16dp.
        Spacer(modifier = Modifier.height(16.dp))

        // Etiqueta de texto obligatoria para el campo de descripción con asterisco rojo.
        Text(requiredText("Descripción"), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextoMarron)
        // Inserta un pequeño espacio vertical de 6dp debajo de la etiqueta.
        Spacer(modifier = Modifier.height(6.dp))

        // Campo de texto multilínea expandido para escribir la descripción detallada de la mascota.
        OutlinedTextField(
            value = state.description,
            onValueChange = viewModel::onDescriptionChange,
            // Ocupa todo el ancho disponible y fija una altura amplia de 180dp.
            modifier = Modifier.fillMaxWidth().height(180.dp),
            // Muestra un texto guía temporal orientativo en tamaño de 13sp cuando está vacío.
            placeholder = { Text("Describe la situación de la mascota, su personalidad, necesidades especiales...", fontSize = 13.sp) },
            // Cambia los bordes a color rojo si la descripción contiene algún error de validación.
            isError = state.descriptionError != null,
            // Texto de apoyo inferior que muestra dinámicamente el error o el contador de caracteres.
            supportingText = {
                // Si hay error, lo muestra junto al contador. Si no, solo muestra el contador actual sobre 500.
                Text(
                    text = state.descriptionError
                        ?.let { "$it (${state.description.length}/500)" }
                        ?: "${state.description.length}/500 caracteres",
                    modifier = Modifier.fillMaxWidth(),
                    // Alinea el texto de apoyo completamente hacia la derecha.
                    textAlign = TextAlign.End
                )
            },
            // Redondea las esquinas del campo de texto con un radio de 12dp.
            shape = RoundedCornerShape(12.dp),
            // Aplica la paleta de colores unificada para los campos del formulario.
            colors = formFieldColors()
        )
    }
}