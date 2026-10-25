package co.edu.uniquindio.petadoptionapp.domain.error

/**
 Errores del dominio de perfil de usuario: datos personales,
 duplicados y existencia del perfil.
 La UI traduce cada uno a un texto amigable usando strings.xml.
 */
sealed class UserException : Exception() {

    // Errores de formato

    // El nombre real está vacío o es demasiado corto.
    object InvalidName : UserException()

    // El alias contiene caracteres prohibidos o no cumple las reglas de formato.
    object InvalidUserName : UserException()

    // El teléfono tiene un formato incorrecto.
    object InvalidPhone : UserException()

    // No se eligió un punto válido en el mapa.
    object InvalidLocation : UserException()


    // Errores de duplicidad

    // El alias ya está ocupado por otra persona.
    object UserNameTaken : UserException()

    // El teléfono ya está asociado a otra cuenta.
    object PhoneTaken : UserException()


    // Errores de existencia

    // La cuenta existe en Auth, pero no se encontró su perfil en la base de datos.
    object ProfileNotFound : UserException()

    // No existe el usuario buscado (por ejemplo, al abrir un perfil ajeno).
    object UserNotFound : UserException()
}