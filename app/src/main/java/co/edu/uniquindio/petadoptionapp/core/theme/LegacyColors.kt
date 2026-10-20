@file:Suppress("unused")

package co.edu.uniquindio.petadoptionapp.core.theme

/**
 * Alias temporales de la nomenclatura anterior.
 * Eliminar este archivo cuando no queden warnings de deprecación.
 * Devuelven siempre valores del tema claro (no reaccionan a modo oscuro).
 */

private const val IMPORT = "co.edu.uniquindio.petadoptionapp.core.theme.AppTheme"

// Fondos
@Deprecated("Usar AppTheme.colors.brand", ReplaceWith("AppTheme.colors.brand", IMPORT))
val FondoMarron get() = LightAppColors.brand

@Deprecated("Usar AppTheme.colors.surfaceElevated", ReplaceWith("AppTheme.colors.surfaceElevated", IMPORT))
val FondoCrema get() = LightAppColors.surfaceElevated

@Deprecated("Usar AppTheme.colors.background", ReplaceWith("AppTheme.colors.background", IMPORT))
val FondoClaro get() = LightAppColors.background

@Deprecated("Usar AppTheme.colors.surfaceTag", ReplaceWith("AppTheme.colors.surfaceTag", IMPORT))
val FondoEtiquetasClaro get() = LightAppColors.surfaceTag

@Deprecated("Usar AppTheme.colors.surface", ReplaceWith("AppTheme.colors.surface", IMPORT))
val FondoTarjeta get() = LightAppColors.surface

@Deprecated("Usar AppTheme.colors.surfaceNotice", ReplaceWith("AppTheme.colors.surfaceNotice", IMPORT))
val FondoAviso get() = LightAppColors.surfaceNotice

@Deprecated("Usar AppTheme.colors.surfaceTag", ReplaceWith("AppTheme.colors.surfaceTag", IMPORT))
val FondoChip get() = LightAppColors.surfaceTag

// Items
@Deprecated("Usar AppTheme.colors.stateSelected", ReplaceWith("AppTheme.colors.stateSelected", IMPORT))
val ItemSeleccionado get() = LightAppColors.stateSelected

@Deprecated("Usar AppTheme.colors.stateSelected", ReplaceWith("AppTheme.colors.stateSelected", IMPORT))
val ItemCrearFeed get() = LightAppColors.stateSelected

@Deprecated("Usar AppTheme.colors.stateUnselected", ReplaceWith("AppTheme.colors.stateUnselected", IMPORT))
val ItemNoSeleccionado get() = LightAppColors.stateUnselected

// Texto y bordes
@Deprecated("Usar AppTheme.colors.textPrimary", ReplaceWith("AppTheme.colors.textPrimary", IMPORT))
val TextoMarron get() = LightAppColors.textPrimary

@Deprecated("Usar AppTheme.colors.border", ReplaceWith("AppTheme.colors.border", IMPORT))
val BordeMarron get() = LightAppColors.border

// Botones y chips
@Deprecated("Usar AppTheme.colors.stateDisabled", ReplaceWith("AppTheme.colors.stateDisabled", IMPORT))
val BotonDeshabilitado get() = LightAppColors.stateDisabled

@Deprecated("Usar AppTheme.colors.actionPrimary", ReplaceWith("AppTheme.colors.actionPrimary", IMPORT))
val BotonHabilitado get() = LightAppColors.actionPrimary

@Deprecated("Usar AppTheme.colors.chipSelected", ReplaceWith("AppTheme.colors.chipSelected", IMPORT))
val ChipSeleccionado get() = LightAppColors.chipSelected

@Deprecated("Usar AppTheme.colors.actionSecondary", ReplaceWith("AppTheme.colors.actionSecondary", IMPORT))
val NaranjaBoton get() = LightAppColors.actionSecondary

// Error
@Deprecated("Usar AppTheme.colors.stateError", ReplaceWith("AppTheme.colors.stateError", IMPORT))
val ColorError get() = LightAppColors.stateError