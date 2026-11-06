package co.edu.uniquindio.petadoptionapp.core.util

import java.text.Normalizer
import kotlin.random.Random

/**
Generador de alias (nombre de usuario) usado al registrar una cuenta: toma el primer nombre de la persona, lo
normaliza (minúsculas, sin tildes ni caracteres especiales, máximo 12 letras) y le agrega un sufijo aleatorio de
4 caracteres, por ejemplo "maria_x7k2". El generador aleatorio se puede inyectar para facilitar las pruebas.
**/

/**
Genera un alias a partir del nombre completo: primer nombre + "_" + sufijo aleatorio.
**/
object UserNameGenerator {

    // Longitud máxima de la parte del nombre.
    private const val MAX_BASE_LENGTH = 12
    // Cantidad de caracteres del sufijo aleatorio.
    private const val SUFFIX_LENGTH = 4
    // Base a usar si el nombre no tiene letras válidas.
    private const val FALLBACK_BASE = "user"
    // Caracteres permitidos en el sufijo.
    private const val SUFFIX_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789"
    // Expresión que reconoce las marcas diacríticas (tildes) separadas por la normalización NFD.
    private val ACCENTS = Regex("\\p{Mn}+")

    // Genera el alias; recibe el nombre completo y, opcionalmente, el generador aleatorio.
    fun generate(fullName: String, random: Random = Random.Default): String {
        // Toma la primera palabra del nombre (separando por espacios); vacío si no hay.
        val firstName = fullName.trim().split(Regex("\\s+")).firstOrNull().orEmpty()

        // NFD separa la letra de su tilde ("á" → "a" + "´"); luego se quitan las tildes
        // y se descarta todo lo que no sea a-z. Si el nombre no tiene letras, usa "user".
        val base = Normalizer.normalize(firstName.lowercase(), Normalizer.Form.NFD)
            .replace(ACCENTS, "")
            .filter { it in 'a'..'z' }
            .take(MAX_BASE_LENGTH)
            .ifEmpty { FALLBACK_BASE }

        // Construye el sufijo eligiendo caracteres al azar.
        val suffix = buildString {
            repeat(SUFFIX_LENGTH) { append(SUFFIX_CHARS[random.nextInt(SUFFIX_CHARS.length)]) }
        }
        // Une la base y el sufijo con un guion bajo.
        return "${base}_$suffix"
    }
}
