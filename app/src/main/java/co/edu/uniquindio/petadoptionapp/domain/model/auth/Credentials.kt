package co.edu.uniquindio.petadoptionapp.domain.model.auth

/**
 Par email + contraseña que el usuario escribe en el formulario de
 registro o login.

 Esta clase vive SOLO EN MEMORIA: se usa para llamar a Firebase Auth y
 luego se descarta. Nunca debe guardarse en Firestore, en SharedPreferences ni en ningún otro almacenamiento.
*/
data class Credentials(
    val email: String = "",
    val password: String = ""
) {

    /**
     Sobrescribe el `toString()` generado por `data class` para ocultar la
     contraseña. Sin esto, un `println(credentials)` o un `Log.d(...)`
     mostraría la contraseña real en Logcat.
     */
    override fun toString(): String = "Credentials(email=$email, password=****)"
}