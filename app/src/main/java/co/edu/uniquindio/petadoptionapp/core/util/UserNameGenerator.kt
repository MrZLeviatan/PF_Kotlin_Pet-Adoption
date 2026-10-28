package co.edu.uniquindio.petadoptionapp.core.util

import java.text.Normalizer
import kotlin.random.Random

/** Genera un alias a partir del nombre completo: primer nombre + "_" + sufijo aleatorio. */
object UserNameGenerator {

    private const val MAX_BASE_LENGTH = 12
    private const val SUFFIX_LENGTH = 4
    private const val FALLBACK_BASE = "user"
    private const val SUFFIX_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789"
    private val ACCENTS = Regex("\\p{Mn}+")

    fun generate(fullName: String, random: Random = Random.Default): String {
        val firstName = fullName.trim().split(Regex("\\s+")).firstOrNull().orEmpty()

        // NFD separa la letra de su tilde ("á" → "a" + "´"); luego se quitan las tildes
        // y se descarta todo lo que no sea a-z. Si el nombre no tiene letras, usa "user".
        val base = Normalizer.normalize(firstName.lowercase(), Normalizer.Form.NFD)
            .replace(ACCENTS, "")
            .filter { it in 'a'..'z' }
            .take(MAX_BASE_LENGTH)
            .ifEmpty { FALLBACK_BASE }

        val suffix = buildString {
            repeat(SUFFIX_LENGTH) { append(SUFFIX_CHARS[random.nextInt(SUFFIX_CHARS.length)]) }
        }
        return "${base}_$suffix"
    }
}