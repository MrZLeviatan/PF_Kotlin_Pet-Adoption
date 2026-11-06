package co.edu.uniquindio.petadoptionapp.domain.error

/**
Errores del dominio de votos "Es importante".
Los lanza ToggleImportantUseCase cuando el voto no está permitido.
La UI traduce cada uno a un texto amigable usando strings.xml.
**/
// Jerarquía sellada con los errores posibles de los votos.
sealed class VoteException : Exception() {

    // Ocurre cuando alguien intenta votar su propia publicación.
    object CannotVoteOwnPost : VoteException()
}
