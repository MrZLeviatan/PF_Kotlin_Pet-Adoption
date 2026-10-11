package co.edu.uniquindio.petadoptionapp.features.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.CafeOscuro
import co.edu.uniquindio.petadoptionapp.core.theme.Cream

@Composable
fun SplashScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "HUELLAS",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            color = CafeOscuro,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Red de adopción y protección de mascotas",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            color = CafeOscuro,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}