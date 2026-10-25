package co.edu.uniquindio.petadoptionapp.domain.usecase.validator

/**
Documenta el objeto que contiene las reglas de formato de los datos de perfil, sin red ni dependencias de Android.
Define un objeto singleton que agrupa las funciones de validación de nombre, alias y teléfono.
 */
object UserValidator {

    // Define el tamaño mínimo que debe tener el nombre real.
    const val MIN_NAME_LENGTH = 2

    // Define el tamaño mínimo que debe tener el alias.
    const val MIN_USERNAME_LENGTH = 3

    // Define el tamaño mínimo que debe tener un teléfono.
    const val MIN_PHONE_LENGTH = 7

    // Define el tamaño máximo que puede tener un teléfono.
    const val MAX_PHONE_LENGTH = 15

    // Define una función pública que recibe un texto, elimina los espacios en blanco y verifica el mínimo de tamaño.
    fun isNameValid(name: String): Boolean = name.trim().length >= MIN_NAME_LENGTH

    // Define una función pública que recibe un texto, elimina los espacios en blanco y verifica el mínimo de tamaño.
    fun isUserNameValid(userName: String): Boolean = userName.trim().length >= MIN_USERNAME_LENGTH

    // Define una función pública que recibe un texto y verifica que contenga solo dígitos, dentro del rango de tamaño permitido.
    fun isPhoneValid(phone: String): Boolean =
        phone.trim().let { p -> p.length in MIN_PHONE_LENGTH..MAX_PHONE_LENGTH && p.all { it.isDigit() } }
}