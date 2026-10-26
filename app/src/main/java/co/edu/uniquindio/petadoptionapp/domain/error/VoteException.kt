package co.edu.uniquindio.petadoptionapp.domain.error

/**
Errores del dominio de votos "Es importante".
La UI traduce cada uno a un texto amigable usando strings.xml.
 */
sealed class VoteException : Exception() {

    // Ocurre cuando alguien intenta votar su propia publicación.
    object CannotVoteOwnPost : VoteException()
}