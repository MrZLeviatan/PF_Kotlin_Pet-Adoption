package co.edu.uniquindio.petadoptionapp.navigation

import kotlinx.serialization.Serializable

sealed class MainRoutes {

    @Serializable
    data object Splash : MainRoutes()

    @Serializable
    data object Login : MainRoutes()

    @Serializable
    data object PasswordReset : MainRoutes()
}