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
 Se usa en los pasos de crear publicación, el selector de ubicación y el diálogo
 de filtros. Reutiliza `requiredText` y `formFieldColors` de FormFields.kt para
 verse igual que el resto de campos del formulario.
**/
@Composable
fun FormDropdown(
    // Texto de la etiqueta del campo (se le agrega el asterisco de obligatorio).
    label: String,
    // Textos de las opciones del menú.
    options: List<String>,
    // Posición de la opción elegida, o null si aún no hay ninguna.
    selectedIndex: Int?,
    // Avisa la posición de la opción que el usuario eligió.
    onSelect: (index: Int) -> Unit,
    // Modificador externo para posicionar el campo.
    modifier: Modifier = Modifier,
    // Mensaje de error bajo el campo; null si no hay error.
    errorText: String? = null,
) {
    // Estado local que indica si el menú desplegable está abierto.
    var expanded by remember { mutableStateOf(false) }
    // Texto a mostrar: la opción elegida o cadena vacía si no hay selección o el índice no existe.
    val value = selectedIndex?.let { options.getOrNull(it) }.orEmpty()

    // Box para superponer el campo, la capa táctil y el menú.
    Box(modifier = modifier) {
        // Campo de solo lectura que muestra la opción elegida con aspecto de formulario.
        OutlinedTextField(
            value = value,
            // No se escribe en él: el valor solo cambia eligiendo en el menú.
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            // Etiqueta con asterisco rojo de campo obligatorio.
            label = { Text(requiredText(label)) },
            // Flecha hacia abajo que sugiere que el campo despliega opciones.
            trailingIcon = { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null) },
            // Marca el campo en error cuando hay mensaje.
            isError = errorText != null,
            // Muestra el mensaje de error solo cuando existe.
            supportingText = if (errorText != null) { { Text(errorText) } } else null,
            // Esquinas redondeadas de 12 dp, iguales a los demás campos del formulario.
            shape = RoundedCornerShape(12.dp),
            // Paleta compartida de los campos del formulario.
            colors = formFieldColors(),
        )
        // Capa que abre el menú al tocar cualquier parte del campo.
        Box(
            modifier = Modifier
                // Cubre exactamente el área del campo.
                .matchParentSize()
                // Al tocar abre el menú y se anuncia a TalkBack como lista desplegable.
                .clickable(role = Role.DropdownList) { expanded = true },
        )
        // Menú con las opciones disponibles.
        DropdownMenu(
            expanded = expanded,
            // Cierra el menú al tocar fuera o pulsar atrás.
            onDismissRequest = { expanded = false },
            // Fondo del menú con el color de superficie del tema.
            containerColor = AppTheme.colors.surface,
        ) {
            // Crea un ítem por opción conservando su posición.
            options.forEachIndexed { index, option ->
                // Indica si esta opción es la elegida actualmente.
                val isSelected = index == selectedIndex
                // Ítem individual del menú.
                DropdownMenuItem(
                    // Texto de la opción con peso medio.
                    text = { Text(option, color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Medium) },
                    onClick = {
                        // Avisa la posición elegida.
                        onSelect(index)
                        // Cierra el menú tras elegir.
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
                    // Colores del texto e íconos del ítem tomados del tema.
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

// Vista previa con dos categorías y la primera seleccionada.
@Preview(showBackground = true)
@Composable
private fun FormDropdownPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Desplegable de ejemplo para categorías.
        FormDropdown(label = "Categoría", options = listOf("Adopción", "Perdidos"), selectedIndex = 0, onSelect = {})
    }
}