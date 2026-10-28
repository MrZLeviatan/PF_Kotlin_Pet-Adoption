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

/** Paso 2: tipo de animal (del catálogo), raza, tamaño, edad y salud. */
@Composable
fun StepPetContent(state: CreatePostUiState, actions: CreatePostActions) {
    FormCard(title = stringResource(R.string.create_post_step_pet)) {
        FormDropdown(
            label = stringResource(R.string.create_post_animal_label),
            options = state.animalTypes.map { it.name },
            selectedIndex = state.animalTypes.indexOfFirst { it.id == state.animalTypeId }.takeIf { it >= 0 },
            onSelect = actions.onAnimalTypeSelected,
        )

        Spacer(modifier = Modifier.height(12.dp))

        FormTextField(
            value = state.breed,
            onValueChange = actions.onBreedChange,
            label = stringResource(R.string.create_post_breed_label),
            errorText = lengthErrorText(state.breedError, CreatePostUiState.MIN_BREED_LENGTH, Int.MAX_VALUE),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // El orden de las opciones es el del enum: Pequeño, Mediano, Grande.
        OptionSelector(
            label = stringResource(R.string.create_post_size_label),
            options = AnimalSize.entries.map { SelectorOption(sizeLabel(it)) },
            selectedIndex = state.size?.ordinal,
            onSelect = actions.onSizeSelected,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Edad: el número y la unidad; se guarda en meses.
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FormTextField(
                value = state.age,
                onValueChange = actions.onAgeChange,
                label = stringResource(R.string.create_post_age_label),
                errorText = ageErrorText(state.ageError, PostValidator.MAX_AGE_IN_MONTHS),
                keyboardType = KeyboardType.Number,
                // Más pequeña: el campo comparte la fila con "Meses / Años".
                labelFontSize = 12.sp,
                modifier = Modifier.weight(1f),
            )
            OptionSelector(
                options = AgeUnit.entries.map { SelectorOption(ageUnitLabel(it)) },
                selectedIndex = state.ageUnit.ordinal,
                onSelect = actions.onAgeUnitSelected,
                // Baja las unidades al nivel del campo (la etiqueta flotante deja espacio arriba).
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SwitchRow(
            title = stringResource(R.string.create_post_vaccinated),
            checked = state.vaccinated,
            onCheckedChange = actions.onVaccinatedChange,
            subtitle = stringResource(R.string.create_post_vaccinated_hint),
        )

        Spacer(modifier = Modifier.height(8.dp))

        SwitchRow(
            title = stringResource(R.string.create_post_neutered),
            checked = state.neutered,
            onCheckedChange = actions.onNeuteredChange,
        )
    }
}

@Composable
private fun sizeLabel(size: AnimalSize) = stringResource(
    when (size) {
        AnimalSize.SMALL -> R.string.home_size_small
        AnimalSize.MEDIUM -> R.string.home_size_medium
        AnimalSize.LARGE -> R.string.home_size_large
    }
)

@Composable
private fun ageUnitLabel(unit: AgeUnit) = stringResource(
    when (unit) {
        AgeUnit.MONTHS -> R.string.create_post_age_months
        AgeUnit.YEARS -> R.string.create_post_age_years
    }
)
