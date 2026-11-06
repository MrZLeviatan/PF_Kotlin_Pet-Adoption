package co.edu.uniquindio.petadoptionapp.domain.model.post

import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import kotlinx.serialization.Serializable


/**
Modelo de dominio: publicación de una mascota: adopción, pérdida, hallazgo, hogar temporal o jornada veterinaria.

Las relaciones con otras entidades se guardan solo como ids ([authorId], [categoryId], [animalTypeId]).
Los comentarios viven en la subcolección `posts/{id}/comments`; aquí solo se guarda su cantidad.

Los campos del animal son nulables porque no aplican a todas las
categorías (una jornada de vacunación no tiene mascota).
Es el modelo central de la app: lo usan PostRepository, los casos de uso de usecase/post,
la moderación, el feed, el mapa y el análisis con IA.
**/
// Permite convertir la publicación a/desde el formato de almacenamiento (Firestore).
@Serializable
// Declara la publicación como clase de datos inmutable.
data class Post(
    // Identificador único de la publicación.
    val id: String = "",
    // Id de quien publica.
    val authorId: String = "",
    // Título de la publicación.
    val title: String = "",
    // Id de la categoría en el catálogo.
    val categoryId: String = "",
    // Descripción detallada del caso.
    val description: String = "",
    // Id del tipo de animal en el catálogo.
    val animalTypeId: String? = null,
    // Raza del animal (texto libre, puede ir vacío).
    val breed: String = "",
    // Tamaño aproximado del animal; nulo si no aplica.
    val size: AnimalSize? = null,
    // Edad aproximada en meses; nula si no se conoce o no aplica.
    val ageInMonths: Int? = null,
    // `true` si tiene las vacunas al día; nulo si no se sabe o no aplica.
    val vaccinated: Boolean? = null,
    // `true` si está castrado; nulo si no se sabe o no aplica.
    val neutered: Boolean? = null,
    // Lista de URLs de las fotos en Cloudinary.
    val photoUrls: List<String> = emptyList(),
    // Localización de la publicación.
    val location: Location = Location(),
    // Ciclo de vida de la publicación.
    val status: PostStatus = PostStatus.IN_PROGRESS,
    // Análisis de IA y verificación de moderadores.
    val verification: Verification = Verification(),
    // Revisión de la IA (resumen, riesgos, duplicado...); nula hasta que se analice.
    val aiReview: AiReview? = null,
    // Cantidad de "Es importante".
    val importantCount: Int = 0,
    // Cantidad de comentarios.
    val commentCount: Int = 0,
    // Fecha de publicación, en milisegundos (epoch).
    val createdAt: Long = System.currentTimeMillis(),
    // Última edición, en milisegundos (epoch).
    val updatedAt: Long = System.currentTimeMillis(),
    // Momento en que se finalizó; nulo si sigue activa.
    val finishedAt: Long? = null
) {


    /**
     ¿Debe aparecer en el feed público? Solo si fue verificada y sigue
     en proceso. Las rechazadas, finalizadas y eliminadas no se muestran.
     */
    fun showsInPublicFeed(): Boolean =
        // Exige verificación aprobada y que el caso siga abierto.
        verification.status == VerificationStatus.VERIFIED &&
                status == PostStatus.IN_PROGRESS

    // Reglas mínimas para poder guardar: título, categoría, foto y coordenadas.
    fun canBePublished(): Boolean =
        // Exige título y categoría no vacíos, al menos una foto y coordenadas válidas.
        title.isNotBlank() &&
                categoryId.isNotBlank() &&
                photoUrls.isNotEmpty() &&
                location.hasCoordinates()
}
