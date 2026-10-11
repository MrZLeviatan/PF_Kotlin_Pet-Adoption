package co.edu.uniquindio.petadoptionapp.features.registro_usuario

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
//Importacion de los temas a usar
import co.edu.uniquindio.petadoptionapp.core.theme.CafeLeche
import co.edu.uniquindio.petadoptionapp.core.theme.CafeOscuro
import co.edu.uniquindio.petadoptionapp.core.theme.Cream
import co.edu.uniquindio.petadoptionapp.core.theme.Gris
import co.edu.uniquindio.petadoptionapp.core.theme.NaranjaCalido
import co.edu.uniquindio.petadoptionapp.core.theme.RojoError
import co.edu.uniquindio.petadoptionapp.core.theme.Blanco

private val BordeCampo = Color(0xFFDCC8AA)
private val FondoMapa = Color(0xFFF5F0E2)
private val LineaMapa = Color(0xFFD8C9A8)


// =====================================================
// PANTALLA PRINCIPAL DE REGISTRO
// =====================================================

@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel,
    onIniciarSesion: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Cream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Encabezado
            if (estado.pasoActual == 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.volverAFaseUno() }
                    ) {
                        Text(
                            text = "←",
                            fontSize = 26.sp,
                            color = CafeOscuro
                        )
                    }
                }
            }

            EncabezadoRegistro()

            Spacer(modifier = Modifier.height(18.dp))

            IndicadorPasos(pasoActual = estado.pasoActual)

            Spacer(modifier = Modifier.height(24.dp))

            if (estado.pasoActual == 1) {

                // FASE 1: DATOS PERSONALES
                FormularioDatosPersonales(
                    estado = estado,
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(30.dp))

                BotonPrincipal(
                    texto = "Siguiente",
                    habilitado = !estado.cargando,
                    alHacerClick = {
                        viewModel.avanzarAFaseDos()
                    }
                )

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Ya tienes una cuenta? ",
                        fontSize = 11.sp,
                        color = CafeOscuro
                    )

                    Text(
                        text = "Inicia Sesión",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeOscuro,
                        modifier = Modifier.clickable {
                            onIniciarSesion()
                        }
                    )
                }

            } else {

                // FASE 2: UBICACIÓN
                FormularioUbicacion(
                    estado = estado,
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(20.dp))

                BotonPrincipal(
                    texto = "Crear Cuenta",
                    habilitado = !estado.cargando,
                    alHacerClick = {
                        viewModel.crearCuenta()
                    }
                )

                if (estado.cargando) {
                    Spacer(modifier = Modifier.height(12.dp))

                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = CafeOscuro
                    )
                }
            }

            estado.mensajeError?.let { mensaje ->
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = mensaje,
                    color = RojoError,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Diálogo de confirmación
    if (estado.mostrarDialogoExito) {
        DialogoCuentaCreada(
            alAceptar = {
                viewModel.cerrarDialogoExito()
            }
        )
    }
}

// =====================================================
// ENCABEZADO: HUELLA Y TÍTULO
// =====================================================

@Composable
private fun EncabezadoRegistro() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CafeLeche),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🐾",
                fontSize = 19.sp
            )
        }

        Spacer(modifier = Modifier.width(9.dp))

        Text(
            text = "Crear Cuenta",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = CafeOscuro
        )
    }
}

// =====================================================
// INDICADOR DE FASES
// =====================================================

@Composable
private fun IndicadorPasos(pasoActual: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(27.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (pasoActual == 1) {
                            NaranjaCalido
                        } else {
                            CafeLeche
                        }
                    )
            )

            Spacer(modifier = Modifier.width(5.dp))

            Box(
                modifier = Modifier
                    .width(27.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (pasoActual == 2) {
                            NaranjaCalido
                        } else {
                            CafeLeche.copy(alpha = 0.65f)
                        }
                    )
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "PASO $pasoActual DE 2",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = CafeOscuro
        )
    }
}

// =====================================================
// FASE 1: DATOS PERSONALES
// =====================================================

