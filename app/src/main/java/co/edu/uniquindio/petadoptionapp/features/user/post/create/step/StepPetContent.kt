package co.edu.uniquindio.petadoptionapp.features.user.post.create.step

// Importación general de materiales y componentes visuales (androidx)
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.component.*
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostUiState
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostViewModel
import co.edu.uniquindio.petadoptionapp.features.user.post.create.component.SizeSelector

// Componente visual correspondiente al segundo paso del formulario (Información detallada de la mascota).
@Composable
fun StepPetContent(state: CreatePostUiState, viewModel: CreatePostViewModel) {
    // Tarjeta contenedora blanca con título centrado para agrupar los campos del paso 2.
    FormCard(title = "Información de la Mascota") {

        // Componente desplegable para seleccionar el tipo de animal (Perro, Gato, Otro).
        FormDropdown(
            value = state.animalType,
            label = "Tipo de Animal",
            options = listOf("Perro", "Gato", "Otro"),
            onSelect = viewModel::onAnimalTypeChange
        )
        // Inserta un espacio vertical de separación de 12dp.
        Spacer(modifier = Modifier.height(12.dp))

        // Campo de texto para ingresar la raza aproximada con validación de errores.
        FormTextField(state.breed, viewModel::onBreedChange, "Raza Aproximada", errorText = state.breedError)
        // Inserta un espacio vertical de separación de 12dp.
        Spacer(modifier = Modifier.height(12.dp))

        // Selector visual de tamaño (Pequeño, Mediano, Grande).
        SizeSelector(selected = state.size, onSelect = viewModel::onSizeChange)
        // Inserta un espacio vertical de separación de 12dp.
        Spacer(modifier = Modifier.height(12.dp))

        // Campo de texto para ingresar la edad aproximada con validación de errores.
        FormTextField(state.age, viewModel::onAgeChange, "Edad Aproximada", errorText = state.ageError)
        // Inserta un espacio vertical de separación de 12dp.
        Spacer(modifier = Modifier.height(12.dp))

        // Fila con interruptor (switch) para indicar si tiene las vacunas al día e incluye subtítulo.
        SwitchRow(
            title = "Vacunas al día",
            checked = state.vaccinated,
            onCheckedChange = viewModel::onVaccinatedChange,
            subtitle = "Se verificará con certificado veterinario"
        )
        // Inserta un espacio vertical de separación de 8dp.
        Spacer(modifier = Modifier.height(8.dp))

        // Fila con interruptor para indicar si la mascota está esterilizada o castrada.
        SwitchRow(
            title = "Esterilizado / Castrado",
            checked = state.sterilized,
            onCheckedChange = viewModel::onSterilizedChange
        )
    }
}