package co.edu.uniquindio.petadoptionapp.domain.repository

import co.edu.uniquindio.petadoptionapp.domain.model.location.Location

/**
Contrato para convertir direcciones en puntos del mapa y al revés.
 */
interface GeocodingRepository {
    /**
    Busca direcciones que coincidan con el texto, cerca de [near] si se da (la ciudad elegida).
    - Qué recibe: Un [Result] con hasta [limit] ubicaciones (con coordenadas, ciudad y dirección).
     */
    suspend fun search(query: String, near: Location?, limit: Int): Result<List<Location>>

    /**
    Obtiene la dirección de un punto del mapa (al tocarlo o con la ubicación actual).
    - Qué recibe: Un [Result] con la ubicación (con coordenadas) o null si no se encontró dirección.
     */
    suspend fun reverse(latitude: Double, longitude: Double): Result<Location?>
}
