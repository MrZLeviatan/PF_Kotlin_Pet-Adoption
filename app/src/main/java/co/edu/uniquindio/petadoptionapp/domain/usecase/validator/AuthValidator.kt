package co.edu.uniquindio.petadoptionapp.domain.usecase.validator

import co.edu.uniquindio.petadoptionapp.domain.model.auth.TwoFactorChallenge

/**
Validador del dominio que contiene las reglas de formato de autenticación, sin red ni dependencias de Android.
Define un objeto singleton que agrupa las funciones de validación de credenciales y del código A2F.
Lo usan LoginUseCase, RegisterUseCase, ChangePassword, SendPasswordReset y VerifyTwoFactorCode
para rechazar datos mal escritos antes de llamar a Firebase.
**/
// Objeto único (singleton) con las reglas de formato de autenticación.
object AuthValidator {

    // Define el tamaño mínimo que debe tener una contraseña.
    const val MIN_PASSWORD_LENGTH = 8

    // Define una expresión regular privada para validar el formato estándar de un correo electrónico.
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Define una función pública que recibe un texto, elimina los espacios en blanco y verifica si cumple con el patrón.
    fun isEmailValid(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    // Define una función pública que recibe un código A2F y verifica que tenga la longitud definida en TwoFactorChallenge y solo dígitos.
    fun isCodeValid(code: String): Boolean =
        // Exige la longitud exacta y que todos los caracteres sean dígitos.
        code.length == TwoFactorChallenge.CODE_LENGTH && code.all { it.isDigit() }

    // Define una función pública que recibe un texto y verifica el mínimo de caracteres, un número y una mayúscula.
    fun isPasswordValid(password: String): Boolean =
        // Exige longitud mínima, al menos un dígito y al menos una mayúscula.
        password.length >= MIN_PASSWORD_LENGTH &&
                password.any { it.isDigit() } &&
                password.any { it.isUpperCase() }
}
