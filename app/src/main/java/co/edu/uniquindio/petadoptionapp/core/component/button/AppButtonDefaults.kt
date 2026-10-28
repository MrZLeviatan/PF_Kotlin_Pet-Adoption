package co.edu.uniquindio.petadoptionapp.core.component.button

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp

/*
 Medidas compartidas por PrimaryButton, SecondaryButton y DangerButton: forma de píldora y la misma altura,
 así dos botones distintos uno junto al otro (ej. "Aceptar" y "Cancelar" de un diálogo) se ven iguales.
 */

// Forma de píldora, como en el diseño.
internal val AppButtonShape = CircleShape

// Altura mínima: área táctil cómoda (el mínimo recomendado es 48 dp).
internal val AppButtonMinHeight = 52.dp