@Composable
private fun FormularioDatosPersonales(
    estado: RegistroUiState,
    viewModel: RegistroViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        CampoRegistro(
            valor = estado.nombreCompleto,
            alCambiar = viewModel::actualizarNombre,
            etiqueta = "♙   Nombre Completo*",
            error = estado.errorNombre
        )

        CampoRegistro(
            valor = estado.correoElectronico,
            alCambiar = viewModel::actualizarCorreo,
            etiqueta = "✉   Correo Electrónico*",
            error = estado.errorCorreo
        )

        CampoRegistro(
            valor = estado.telefono,
            alCambiar = viewModel::actualizarTelefono,
            etiqueta = "☎   Teléfono*",
            error = estado.errorTelefono
        )

        CampoRegistro(
            valor = estado.contrasena,
            alCambiar = viewModel::actualizarContrasena,
            etiqueta = "♙   Contraseña*",
            esContrasena = true,
            mostrarContrasena = estado.mostrarContrasena,
            alternarVisibilidad = {
                viewModel.alternarVisibilidadContrasena()
            },
            error = estado.errorContrasena
        )

        // Indicador de seguridad
        if (estado.contrasena.isNotEmpty()) {
            IndicadorSeguridad(
                nivel = viewModel.nivelSeguridadContrasena
            )
        }

        CampoRegistro(
            valor = estado.confirmarContrasena,
            alCambiar = viewModel::actualizarConfirmacionContrasena,
            etiqueta = "♙   Confirmar Contraseña*",
            esContrasena = true,
            mostrarContrasena = estado.mostrarConfirmarContrasena,
            alternarVisibilidad = {
                viewModel.alternarVisibilidadConfirmacion()
            },
            error = estado.errorConfirmarContrasena
        )
    }
}

// =====================================================
// CAMPO REUTILIZABLE
// =====================================================


@Composable
private fun CampoRegistro(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    error: String? = null,
    esContrasena: Boolean = false,
    mostrarContrasena: Boolean = false,
    alternarVisibilidad: (() -> Unit)? = null
) {
    // Separamos el icono del texto de la etiqueta.
    val partes = etiqueta.split("   ", limit = 2)
    val icono = partes.getOrNull(0) ?: ""
    val textoEtiqueta = partes.getOrNull(1) ?: etiqueta

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiar,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            singleLine = true,

            // Icono separado, a la izquierda del campo.
            leadingIcon = {
                Text(
                    text = icono,
                    fontSize = 15.sp,
                    color = CafeLeche
                )
            },

            // Texto dentro del campo cuando está vacío.
            placeholder = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = textoEtiqueta.removeSuffix("*"),
                        fontSize = 12.sp,
                        color = Gris
                    )

                    if (textoEtiqueta.endsWith("*")) {
                        Text(
                            text = "*",
                            fontSize = 12.sp,
                            color = RojoError
                        )
                    }
                }
            },

            visualTransformation = if (
                esContrasena && !mostrarContrasena
            ) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },

            keyboardOptions = KeyboardOptions(
                keyboardType = when {
                    esContrasena -> KeyboardType.Password
                    textoEtiqueta.contains("Correo", ignoreCase = true) ->
                        KeyboardType.Email
                    textoEtiqueta.contains("Teléfono", ignoreCase = true) ->
                        KeyboardType.Phone
                    else -> KeyboardType.Text
                }
            ),

            trailingIcon = if (esContrasena) {
                {
                    Text(
                        text = if (mostrarContrasena) "◉" else "◎",
                        fontSize = 15.sp,
                        color = CafeLeche,
                        modifier = Modifier.clickable {
                            alternarVisibilidad?.invoke()
                        }
                    )
                }
            } else {
                null
            },

            shape = RoundedCornerShape(7.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CafeLeche,
                unfocusedBorderColor = BordeCampo,
                focusedContainerColor = Color(0xFFFFFBF5),
                unfocusedContainerColor = Color(0xFFFFFBF5),
                focusedTextColor = CafeOscuro,
                unfocusedTextColor = CafeOscuro,
                cursorColor = CafeOscuro,
                errorBorderColor = RojoError
            ),

            isError = error != null
        )

        if (error != null) {
            Text(
                text = error,
                color = RojoError,
                fontSize = 11.sp,
                modifier = Modifier.padding(
                    start = 4.dp,
                    top = 3.dp
                )
            )
        }
    }
}


// =====================================================
// INDICADOR DE SEGURIDAD DE CONTRASEÑA
// =====================================================

@Composable
private fun IndicadorSeguridad(nivel: Int) {
    val descripcion = when {
        nivel <= 2 -> "Seguridad: Baja"
        nivel <= 4 -> "Seguridad: Media"
        else -> "Seguridad: Alta"
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = descripcion,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = CafeOscuro
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { indice ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                nivel <= 2 && indice == 0 ->
                                    RojoError

                                nivel in 3..4 && indice < 2 ->
                                    NaranjaCalido

                                nivel >= 5 ->
                                    VerdeSeguridad

                                else ->
                                    CafeLeche.copy(alpha = 0.45f)
                            }
                        )
                )
            }
        }
    }
}

private val VerdeSeguridad = Color(0xFF72966B)

// =====================================================
// FASE 2: UBICACIÓN
// =====================================================

