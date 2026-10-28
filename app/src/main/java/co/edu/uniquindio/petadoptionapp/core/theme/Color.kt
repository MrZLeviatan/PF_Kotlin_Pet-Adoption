package co.edu.uniquindio.petadoptionapp.core.theme

import androidx.compose.ui.graphics.Color

/**
 Capa 1: Paleta primitiva.
 Única fuente de verdad de los valores hexadecimales de la marca. Los nombres
 describen el color (`Sand200`) y nunca su uso (`FondoChip`), de modo que un mismo
 tono pueda cumplir varios roles y un cambio de identidad visual se haga en un
 solo punto.
 Esta paleta no se consume desde la UI: los roles se expresan en [AppColors].
 */

// `internal`: visible solo dentro del módulo `app`. Evita que otros módulos futuros dependan de colores crudos y se salten el sistema de roles.
internal object PetPalette {

    // Marrones
    val WarmBrown = Color(0xFFD4A373)
    val DarkBrown = Color(0xFF4A2C0F)
    val BorderBrown = Color(0xFFC98F55)
    val Tan = Color(0xFFD4A06A)

    val LinkBrown = Color(0xFFA06931)

    // Cremas y arenas
    val Cream50 = Color(0xFFFFF6E0)
    val Cream100 = Color(0xFFFBEBC4)
    val Sand100 = Color(0xFFF5E6D3)
    val Sand200 = Color(0xFFF1D9B5)

    // Acentos
    val Yellow = Color(0xFFE9C46A)
    val Orange = Color(0xFFC98A45)

    // Neutros
    val White = Color(0xFFFFFFFF)
    val Ink = Color(0xFF1F1F1F)
    val Stone = Color(0xFF9E9382)

    // Sistema
    val Red = Color(0xFFD32F2F)

    // Modo oscuro
    val Night900 = Color(0xFF1C1612)
    val Night800 = Color(0xFF2A211A)
    val Night700 = Color(0xFF3A2E24)
    val Night600 = Color(0xFF4A3B2E)
    val OnNight = Color(0xFFF5E6D3)
    val RedLight = Color(0xFFEF9A9A)
}