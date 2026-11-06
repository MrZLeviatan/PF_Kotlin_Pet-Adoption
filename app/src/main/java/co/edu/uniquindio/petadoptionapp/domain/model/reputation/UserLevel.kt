package co.edu.uniquindio.petadoptionapp.domain.model.reputation

/**
Modelo de dominio: niveles de reputación definidos por el enunciado.

Cada nivel se alcanza al acumular [minPoints]. El nivel NO se guarda
como dato independiente: siempre se calcula desde los puntos, así nunca
queda desincronizado.

- displayName: nombre que se muestra en la interfaz.
- minPoints: puntos mínimos necesarios para alcanzar el nivel.
Lo usan [Reputation], [AwardResult] y el perfil para mostrar el nivel actual.
**/
// Enumera los niveles con su nombre visible y los puntos mínimos requeridos.
enum class UserLevel(val displayName: String, val minPoints: Int) {
    // Nivel inicial: desde 0 puntos.
    ANIMAL_FRIEND("Amigo Animal", 0),
    // Segundo nivel: desde 100 puntos.
    PROTECTOR("Protector", 100),
    // Tercer nivel: desde 300 puntos.
    GUARDIAN("Guardián", 300),
    // Nivel máximo: desde 700 puntos.
    PET_HERO("Héroe de las Mascotas", 700);

    // Utilidades compartidas sin necesidad de instanciar el enum.
    companion object {
        // Devuelve el nivel más alto cuyo mínimo ya fue alcanzado.
        fun fromPoints(points: Int): UserLevel =
            // Recorre los niveles en orden y toma el último cuyo mínimo no supera los puntos.
            entries.last { points >= it.minPoints }
    }
}
