package co.edu.uniquindio.petadoptionapp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.uniquindio.petadoptionapp.features.login.LoginScreen
import co.edu.uniquindio.petadoptionapp.features.passwordreset.PasswordResetScreen
import co.edu.uniquindio.petadoptionapp.features.splash.SplashScreen
import kotlinx.coroutines.delay

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = MainRoutes.Splash
        ) {

            composable<MainRoutes.Splash> {
                SplashRoute(navController)
            }

            composable<MainRoutes.Login> {
                LoginScreen(
                    onForgotPassword = {
                        navController.navigate(
                            MainRoutes.PasswordReset
                        )
                    }
                )
            }

            composable<MainRoutes.PasswordReset> {
                PasswordResetScreen(
                    onBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun SplashRoute(
    navController: NavHostController
) {

    LaunchedEffect(Unit) {
        delay(2000)

        navController.navigate(MainRoutes.Login) {
            popUpTo<MainRoutes.Splash> {
                inclusive = true
            }
        }
    }

    SplashScreen()
}