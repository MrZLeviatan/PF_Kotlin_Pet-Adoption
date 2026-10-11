package co.edu.uniquindio.petadoptionapp.features.registro_usuario

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegistroViewModel : ViewModel() {

    // Estado privado modificable por el ViewModel.
    // La interfaz solamente recibirá una versión de lectura.
    private val _uiState = MutableStateFlow(RegistroUiState())

    val uiState = _uiState.asStateFlow()

    // --------------------------------------------------
    // FASE 1: ACTUALIZAR DATOS PERSONALES
    // --------------------------------------------------

    fun actualizarNombre(nombre: String)
    {
        _uiState.value = _uiState.value.copy(
            nombreCompleto = nombre,
            errorNombre = null,
            mensajeError = null
        )
    }

    fun actualizarCorreo(correo: String)
    {
        _uiState.value = _uiState.value.copy(
            correoElectronico = correo,
            errorCorreo = null,
            mensajeError = null
        )
    }

    fun actualizarTelefono(telefono: String)
    {
        _uiState.value = _uiState.value.copy(
            telefono = telefono,
            errorTelefono = null,
            mensajeError = null
        )
    }

    fun actualizarContrasena(contrasena: String)
    {
        _uiState.value = _uiState.value.copy(
            contrasena = contrasena,
            errorContrasena = null,
            errorConfirmarContrasena = null,
            mensajeError = null
        )
    }

    fun actualizarConfirmacionContrasena(confirmacion: String)
    {
        _uiState.value = _uiState.value.copy(
            confirmarContrasena = confirmacion,
            errorConfirmarContrasena = null,
            mensajeError = null
        )
    }

    fun alternarVisibilidadContrasena()
    {
        val estado = _uiState.value

        _uiState.value = estado.copy(
            mostrarContrasena = !estado.mostrarContrasena
        )
    }

    fun alternarVisibilidadConfirmacion()
    {
        val estado = _uiState.value

        _uiState.value = estado.copy(
            mostrarConfirmarContrasena =
                !estado.mostrarConfirmarContrasena
        )
    }

    // --------------------------------------------------
    // VALIDACIÓN DE LA FASE 1
    // --------------------------------------------------

    private fun validarDatosPersonales(): Boolean
    {
        val estado = _uiState.value

        val errorNombre = when {
            estado.nombreCompleto.isBlank() ->
                "Ingresa tu nombre completo"

            estado.nombreCompleto.trim().split(Regex("\\s+")).size < 2 ->
                "Ingresa tu nombre y apellido"

            else -> null
        }

        val correo = estado.correoElectronico.trim()

        val errorCorreo = when
        {
            correo.isBlank() ->
                "Ingresa tu correo electrónico"

            !Regex(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            ).matches(correo) ->
                "Ingresa un correo electrónico válido"

            else -> null
        }

        val digitosTelefono = estado.telefono.filter{ it.isDigit() }

        val errorTelefono = when
        {
            estado.telefono.isBlank() ->
                "Ingresa tu teléfono"

            digitosTelefono.length !in 10..15 ->
                "Ingresa un teléfono válido"

            else -> null
        }

        val contrasena = estado.contrasena

        val errorContrasena = when
        {
            contrasena.isBlank() ->
                "Ingresa una contraseña"

            contrasena.length < 8 ->
                "Debe tener al menos 8 caracteres"

            !contrasena.any { it.isUpperCase() } ->
                "Incluye al menos una letra mayúscula"

            !contrasena.any { it.isLowerCase() } ->
                "Incluye al menos una letra minúscula"

            !contrasena.any { it.isDigit() } ->
                "Incluye al menos un número"

            else -> null
        }

        val errorConfirmacion = when
        {
            estado.confirmarContrasena.isBlank() ->
                "Confirma tu contraseña"

            estado.confirmarContrasena != contrasena ->
                "Las contraseñas no coinciden"

            else -> null
        }

        _uiState.value = estado.copy(
            errorNombre = errorNombre,
            errorCorreo = errorCorreo,
            errorTelefono = errorTelefono,
            errorContrasena = errorContrasena,
            errorConfirmarContrasena = errorConfirmacion
        )

        return listOf(
            errorNombre,
            errorCorreo,
            errorTelefono,
            errorContrasena,
            errorConfirmacion
        ).all { it == null }
    }

    fun avanzarAFaseDos()
    {
        if (validarDatosPersonales()) {
            _uiState.value = _uiState.value.copy(
                pasoActual = 2,
                mensajeError = null
            )
        }
    }

    // --------------------------------------------------
    // INDICADOR DE SEGURIDAD DE LA CONTRASEÑA
    // --------------------------------------------------

    val nivelSeguridadContrasena: Int
        get()
        {
            val contrasena = _uiState.value.contrasena

            if (contrasena.isEmpty()) return 0

            var puntos = 0

            if (contrasena.length >= 8) puntos++
            if (contrasena.any { it.isUpperCase() }) puntos++
            if (contrasena.any { it.isLowerCase() }) puntos++
            if (contrasena.any { it.isDigit() }) puntos++
            if (contrasena.any { !it.isLetterOrDigit() }) puntos++

            return puntos
        }

    // --------------------------------------------------
    // FASE 2: UBICACIÓN
    // --------------------------------------------------

    fun actualizarCiudad(ciudad: String)
    {
        _uiState.value = _uiState.value.copy(
            ciudad = ciudad,
            errorCiudad = null,
            mensajeError = null
        )
    }

    fun actualizarDireccion(direccion: String)
    {
        _uiState.value = _uiState.value.copy(
            direccion = direccion,
            errorDireccion = null,
            mensajeError = null
        )
    }

    fun actualizarAceptacionTerminos(acepta: Boolean)
    {
        _uiState.value = _uiState.value.copy(
            aceptaTerminos = acepta,
            errorTerminos = null,
            mensajeError = null
        )
    }

    fun volverAFaseUno()
    {
        _uiState.value = _uiState.value.copy(
            pasoActual = 1,
            mensajeError = null
        )
    }

    // --------------------------------------------------
    // VALIDACIÓN DE LA FASE 2
    // --------------------------------------------------

    private fun validarUbicacion(): Boolean
    {
        val estado = _uiState.value

        val errorCiudad = if (estado.ciudad.isBlank())
        {
            "Ingresa tu ciudad"
        } else {
            null
        }

        val errorDireccion = if (estado.direccion.isBlank())
        {
            "Ingresa tu dirección"
        } else {
            null
        }

        val errorTerminos = if (!estado.aceptaTerminos)
        {
            "Debes aceptar los términos y condiciones"
        } else {
            null
        }

        _uiState.value = estado.copy(
            errorCiudad = errorCiudad,
            errorDireccion = errorDireccion,
            errorTerminos = errorTerminos
        )

        return errorCiudad == null &&
                errorDireccion == null &&
                errorTerminos == null
    }

    // --------------------------------------------------
    // CREACIÓN DE CUENTA: SIMULACIÓN DE INTERFAZ
    // --------------------------------------------------

    fun crearCuenta()
    {
        if (validarUbicacion())
        {
            _uiState.value = _uiState.value.copy(
                mostrarDialogoExito = true,
                mensajeError = null
            )
        }
    }

    fun cerrarDialogoExito()
    {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoExito = false
        )
    }

    // Permite limpiar un mensaje general si la pantalla lo necesita.
    fun limpiarMensajeError()
    {
        _uiState.value = _uiState.value.copy(
            mensajeError = null
        )
    }
}
