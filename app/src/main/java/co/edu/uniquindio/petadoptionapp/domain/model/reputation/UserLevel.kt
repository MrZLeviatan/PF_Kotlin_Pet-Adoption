package co.edu.uniquindio.petadoptionapp.domain.model.reputation

/**
 Niveles de reputación definidos por el enunciado.

 Cada nivel se alcanza al acumular [minPoints]. El nivel NO se guarda
 como dato independiente: siempre se calcula desde los puntos, así nunca
 queda desincronizado.

 - displayName Nombre que se muestra en la interfaz.
 - minPoints Puntos mínimos necesarios para alcanzar el nivel.
 */
enum class UserLevel(val displayName: String, val minPoints: Int) {
    ANIMAL_FRIEND("Amigo Animal", 0),
    PROTECTOR("Protector", 100),
    GUARDIAN("Guardián", 300),
    PET_HERO("Héroe de las Mascotas", 700);

    companion object {
        // Devuelve el nivel más alto cuyo mínimo ya fue alcanzado.
        fun fromPoints(points: Int): UserLevel =
            entries.last { points >= it.minPoints }
    }
}