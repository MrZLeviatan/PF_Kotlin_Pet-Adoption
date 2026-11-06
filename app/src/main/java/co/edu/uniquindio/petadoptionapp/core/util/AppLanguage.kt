package co.edu.uniquindio.petadoptionapp.core.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
Utilidades para el idioma de la app, usadas por el selector de idioma (por ejemplo, en Ajustes o Perfil).
Se apoyan en la API de idiomas por aplicación de AppCompat ([AppCompatDelegate.setApplicationLocales]): al cambiar
el idioma, AppCompat lo guarda automáticamente y recrea las actividades para que todos los textos se lean de la
carpeta de recursos correspondiente (res/values, values-en o values-fr).
**/

/**
Idiomas de la app. El texto sale de res/values (español), values-en y values-fr.
La elección se guarda sola (AppCompat) y la app se vuelve a dibujar en el idioma nuevo.
[tag] es el código de idioma BCP 47 que entiende Android.
**/
enum class AppLanguage(val tag: String) { SPANISH("es"), ENGLISH("en"), FRENCH("fr") }

/**
Idioma actual: el elegido en la app o, si nunca se eligió, el del teléfono (español si no es uno de los tres).
**/
fun currentAppLanguage(): AppLanguage {
    // Toma el idioma elegido en la app; si la lista está vacía, usa el idioma del sistema.
    val tag = AppCompatDelegate.getApplicationLocales()[0]?.language ?: Locale.getDefault().language
    // Busca el idioma soportado con ese código; si no lo hay, se usa español por defecto.
    return AppLanguage.entries.firstOrNull { it.tag == tag } ?: AppLanguage.SPANISH
}

// Cambia el idioma de la app; AppCompat lo persiste y recrea las pantallas con los nuevos textos.
fun setAppLanguage(language: AppLanguage) {
    // Aplica la lista de idiomas construida a partir del código del idioma elegido.
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
}
