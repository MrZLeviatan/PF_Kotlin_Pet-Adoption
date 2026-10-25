package co.edu.uniquindio.petadoptionapp.domain.error

/**
 Errores del dominio relacionados con la autenticación y cuentas.
 La UI traduce cada uno a un texto amigable usando strings.xml.
 */
sealed class AuthException : Exception() {

    // Errores de formato

    // Ocurre cuando el correo ingresado no cumple con el formato válido (ej: falta el '@').
    object InvalidEmail : AuthException()

    // Ocurre cuando la contraseña es muy corta o no cumple con los criterios mínimos de seguridad.
    object WeakPassword : AuthException()

    // Ocurre cuando el nombre de usuario contiene caracteres prohibidos o no cumple las reglas de formato.
    object InvalidUserName : AuthException()

    // Ocurre cuando el número de teléfono ingresado tiene un formato incorrecto.
    object InvalidPhone : AuthException()

    // Ocurre cuando no se eligió un punto válido de ubicación
    object InvalidLocation : AuthException()


    // Errores de duplicidad

    // Ocurre durante el registro cuando el nombre de usuario ya está ocupado por otra persona en el sistema.
    object UserNameTaken : AuthException()

    // Ocurre cuando el número de teléfono ya se encuentra registrado y asociado a otra cuenta.
    object PhoneTaken : AuthException()

    // Ocurre cuando el usuario intenta cambiar su contraseña y la nueva clave es exactamente igual a la actual.
    object SamePassword : AuthException()

    // Email ya en uso por otra cuenta (Error propio de Firebase)
    object EmailAlreadyInUse : AuthException()

    // Ocurre cuando la contraseña ingresada no coindice.
    object WrongPassword : AuthException()




    // Errores de Inicio de sesión

    // Ocurre cuando se autentica en el sistema de Auth, pero no se encuentra su documento o perfil en la base de datos.
    object ProfileNotFound : AuthException()

    // Ocurre cuando un moderador intenta iniciar sesión, pero su cuenta se encuentra inactiva o suspendida.
    object InactiveModerator : AuthException()


    // Errores de A2F

    // Ocurre cuando el código de verificación o de doble factor (A2F) es incorrecto o expiró.
    object InvalidCode : AuthException()

    // Ocurre cuando los 6 dígitos del código no coinciden.
    object WrongCode : AuthException()

    // Ocurre cuando el código ya expiro.
    object CodeExpired : AuthException()

    // Ocurre cuando se agotan los intentos de autentificación.
    object TooManyAttempts : AuthException()
}