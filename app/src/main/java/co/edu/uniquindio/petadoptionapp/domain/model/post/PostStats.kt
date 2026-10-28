package co.edu.uniquindio.petadoptionapp.domain.model.post

/**
Métricas del dashboard personal de un usuario. No se guardan: se calculan con conteos.
 */
data class PostStats(
    // Publicaciones verificadas y en proceso.
    val active: Int = 0,
    // Publicaciones finalizadas.
    val finished: Int = 0,
    // Publicaciones esperando o en revisión de un moderador.
    val pendingVerification: Int = 0
)