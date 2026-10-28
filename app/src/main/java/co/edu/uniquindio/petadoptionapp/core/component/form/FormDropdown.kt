package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Lista desplegable obligatoria. Recibe los textos de las opciones y avisa la POSICIÓN elegida,
 así quien la usa traduce a su id (por ejemplo, la categoría del catálogo).
 [selectedIndex] en `null` = nada elegido todavía.
 */
@Composable
fun FormDropdown(
    label: String,
    options: List<String>,
    selectedIndex: Int?,
    onSelect: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val value = selectedIndex?.let { options.getOrNull(it) }.orEmpty()

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(requiredText(label)) },
            trailingIcon = { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null) },
            isError = errorText != null,
            supportingText = if (errorText != null) { { Text(errorText) } } else null,
            shape = RoundedCornerShape(12.dp),
            colors = formFieldColors(),
        )
        // Capa que abre el menú al tocar cualquier parte del campo.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(role = Role.DropdownList) { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = AppTheme.colors.surface,
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                DropdownMenuItem(
                    text = { Text(option, color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Medium) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    },
                    // La opción elegida lleva un contorno para distinguirla sin depender del color.
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .then(
                            if (isSelected) {
                                Modifier.border(1.5.dp, AppTheme.colors.textPrimary, RoundedCornerShape(8.dp))
                            } else {
                                Modifier
                            }
                        ),
                    colors = MenuDefaults.itemColors(
                        textColor = AppTheme.colors.textPrimary,
                        leadingIconColor = AppTheme.colors.textPrimary,
                        trailingIconColor = AppTheme.colors.textPrimary,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FormDropdownPreview() {
    PetAdoptionAppTheme {
        FormDropdown(label = "Categoría", options = listOf("Adopción", "Perdidos"), selectedIndex = 0, onSelect = {})
    }
}