@Composable
private fun FormularioUbicacion(
    estado: RegistroUiState,
    viewModel: RegistroViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Configura tu ubicación para\n" +
                    "encontrar mascotas cerca de ti",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = CafeOscuro,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        CampoRegistro(
            valor = estado.ciudad,
            alCambiar = viewModel::actualizarCiudad,
            etiqueta = "◎   Ciudad*",
            error = estado.errorCiudad
        )

        Spacer(modifier = Modifier.height(10.dp))

        CampoRegistro(
            valor = estado.direccion,
            alCambiar = viewModel::actualizarDireccion,
            etiqueta = "♧   Dirección*",
            error = estado.errorDireccion
        )

        Spacer(modifier = Modifier.height(12.dp))

        MapaIlustrativo()

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = estado.aceptaTerminos,
                onCheckedChange = {
                    viewModel.actualizarAceptacionTerminos(it)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = NaranjaCalido,
                    uncheckedColor = BordeCampo,
                    checkmarkColor = CafeOscuro
                ),
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Acepto los Términos y Condiciones y las " +
                            "Políticas de Adopción de la app.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = CafeOscuro
                )

                if (estado.errorTerminos != null) {
                    Text(
                        text = estado.errorTerminos,
                        fontSize = 10.sp,
                        color = RojoError
                    )
                }
            }
        }
    }
}

// =====================================================
// MAPA ILUSTRATIVO TEMPORAL
// =====================================================

@Composable
private fun MapaIlustrativo() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(FondoMapa)
            .border(
                width = 1.dp,
                color = BordeCampo,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Calles horizontales
            for (i in 1..5) {
                val y = size.height * i / 6f

                drawLine(
                    color = LineaMapa,
                    start = Offset(0f, y),
                    end = Offset(size.width, y - 12f),
                    strokeWidth = 3f
                )
            }

            // Calles verticales
            for (i in 1..6) {
                val x = size.width * i / 7f

                drawLine(
                    color = LineaMapa,
                    start = Offset(x, 0f),
                    end = Offset(x + 15f, size.height),
                    strokeWidth = 3f
                )
            }

            // Vía diagonal
            val camino = Path().apply {
                moveTo(0f, size.height * 0.8f)
                lineTo(size.width * 0.35f, size.height * 0.4f)
                lineTo(size.width * 0.62f, size.height * 0.55f)
                lineTo(size.width, size.height * 0.2f)
            }

            drawPath(
                path = camino,
                color = CafeLeche.copy(alpha = 0.7f),
                style = Stroke(
                    width = 5f,
                    cap = StrokeCap.Round
                )
            )

            // Marcador central
            drawCircle(
                color = NaranjaCalido,
                radius = 9f,
                center = Offset(
                    size.width * 0.5f,
                    size.height * 0.43f
                )
            )

            drawCircle(
                color = CafeOscuro,
                radius = 4f,
                center = Offset(
                    size.width * 0.5f,
                    size.height * 0.43f
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "UBICACIÓN DE REFERENCIA",
                fontSize = 8.sp,
                color = CafeOscuro
            )

            Spacer(modifier = Modifier.height(35.dp))

            Surface(
                color = NaranjaCalido,
                shape = RoundedCornerShape(30.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.clickable {
                    // La geolocalización real se integrará después.
                }
            ) {
                Text(
                    text = "➤  Establecer mi ubicación",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = CafeOscuro,
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 9.dp
                    )
                )
            }
        }
    }
}

// =====================================================
// BOTÓN PRINCIPAL REUTILIZABLE
// =====================================================

@Composable
private fun BotonPrincipal(
    texto: String,
    habilitado: Boolean = true,
    alHacerClick: () -> Unit
) {
    Button(
        onClick = alHacerClick,
        enabled = habilitado,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NaranjaCalido,
            contentColor = CafeOscuro,
            disabledContainerColor = Color(0xFFA69A88),
            disabledContentColor = CafeOscuro
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp
        )
    ) {
        Text(
            text = texto,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// =====================================================
// DIÁLOGO: CUENTA CREADA
// =====================================================

@Composable
private fun DialogoCuentaCreada(
    alAceptar: () -> Unit
) {
    Dialog(
        onDismissRequest = alAceptar
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFFBF5),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 20.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(CafeLeche),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🐾",
                        fontSize = 27.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Cuenta creada",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeOscuro
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "¡Bienvenido! Tu cuenta ya está lista para " +
                            "encontrar mascotas cerca de ti.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center,
                    color = CafeOscuro
                )

                Spacer(modifier = Modifier.height(20.dp))

                BotonPrincipal(
                    texto = "Aceptar",
                    alHacerClick = alAceptar
                )
            }
        }
    }
}
