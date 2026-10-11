package co.edu.uniquindio.petadoptionapp.features.registro_usuario

/**
 * Representa el estado actual del formulario de registro.
 *
 * Este objeto almacena los datos que el usuario escribe
 * y los estados visuales de las dos fases del registro.
 */
data class RegistroUiState(

    // Fase 1: datos personales
    val nombreCompleto: String = "",
    val correoElectronico: String = "",
    val telefono: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",

    // Visibilidad de las contraseñas
    val mostrarContrasena: Boolean = false,
    val mostrarConfirmarContrasena: Boolean = false,

    // Fase 2: ubicación
    val ciudad: String = "",
    val direccion: String = "",
    val aceptaTerminos: Boolean = false,

    // Control del formulario
    val pasoActual: Int = 1,
    val mostrarDialogoExito: Boolean = false,
    val cargando: Boolean = false,

    // Mensajes de validación
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorContrasena: String? = null,
    val errorConfirmarContrasena: String? = null,
    val errorCiudad: String? = null,
    val errorDireccion: String? = null,
    val errorTerminos: String? = null,

    // Mensaje general para informar errores del registro
    val mensajeError: String? = null
)
