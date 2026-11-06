package co.edu.uniquindio.petadoptionapp.features.user.post.create.step

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.FormCard
import co.edu.uniquindio.petadoptionapp.core.component.form.FormDropdown
import co.edu.uniquindio.petadoptionapp.core.component.form.FormTextField
import co.edu.uniquindio.petadoptionapp.core.component.form.OptionSelector
import co.edu.uniquindio.petadoptionapp.core.component.form.SelectorOption
import co.edu.uniquindio.petadoptionapp.core.component.form.SwitchRow
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalSize
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.PostValidator
import co.edu.uniquindio.petadoptionapp.features.user.post.create.AgeUnit
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostActions
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostUiState

/**
Paso 2 del asistente de crear/editar publicación: tipo de animal (del catálogo), raza,
tamaño, edad (número + unidad meses/años) y salud (vacunado y esterilizado).
No tiene estado propio: lee los valores y errores de CreatePostUiState y avisa cada cambio
a CreatePostViewModel mediante las lambdas de CreatePostActions.
**/
@Composable
fun StepPetContent(state: CreatePostUiState, actions: CreatePostActions) {
    // Tarjeta del formulario con el título del paso.
    FormCard(title = stringResource(R.string.create_post_step_pet)) {
        // Lista desplegable para elegir el tipo de animal.
        FormDropdown(
            // Etiqueta del desplegable.
            label = stringResource(R.string.create_post_animal_label),
            // Nombres de los tipos de animal del catálogo.
            options = state.animalTypes.map { it.name },
            // Posición del tipo seleccionado, o null si no hay ninguno.
            selectedIndex = state.animalTypes.indexOfFirst { it.id == state.animalTypeId }.takeIf { it >= 0 },
            // Avisa al ViewModel la posición elegida.
            onSelect = actions.onAnimalTypeSelected,
        )

        // Espacio vertical entre campos.
        Spacer(modifier = Modifier.height(12.dp))

        // Campo de texto para la raza.
        FormTextField(
            // Valor actual de la raza.
            value = state.breed,
            // Avisa al ViewModel cada cambio.
            onValueChange = actions.onBreedChange,
            // Etiqueta del campo.
            label = stringResource(R.string.create_post_breed_label),
            // Mensaje de error con la longitud mínima (sin máximo práctico).
            errorText = lengthErrorText(state.breedError, CreatePostUiState.MIN_BREED_LENGTH, Int.MAX_VALUE),
        )

        // Espacio vertical entre campos.
        Spacer(modifier = Modifier.height(12.dp))

        // Selector del tamaño. El orden de las opciones es el del enum: Pequeño, Mediano, Grande.
        OptionSelector(
            // Etiqueta del selector.
            label = stringResource(R.string.create_post_size_label),
            // Una opción por cada valor de AnimalSize con su texto traducido.
            options = AnimalSize.entries.map { SelectorOption(sizeLabel(it)) },
            // Posición del tamaño elegido (su ordinal), o null.
            selectedIndex = state.size?.ordinal,
            // Avisa al ViewModel la posición elegida.
            onSelect = actions.onSizeSelected,
        )

        // Espacio vertical entre campos.
        Spacer(modifier = Modifier.height(12.dp))

        // Edad: el número y la unidad en la misma fila; se guarda en meses.
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Campo numérico para la edad.
            FormTextField(
                // Valor actual de la edad (texto).
                value = state.age,
                // Avisa al ViewModel cada cambio.
                onValueChange = actions.onAgeChange,
                // Etiqueta del campo.
                label = stringResource(R.string.create_post_age_label),
                // Mensaje de error con la edad máxima permitida.
                errorText = ageErrorText(state.ageError, PostValidator.MAX_AGE_IN_MONTHS),
                // Muestra el teclado numérico.
                keyboardType = KeyboardType.Number,
                // Etiqueta más pequeña: el campo comparte la fila con "Meses / Años".
                labelFontSize = 12.sp,
                // Ocupa el espacio restante de la fila.
                modifier = Modifier.weight(1f),
            )
            // Selector de la unidad de la edad.
            OptionSelector(
                // Una opción por cada AgeUnit con su texto traducido.
                options = AgeUnit.entries.map { SelectorOption(ageUnitLabel(it)) },
                // Posición de la unidad elegida.
                selectedIndex = state.ageUnit.ordinal,
                // Avisa al ViewModel la unidad elegida.
                onSelect = actions.onAgeUnitSelected,
                // Baja las unidades al nivel del campo (la etiqueta flotante deja espacio arriba).
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        // Espacio vertical entre campos.
        Spacer(modifier = Modifier.height(12.dp))

        // Interruptor de "vacunado".
        SwitchRow(
            // Título del interruptor.
            title = stringResource(R.string.create_post_vaccinated),
            // Estado actual.
            checked = state.vaccinated,
            // Avisa al ViewModel el cambio.
            onCheckedChange = actions.onVaccinatedChange,
            // Texto de ayuda debajo del título.
            subtitle = stringResource(R.string.create_post_vaccinated_hint),
        )

        // Espacio vertical entre interruptores.
        Spacer(modifier = Modifier.height(8.dp))

        // Interruptor de "esterilizado".
        SwitchRow(
            // Título del interruptor.
            title = stringResource(R.string.create_post_neutered),
            // Estado actual.
            checked = state.neutered,
            // Avisa al ViewModel el cambio.
            onCheckedChange = actions.onNeuteredChange,
        )
    }
}

// Devuelve el texto traducido de cada tamaño de animal.
@Composable
private fun sizeLabel(size: AnimalSize) = stringResource(
    // Elige el recurso de texto según el tamaño.
    when (size) {
        // Pequeño.
        AnimalSize.SMALL -> R.string.home_size_small
        // Mediano.
        AnimalSize.MEDIUM -> R.string.home_size_medium
        // Grande.
        AnimalSize.LARGE -> R.string.home_size_large
    }
)

// Devuelve el texto traducido de cada unidad de edad.
@Composable
private fun ageUnitLabel(unit: AgeUnit) = stringResource(
    // Elige el recurso de texto según la unidad.
    when (unit) {
        // Meses.
        AgeUnit.MONTHS -> R.string.create_post_age_months
        // Años.
        AgeUnit.YEARS -> R.string.create_post_age_years
    }
)
