package co.edu.uniquindio.petadoptionapp.core.theme

import androidx.compose.ui.graphics.Color

/**
 Capa 1: Paleta primitiva.
 Única fuente de verdad de los valores hexadecimales de la marca. Los nombres
 describen el color (`Sand200`) y nunca su uso (`FondoChip`), de modo que un mismo
 tono pueda cumplir varios roles y un cambio de identidad visual se haga en un
 solo punto.
 Esta paleta no se consume desde la UI: los roles se expresan en [AppColors],
 que asigna cada uno de estos tonos a un papel semántico para el tema claro
 (LightAppColors) y para el tema oscuro (DarkAppColors).
**/

// `internal`: visible solo dentro del módulo `app`. Evita que otros módulos futuros dependan de colores crudos y se salten el sistema de roles.
internal object PetPalette {

    // Marrones
    // Marrón cálido principal de la marca; se usa como color de marca y como borde en modo oscuro.
    val WarmBrown = Color(0xFFD4A373)
    // Marrón muy oscuro usado como color de texto principal en el tema claro por su alto contraste sobre cremas.
    val DarkBrown = Color(0xFF4A2C0F)
    // Marrón medio reservado para los bordes de campos y tarjetas en el tema claro.
    val BorderBrown = Color(0xFFC98F55)
    // Tono canela que identifica a los chips seleccionados en ambos temas.
    val Tan = Color(0xFFD4A06A)

    // Marrón intenso para hipervínculos en tema claro, distinguible del texto normal sin romper la paleta.
    val LinkBrown = Color(0xFFA06931)

    // Cremas y arenas
    // Crema muy clara usada como fondo general de las pantallas en tema claro.
    val Cream50 = Color(0xFFFFF6E0)
    // Crema algo más intensa para superficies elevadas (tarjetas destacadas, encabezados).
    val Cream100 = Color(0xFFFBEBC4)
    // Arena suave para superficies de aviso o información.
    val Sand100 = Color(0xFFF5E6D3)
    // Arena más saturada para etiquetas y chips no seleccionados.
    val Sand200 = Color(0xFFF1D9B5)

    // Acentos
    // Amarillo de acción principal: botones primarios y elementos seleccionados.
    val Yellow = Color(0xFFE9C46A)
    // Naranja de acción secundaria: botones alternativos.
    val Orange = Color(0xFFC98A45)

    // Neutros
    // Blanco puro para superficies y texto sobre fondos oscuros.
    val White = Color(0xFFFFFFFF)
    // Casi negro para íconos o elementos no seleccionados en tema claro.
    val Ink = Color(0xFF1F1F1F)
    // Gris piedra cálido para elementos deshabilitados.
    val Stone = Color(0xFF9E9382)

    // Sistema
    // Rojo estándar para errores en tema claro.
    val Red = Color(0xFFD32F2F)

    // Modo oscuro
    // Marrón casi negro usado como fondo base del tema oscuro.
    val Night900 = Color(0xFF1C1612)
    // Un nivel más claro que el fondo; superficie de tarjetas en modo oscuro.
    val Night800 = Color(0xFF2A211A)
    // Superficie elevada o de aviso en modo oscuro.
    val Night700 = Color(0xFF3A2E24)
    // Superficie más clara del modo oscuro, usada para etiquetas y chips no seleccionados.
    val Night600 = Color(0xFF4A3B2E)
    // Color claro para el texto que se dibuja sobre las superficies nocturnas.
    val OnNight = Color(0xFFF5E6D3)
    // Rojo suavizado para errores en modo oscuro, evita un contraste agresivo sobre fondos oscuros.
    val RedLight = Color(0xFFEF9A9A)
}
