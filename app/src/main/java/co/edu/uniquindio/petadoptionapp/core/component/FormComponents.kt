package co.edu.uniquindio.petadoptionapp.core.component

// Importación general de materiales y componentes visuales (androidx)
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.petadoptionapp.core.theme.*

// Genera un texto estilizado agregando un asterisco de color rojo al final para indicar campo obligatorio.
fun requiredText(text: String): AnnotatedString = buildAnnotatedString {
    // Añade el texto base recibido como parámetro.
    append(text)
    // Aplica un estilo con color rojo exclusivamente al asterisco añadido al final.
    withStyle(SpanStyle(color = Color.Red)) { append("*") }
}

// Configuración de colores estandarizada que comparten todos los campos de texto del formulario.
@Composable
fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    // Color de fondo cuando el campo tiene el foco de escritura.
    focusedContainerColor = FondoClaro,
    // Color de fondo cuando el campo se encuentra en reposo.
    unfocusedContainerColor = FondoClaro,
    // Color de fondo cuando ocurre un error
    errorContainerColor = FondoClaro,
    // Color del borde exterior cuando el usuario está escribiendo.
    focusedBorderColor = BordeMarron,
    // Color del borde exterior cuando el campo está inactivo.
    unfocusedBorderColor = BordeMarron,
    // Color del texto que el usuario escribe al enfocar el campo.
    focusedTextColor = TextoMarron,
    // Color del texto cuando el campo está inactivo.
    unfocusedTextColor = TextoMarron,
    // Color de la etiqueta flotante cuando el campo está enfocado.
    focusedLabelColor = TextoMarron,
    // Color del texto al ocurrir un error
    errorTextColor = TextoMarron,
    // Color de la etiqueta flotante cuando el campo está en reposo.
    unfocusedLabelColor = TextoMarron,

    // Color de los mensajes de error

    errorBorderColor = ColorError,
    // Color de la etiqueta flotante al haber error
    errorLabelColor = ColorError,
    // Color del texto de apoyo (el mensaje de error debajo del campo)
    errorSupportingTextColor = ColorError,
    // Color del cursor de escritura si llega a activarse en error
    errorCursorColor = ColorError
)

// Componente de tarjeta blanca con esquinas redondeadas y un título centrado para agrupar secciones.
@Composable
fun FormCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        // Hace que la tarjeta ocupe todo el ancho disponible del contenedor padre.
        modifier = Modifier.fillMaxWidth(),
        // Redondea las esquinas de la tarjeta con un radio de 24dp.
        shape = RoundedCornerShape(24.dp),
        // Define los colores de la tarjeta usando el color de fondo para tarjetas del tema.
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta)
    ) {
        // Columna interna que aplica un margen uniforme de 20dp alrededor del contenido.
        Column(modifier = Modifier.padding(20.dp)) {
            // Componente de texto para mostrar el título principal de la tarjeta.
            Text(
                text = title,
                // Ocupa todo el ancho para permitir el centrado exacto.
                modifier = Modifier.fillMaxWidth(),
                // Centra horizontalmente el texto del título.
                textAlign = TextAlign.Center,
                // Define el tamaño de letra en 18sp.
                fontSize = 18.sp,
                // Aplica negrita fuerte al título.
                fontWeight = FontWeight.ExtraBold,
                // Asigna el color de texto marrón característico.
                color = TextoMarron
            )
            // Inserta un espacio vertical de separación de 16dp bajo el título.
            Spacer(modifier = Modifier.height(16.dp))
            // Ejecuta y dibuja el contenido específico pasado como parámetro lambda en cada pantalla.
            content()
        }
    }
}

// Componente reutilizable para campos de texto de una sola línea con soporte opcional para mensajes de error.
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        // Hace que el campo de texto ocupe todo el ancho disponible en la fila.
        modifier = Modifier.fillMaxWidth(),
        // Asigna la etiqueta superior incluyendo automáticamente el asterisco rojo obligatorio.
        label = { Text(requiredText(label)) },
        // Limita la entrada de texto exclusivamente a una sola línea.
        singleLine = true,
        // Activa el estado de error si el texto de error no es nulo, cambiando los bordes a rojo.
        isError = errorText != null,
        // Muestra condicionalmente un texto de error debajo del campo si existe alguna incidencia.
        supportingText = if (errorText != null) { { Text(errorText) } } else null,
        // Redondea las esquinas del campo de texto con un radio de 12dp.
        shape = RoundedCornerShape(12.dp),
        // Aplica la paleta de colores unificada para campos de formulario.
        colors = formFieldColors()
    )
}

