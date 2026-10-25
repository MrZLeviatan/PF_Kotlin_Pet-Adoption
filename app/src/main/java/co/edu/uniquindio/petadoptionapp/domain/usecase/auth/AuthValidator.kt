package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

/**
Documenta el objeto que contiene las reglas de formato sin red ni dependencias de Android.
Define un objeto singleton que agrupa las funciones de validación de autenticación.
 */
object AuthValidator {
    // Define una expresión regular privada para validar el formato estándar de un correo electrónico.
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Define una función pública que recibe un texto, elimina los espacios en blanco y verifica si cumple con el patrón.
    fun isEmailValid(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    // Define una función pública que recibe un código A2F, verifica si tiene exactamente 6 dígitos.
    fun isCodeValid(code: String): Boolean =
        code.length == 6 && code.all { it.isDigit() }


    // Define una función pública que recibe un texto, verifica el mínimo 8 caracteres, un número y una mayúscula.
    fun isPasswordValid(password: String): Boolean =
        password.length >= 8 &&
                password.any { it.isDigit() } &&
                password.any { it.isUpperCase() }

    // Define una función pública que recibe un texto, verifica el mínimo de tamaño de 3.
    fun isUserNameValid(userName: String): Boolean = userName.trim().length >= 3

    // Solo dígitos, entre 7 y 15.
    fun isPhoneValid(phone: String): Boolean =
        phone.trim().let { p -> p.length in 7..15 && p.all { it.isDigit() } }
}