package co.edu.uniquindio.petadoptionapp.features.passwordreset


import co.edu.uniquindio.petadoptionapp.core.util.RequestResult

data class PasswordResetUiState(
    val verificationCode: List<String> = List(5) { "" },
    val newPassword: String = "",
    val confirmPassword: String = "",

    val codeError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,

    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,

    val resetResult: RequestResult? = null
) {
    val isFormValid: Boolean
        get() =
            verificationCode.size == 5 &&
                    verificationCode.all {
                        it.length == 1 && it[0].isDigit()
                    } &&
                    newPassword.isNotBlank() &&
                    confirmPassword.isNotBlank() &&
                    codeError == null &&
                    newPasswordError == null &&
                    confirmPasswordError == null &&
                    newPassword == confirmPassword
}