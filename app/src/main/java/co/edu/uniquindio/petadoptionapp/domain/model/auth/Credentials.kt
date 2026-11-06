package co.edu.uniquindio.petadoptionapp.domain.model.auth

/**
Modelo de dominio: par email + contraseña que el usuario escribe en el formulario de
registro o login.

Esta clase vive SOLO EN MEMORIA: se usa para llamar a Firebase Auth y
luego se descarta. Nunca debe guardarse en Firestore, en SharedPreferences ni en ningún otro almacenamiento.
La usan LoginUseCase, RegisterUseCase y AuthRepository.
**/
// Declara las credenciales como clase de datos.
data class Credentials(
    // Correo electrónico escrito por la persona.
    val email: String = "",
    // Contraseña en claro; solo se usa para autenticarse y nunca se persiste.
    val password: String = ""
) {

    /**
     Sobrescribe el `toString()` generado por `data class` para ocultar la
     contraseña. Sin esto, un `println(credentials)` o un `Log.d(...)`
     mostraría la contraseña real en Logcat.
     */
    // Devuelve el texto con el correo visible y la contraseña enmascarada.
    override fun toString(): String = "Credentials(email=$email, password=****)"
}
