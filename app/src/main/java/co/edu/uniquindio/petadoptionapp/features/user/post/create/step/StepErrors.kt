package co.edu.uniquindio.petadoptionapp.features.user.post.create.step

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.features.user.post.create.FieldError

/**
Funciones auxiliares de los pasos del formulario de crear/editar publicación.
Traducen el tipo de error de un campo (FieldError, calculado por CreatePostViewModel como
lógica) al texto que se muestra debajo del campo, usando los límites del validador
(PostValidator) como parte de la presentación. Devuelven null cuando el campo no tiene error.
**/

// Convierte un error de longitud (vacío, corto, largo o inválido) en su mensaje con los límites.
@Composable
internal fun lengthErrorText(error: FieldError?, min: Int, max: Int): String? = when (error) {
    // Sin error: no se muestra mensaje.
    null -> null
    // Campo vacío: mensaje de campo obligatorio.
    FieldError.EMPTY -> stringResource(R.string.create_post_error_required)
    // Muy corto: indica la longitud mínima.
    FieldError.TOO_SHORT -> stringResource(R.string.create_post_error_min, min)
    // Muy largo: indica la longitud máxima.
    FieldError.TOO_LONG -> stringResource(R.string.create_post_error_max, max)
    // Valor no válido: mensaje genérico.
    FieldError.INVALID -> stringResource(R.string.create_post_error_invalid)
}

// Convierte un error de la edad en su mensaje; el máximo se expresa en años.
@Composable
internal fun ageErrorText(error: FieldError?, maxMonths: Int): String? = when (error) {
    // Sin error: no se muestra mensaje.
    null -> null
    // Campo vacío: mensaje de campo obligatorio.
    FieldError.EMPTY -> stringResource(R.string.create_post_error_required)
    // Cualquier otro error: edad fuera de rango, con el máximo convertido de meses a años.
    else -> stringResource(R.string.create_post_error_age, maxMonths / 12)
}
