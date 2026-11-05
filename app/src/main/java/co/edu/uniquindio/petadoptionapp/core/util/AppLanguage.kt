package co.edu.uniquindio.petadoptionapp.core.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 Idiomas de la app. El texto sale de res/values (español), values-en y values-fr.
 La elección se guarda sola (AppCompat) y la app se vuelve a dibujar en el idioma nuevo.
 */
enum class AppLanguage(val tag: String) { SPANISH("es"), ENGLISH("en"), FRENCH("fr") }

/** Idioma actual: el elegido en la app o, si nunca se eligió, el del teléfono (español si no es uno de los tres). */
fun currentAppLanguage(): AppLanguage {
    val tag = AppCompatDelegate.getApplicationLocales()[0]?.language ?: Locale.getDefault().language
    return AppLanguage.entries.firstOrNull { it.tag == tag } ?: AppLanguage.SPANISH
}

fun setAppLanguage(language: AppLanguage) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
}
