// Silencia los avisos de "no usado" porque estos alias solo existen para compatibilidad temporal.
@file:Suppress("unused")

package co.edu.uniquindio.petadoptionapp.core.theme

/**
 Alias temporales de la nomenclatura anterior.
 Antes del sistema de roles (AppColors) las pantallas usaban constantes con nombres
 en español como `FondoMarron` o `BotonHabilitado`. Este archivo las conserva
 marcadas como @Deprecated con su `ReplaceWith`, para que el IDE ofrezca migrar
 cada uso a `AppTheme.colors.<rol>` con un clic.
 Eliminar este archivo cuando no queden warnings de deprecación.
 Devuelven siempre valores del tema claro (no reaccionan a modo oscuro).
**/

// Import que el IDE agrega automáticamente al aplicar la sugerencia de reemplazo.
private const val IMPORT = "co.edu.uniquindio.petadoptionapp.core.theme.AppTheme"

// Fondos
// Antiguo fondo marrón; equivale al color de marca.
@Deprecated("Usar AppTheme.colors.brand", ReplaceWith("AppTheme.colors.brand", IMPORT))
val FondoMarron get() = LightAppColors.brand

// Antiguo fondo crema; equivale a la superficie elevada.
@Deprecated("Usar AppTheme.colors.surfaceElevated", ReplaceWith("AppTheme.colors.surfaceElevated", IMPORT))
val FondoCrema get() = LightAppColors.surfaceElevated

// Antiguo fondo claro de pantalla; equivale al fondo general.
@Deprecated("Usar AppTheme.colors.background", ReplaceWith("AppTheme.colors.background", IMPORT))
val FondoClaro get() = LightAppColors.background

// Antiguo fondo de etiquetas; equivale a la superficie de etiqueta.
@Deprecated("Usar AppTheme.colors.surfaceTag", ReplaceWith("AppTheme.colors.surfaceTag", IMPORT))
val FondoEtiquetasClaro get() = LightAppColors.surfaceTag

// Antiguo fondo de tarjetas; equivale a la superficie base.
@Deprecated("Usar AppTheme.colors.surface", ReplaceWith("AppTheme.colors.surface", IMPORT))
val FondoTarjeta get() = LightAppColors.surface

// Antiguo fondo de avisos; equivale a la superficie de aviso.
@Deprecated("Usar AppTheme.colors.surfaceNotice", ReplaceWith("AppTheme.colors.surfaceNotice", IMPORT))
val FondoAviso get() = LightAppColors.surfaceNotice

// Antiguo fondo de chips; se unificó con la superficie de etiqueta.
@Deprecated("Usar AppTheme.colors.surfaceTag", ReplaceWith("AppTheme.colors.surfaceTag", IMPORT))
val FondoChip get() = LightAppColors.surfaceTag

// Items
// Antiguo color de ítem seleccionado; equivale al estado seleccionado.
@Deprecated("Usar AppTheme.colors.stateSelected", ReplaceWith("AppTheme.colors.stateSelected", IMPORT))
val ItemSeleccionado get() = LightAppColors.stateSelected

// Antiguo color del botón de crear publicación; comparte el estado seleccionado.
@Deprecated("Usar AppTheme.colors.stateSelected", ReplaceWith("AppTheme.colors.stateSelected", IMPORT))
val ItemCrearFeed get() = LightAppColors.stateSelected

// Antiguo color de ítem no seleccionado; equivale al estado no seleccionado.
@Deprecated("Usar AppTheme.colors.stateUnselected", ReplaceWith("AppTheme.colors.stateUnselected", IMPORT))
val ItemNoSeleccionado get() = LightAppColors.stateUnselected

// Texto y bordes
// Antiguo color de texto marrón; equivale al texto principal.
@Deprecated("Usar AppTheme.colors.textPrimary", ReplaceWith("AppTheme.colors.textPrimary", IMPORT))
val TextoMarron get() = LightAppColors.textPrimary

// Antiguo color de borde marrón; equivale al borde del sistema.
@Deprecated("Usar AppTheme.colors.border", ReplaceWith("AppTheme.colors.border", IMPORT))
val BordeMarron get() = LightAppColors.border

// Botones y chips
// Antiguo color de botón deshabilitado; equivale al estado deshabilitado.
@Deprecated("Usar AppTheme.colors.stateDisabled", ReplaceWith("AppTheme.colors.stateDisabled", IMPORT))
val BotonDeshabilitado get() = LightAppColors.stateDisabled

// Antiguo color de botón habilitado; equivale a la acción principal.
@Deprecated("Usar AppTheme.colors.actionPrimary", ReplaceWith("AppTheme.colors.actionPrimary", IMPORT))
val BotonHabilitado get() = LightAppColors.actionPrimary

// Antiguo color de chip seleccionado; equivale al rol de chip seleccionado.
@Deprecated("Usar AppTheme.colors.chipSelected", ReplaceWith("AppTheme.colors.chipSelected", IMPORT))
val ChipSeleccionado get() = LightAppColors.chipSelected

// Antiguo botón naranja; equivale a la acción secundaria.
@Deprecated("Usar AppTheme.colors.actionSecondary", ReplaceWith("AppTheme.colors.actionSecondary", IMPORT))
val NaranjaBoton get() = LightAppColors.actionSecondary

// Error
// Antiguo color de error; equivale al estado de error.
@Deprecated("Usar AppTheme.colors.stateError", ReplaceWith("AppTheme.colors.stateError", IMPORT))
val ColorError get() = LightAppColors.stateError
