package co.edu.uniquindio.petadoptionapp.features.user.post.create.step

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.features.user.post.create.FieldError

// Traducen el tipo de error (lógica) al texto, con los límites del validador (presentación).

@Composable
internal fun lengthErrorText(error: FieldError?, min: Int, max: Int): String? = when (error) {
    null -> null
    FieldError.EMPTY -> stringResource(R.string.create_post_error_required)
    FieldError.TOO_SHORT -> stringResource(R.string.create_post_error_min, min)
    FieldError.TOO_LONG -> stringResource(R.string.create_post_error_max, max)
    FieldError.INVALID -> stringResource(R.string.create_post_error_invalid)
}

@Composable
internal fun ageErrorText(error: FieldError?, maxMonths: Int): String? = when (error) {
    null -> null
    FieldError.EMPTY -> stringResource(R.string.create_post_error_required)
    else -> stringResource(R.string.create_post_error_age, maxMonths / 12)
}
