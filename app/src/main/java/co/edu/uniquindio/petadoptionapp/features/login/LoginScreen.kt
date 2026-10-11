package co.edu.uniquindio.petadoptionapp.features.login


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.petadoptionapp.core.component.HuellasButton
import co.edu.uniquindio.petadoptionapp.core.component.HuellasPasswordField
import co.edu.uniquindio.petadoptionapp.core.component.HuellasTextField
import co.edu.uniquindio.petadoptionapp.core.theme.CafeOscuro
import co.edu.uniquindio.petadoptionapp.core.theme.Cream

@Composable
fun LoginScreen(
    onForgotPassword: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LoginContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordVisibilityChange = viewModel::onPasswordVisibilityChange,
        onLogin = viewModel::login,
        onForgotPassword = onForgotPassword


    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(
                horizontal = 24.dp,
                vertical = 32.dp
            ),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.headlineMedium,
            color = CafeOscuro
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        HuellasTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = "Correo electrónico",
            isError = uiState.emailError != null,
            errorMessage = uiState.emailError
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        HuellasPasswordField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            passwordVisible = uiState.passwordVisible,
            onPasswordVisibilityChange = onPasswordVisibilityChange,
            isError = uiState.passwordError != null,
            errorMessage = uiState.passwordError
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        HuellasButton(
            text = "Iniciar sesión",
            onClick = onLogin,
            isLoading = uiState.isLoading
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        TextButton(
            onClick = onForgotPassword
        ) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                color = CafeOscuro
            )
        }
    }
}