package co.edu.uniquindio.petadoptionapp.core.component.button

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp

/**
Medidas compartidas por los botones del sistema de diseño (PrimaryButton, SecondaryButton y DangerButton).
Centralizar aquí la forma de píldora y la altura mínima garantiza que dos botones distintos colocados
uno junto al otro (por ejemplo "Aceptar" y "Cancelar" de un diálogo) se vean exactamente iguales.
Son `internal` porque solo los componentes de este paquete deben usarlas; las pantallas consumen los botones ya armados.
**/

// Forma de píldora (bordes totalmente redondeados) definida por el diseño para los botones principales.
internal val AppButtonShape = CircleShape

// Altura mínima del botón: garantiza un área táctil cómoda (el mínimo recomendado por accesibilidad es 48 dp).
internal val AppButtonMinHeight = 52.dp
