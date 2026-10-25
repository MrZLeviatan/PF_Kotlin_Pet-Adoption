package co.edu.uniquindio.petadoptionapp.domain.error

/**
 Errores del dominio de autenticación: credenciales, contraseña, sesión y A2F.
 La UI traduce cada uno a un texto amigable usando strings.xml.
 */
sealed class AuthException : Exception() {

    // Errores de formato

    // El correo no cumple el formato válido (ej: falta el '@').
    object InvalidEmail : AuthException()

    // La contraseña no cumple los criterios mínimos de seguridad.
    object WeakPassword : AuthException()


    // Errores de credenciales y contraseña

    // El correo ya está registrado en otra cuenta (lo rechaza Firebase Auth).
    object EmailAlreadyInUse : AuthException()

    // La contraseña ingresada no coincide con la actual.
    object WrongPassword : AuthException()

    // La nueva contraseña es exactamente igual a la actual.
    object SamePassword : AuthException()


    // Errores de inicio de sesión

    // Un moderador intentó entrar, pero su cuenta está inactiva o suspendida.
    object InactiveModerator : AuthException()

    // Ocurre cuando una acción exclusiva de moderadores la intenta otra persona o un moderador inactivo.
    object NotModerator : AuthException()


    //  Errores de A2F

    // El código no tiene el formato esperado (6 dígitos).
    object InvalidCode : AuthException()

    // Los 6 dígitos no coinciden con el código enviado.
    object WrongCode : AuthException()

    // El código ya expiró.
    object CodeExpired : AuthException()

    // Se agotaron los intentos de verificación.
    object TooManyAttempts : AuthException()

    // Ocurre cuando se intenta una acción que exige sesión y no hay ninguna abierta.
    object NotAuthenticated : AuthException()


}