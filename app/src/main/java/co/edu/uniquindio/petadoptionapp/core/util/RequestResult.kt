package co.edu.uniquindio.petadoptionapp.core.util

sealed interface RequestResult {
    data class Success(val message: String) : RequestResult
    data class Error(val message: String) : RequestResult
    data object Loading : RequestResult
}
