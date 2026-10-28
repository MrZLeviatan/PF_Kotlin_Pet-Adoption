package co.edu.uniquindio.petadoptionapp.domain.model.auth

/**
 Verificación en dos pasos (A2F) de una cuenta.

 Representa UN código temporal enviado al correo de la persona después de
 que su email y contraseña fueron validados. Guarda lo mínimo para decidir
 si el código que escribe la persona es válido.

 Reglas de seguridad que este modelo hace posibles:
 - Se guarda el HASH del código, nunca el código en claro.
 - Expira a los [TTL_MILLIS] (5 minutos).
 - Permite máximo [MAX_ATTEMPTS] intentos fallidos.
 - Es de un solo uso ([consumed]).

 Se guarda en su propia colección (`twoFactorChallenges`), con un documento
 por usuario cuyo id es el mismo `Account.id`. NO va dentro de [User]
 porque es un dato temporal y sensible que el cliente nunca debe leer.
 */
data class TwoFactorChallenge(
    val userId: String = "",
    // Hash del código de 6 dígitos. Nunca el código real.
    val codeHash: String = "",
    // Momento exacto de expiración, en milisegundos (epoch).
    val expiresAt: Long = 0L,
    // Intentos fallidos acumulados.
    val attempts: Int = 0,
    // Cantidad máxima de intentos permitidos.
    val maxAttempts: Int = MAX_ATTEMPTS,
    //`true` cuando el código ya se usó con éxito (un solo uso).
    val consumed: Boolean = false,
    // Momento en que se generó el código, en milisegundos (epoch).
    val createdAt: Long = System.currentTimeMillis()
) {

    // ¿Ya pasó el tiempo de vida del código?
    fun hasExpired(now: Long = System.currentTimeMillis()): Boolean =
        now >= expiresAt

    // ¿Se agotaron los intentos? Si es así, el reto queda bloqueado.
    fun hasReachedMaxAttempts(): Boolean =
        attempts >= maxAttempts

    /**
     Resumen para decidir si todavía vale la pena comparar el código:
     no debe estar vencido, bloqueado ni usado.
     */
    fun canBeUsed(now: Long = System.currentTimeMillis()): Boolean =
        !hasExpired(now) && !hasReachedMaxAttempts() && !consumed

    /**
     Objeto estático: contiene constantes globales compartidas por toda la clase sin necesidad de instanciarla.
     */
    companion object {
        // Cantidad de dígitos del código que recibe la persona.
        const val CODE_LENGTH = 6

        // Vida del código: 5 minutos, expresados en milisegundos.
        const val TTL_MILLIS = 5 * 60 * 1000L

        // Intentos fallidos permitidos.
        const val MAX_ATTEMPTS = 5
    }
}