// Componente personalizado para listas desplegables (Dropdown) seleccionables.
@Composable
fun FormDropdown(
    value: String,
    label: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    // Estado mutable para controlar si el menú desplegable se encuentra abierto o cerrado.
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = value,
            // Evita modificaciones manuales directas; el valor solo se cambia mediante selección.
            onValueChange = {},
            // Configura el campo como de solo lectura.
            readOnly = true,
            // Ocupa todo el ancho disponible.
            modifier = Modifier.fillMaxWidth(),
            // Muestra la etiqueta obligatoria con asterisco rojo.
            label = { Text(requiredText(label)) },
            // Coloca un icono indicador de flecha hacia abajo al final del campo.
            trailingIcon = { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null) },
            // Redondea las esquinas con un radio de 12dp.
            shape = RoundedCornerShape(12.dp),
            // Aplica los colores estándar del formulario.
            colors = formFieldColors()
        )
        // Capa invisible superpuesta que detecta toques para abrir el menú desplegable.
        Box(modifier = Modifier.matchParentSize().clickable { expanded = true })

        // Menú desplegable que muestra las opciones disponibles.
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = FondoTarjeta) {
            // Recorre cada opción de la lista para renderizarla como un elemento seleccionable.
            options.forEach { option ->

                // Evaluamos si esta opción es la que está seleccionada actualmente
                val isSelected = option == value

                DropdownMenuItem(
                    // Muestra el texto de la opción actual.
                    text = { Text(option, color = TextoMarron, fontWeight = FontWeight.Medium) },
                    onClick = {
                        // Notifica al componente padre cuál opción fue seleccionada.
                        onSelect(option)
                        // Cierra automáticamente el menú desplegable.
                        expanded = false
                    },
                    // Aplicamos un modificador condicional para que dibuje el borde hacia afuera si está seleccionada
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp) // Pequeño margen alrededor
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 1.5.dp,
                                    color = TextoMarron, // O el color de borde que prefieras para la selección
                                    shape = RoundedCornerShape(8.dp) // Bordes redondeados para el contorno
                                )
                            } else {
                                Modifier // Sin borde si no está seleccionado
                            }
                        ),
                    // Personaliza los colores de los elementos internos si lo requieres
                    colors = MenuDefaults.itemColors(
                        textColor = TextoMarron,
                        leadingIconColor = TextoMarron,
                        trailingIconColor = TextoMarron
                    )
                )
            }
        }
    }
}

// Componente que organiza en una fila un texto explicativo a la izquierda y un interruptor (switch) a la derecha.
@Composable
fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    Row(
        // Ocupa todo el ancho disponible de la fila.
        modifier = Modifier.fillMaxWidth(),
        // Alinea verticalmente los elementos al centro.
        verticalAlignment = Alignment.CenterVertically,
        // Distribuye los elementos extremos separándolos al máximo espacio posible.
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Columna para agrupar el título principal y el subtítulo opcional ocupando el espacio flexible restante.
        Column(modifier = Modifier.weight(1f)) {
            // Muestra el título principal de la fila.
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextoMarron)
            // Si el subtítulo no es nulo, lo renderiza con un tamaño más pequeño y color translúcido.
            if (subtitle != null) {
                Text(subtitle, fontSize = 11.sp, color = TextoMarron.copy(alpha = 0.7f))
            }
        }
        // Componente interruptor (Switch) para alternar estados booleanos.
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            // Configura los colores personalizados para los estados activo e inactivo del interruptor.
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ChipSeleccionado,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = BotonDeshabilitado,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

// Botón de acción principal redondeado que cambia de color según su estado de habilitación.
@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        // Asigna el ancho máximo y una altura fija de 52dp.
        modifier = modifier.fillMaxWidth().height(52.dp),
        // Da una forma completamente redondeada tipo píldora a los bordes del botón.
        shape = RoundedCornerShape(50),
        // Configura los colores del contenedor y texto tanto para cuando está activo como deshabilitado.
        colors = ButtonDefaults.buttonColors(
            containerColor = ItemSeleccionado,
            contentColor = TextoMarron,
            disabledContainerColor = BotonDeshabilitado,
            disabledContentColor = TextoMarron
        )
    ) {
        // Texto interior del botón con tipografía en negrita.
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

// Componente en forma de superficie contenedora de avisos informativos con icono incorporado.
@Composable
fun InfoTip(icon: ImageVector, text: String) {
    Surface(
        // Ocupa todo el ancho de la línea.
        modifier = Modifier.fillMaxWidth(),
        // Redondea las esquinas con un radio de 12dp.
        shape = RoundedCornerShape(12.dp),
        // Define el color de fondo para la caja de aviso.
        color = FondoAviso,
        // Agrega un borde exterior con una sutil transparencia marrón.
        border = BorderStroke(1.dp, TextoMarron.copy(alpha = 0.6f))
    ) {
        // Fila para alinear horizontalmente el icono y el texto informativo.
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            // Agrega una separación constante de 12dp entre el icono y el texto.
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Muestra el icono representativo del aviso con tamaño fijo de 24dp.
            Icon(icon, contentDescription = null, tint = ChipSeleccionado, modifier = Modifier.size(24.dp))
            // Muestra el texto explicativo del aviso.
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextoMarron)
        }
    }
}