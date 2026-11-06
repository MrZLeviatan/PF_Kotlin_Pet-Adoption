package co.edu.uniquindio.petadoptionapp.core.component.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.location.CityCatalog
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.usecase.location.ReverseGeocodeUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.location.SearchAddressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
ViewModel del componente [LocationPicker]. Mantiene el estado del selector de ubicación (ciudad, texto de búsqueda,
sugerencias, punto elegido y centro de la cámara) y se apoya en dos casos de uso del dominio:
- [SearchAddressUseCase]: busca direcciones a partir del texto escrito (geocodificación), acotadas a la ciudad.
- [ReverseGeocodeUseCase]: obtiene la dirección de un punto tocado en el mapa (geocodificación inversa).
Se inyecta con Hilt y expone su estado como un [StateFlow] inmutable que la interfaz observa.
**/

// Estado de la interfaz del selector de ubicación.
data class LocationPickerUiState(
    // Ciudad elegida en el selector (índice en CityCatalog); centra el mapa y acota la búsqueda.
    val cityIndex: Int = 0,
    // Texto escrito en el campo de dirección.
    val query: String = "",
    // Direcciones sugeridas para el texto escrito.
    val suggestions: List<Location> = emptyList(),
    // Indica si hay una búsqueda de direcciones en curso.
    val isSearching: Boolean = false,
    // Punto elegido (tocando el mapa o una sugerencia), con su dirección.
    val selected: Location? = null,
    // Centro de la cámara del mapa; por defecto, la primera ciudad del catálogo.
    val cameraCenter: GeoPoint = CityCatalog.all.first().let { GeoPoint(it.latitude, it.longitude) },
    // Buscando la dirección del punto tocado.
    val isResolving: Boolean = false,
)

/**
Elegir una ubicación: la ciudad (selector) mueve el mapa; la dirección se busca escribiendo
o se toma del punto que se toque en el mapa. Lo usan el registro y "Seleccionar en el mapa" al publicar.
**/
@HiltViewModel
class LocationPickerViewModel @Inject constructor(
    // Caso de uso que busca direcciones por texto.
    private val searchAddress: SearchAddressUseCase,
    // Caso de uso que obtiene la dirección de unas coordenadas.
    private val reverseGeocode: ReverseGeocodeUseCase,
) : ViewModel() {

    // Estado mutable interno, solo modificable desde el ViewModel.
    private val _uiState = MutableStateFlow(LocationPickerUiState())
    // Estado público de solo lectura que observa la interfaz.
    val uiState: StateFlow<LocationPickerUiState> = _uiState.asStateFlow()

    // Evita reiniciar el estado si la pantalla vuelve a llamar a start (por ejemplo, tras recomponer).
    private var started = false
    // Trabajo de búsqueda en curso, para poder cancelarlo cuando llega un texto nuevo.
    private var searchJob: Job? = null

    /**
    Primera vez: si ya había un punto (ej. al editar), arranca ahí y en su ciudad.
    **/
    fun start(initial: Location?) {
        // Si ya se inició antes, no hace nada.
        if (started) return
        // Marca el ViewModel como iniciado.
        started = true
        // Si no hay ubicación inicial con coordenadas, se mantiene el estado por defecto.
        val point = initial?.toGeoPoint() ?: return
        // Busca la ciudad de la ubicación inicial en el catálogo; si no aparece, usa la primera.
        val cityIndex = CityCatalog.find(initial.city)?.let { CityCatalog.all.indexOf(it) } ?: 0
        // Carga en el estado la ciudad, el punto elegido, la cámara y el texto de la dirección.
        _uiState.update { it.copy(cityIndex = cityIndex, selected = initial, cameraCenter = point, query = initial.address) }
    }

    // Cambia la ciudad elegida y mueve el mapa hacia ella.
    fun onCitySelected(index: Int) {
        // Obtiene la ciudad del catálogo; si el índice no es válido, no hace nada.
        val city = CityCatalog.all.getOrNull(index) ?: return
        // Actualiza el estado con la nueva ciudad.
        _uiState.update {
            it.copy(
                // Guarda el índice de la ciudad elegida.
                cityIndex = index,
                // Centra la cámara en las coordenadas de la ciudad.
                cameraCenter = GeoPoint(city.latitude, city.longitude),
                // Limpia las sugerencias, que eran de la ciudad anterior.
                suggestions = emptyList(),
                // El punto anterior era de otra ciudad: se vuelve a elegir.
                selected = it.selected?.takeIf { s -> s.city.equals(city.name, ignoreCase = true) },
            )
        }
    }

    // Busca mientras se escribe (espera a que deje de escribir para no consultar por cada letra).
    fun onQueryChange(value: String) {
        // Refleja de inmediato el texto escrito en el campo.
        _uiState.update { it.copy(query = value) }
        // Cancela la búsqueda anterior, si seguía pendiente.
        searchJob?.cancel()
        // Inicia una nueva búsqueda en segundo plano.
        searchJob = viewModelScope.launch {
            // Espera 400 ms (debounce); si llega otra letra antes, este trabajo se cancela.
            delay(400)
            // Indica que la búsqueda está en curso.
            _uiState.update { it.copy(isSearching = true) }
            // Toma la ciudad elegida para acotar la búsqueda.
            val city = CityCatalog.all[_uiState.value.cityIndex].toLocation()
            // Ejecuta la búsqueda; si falla, se usa una lista vacía.
            val results = searchAddress(value, city).getOrDefault(emptyList())
            // Termina la búsqueda y publica las sugerencias.
            _uiState.update { it.copy(isSearching = false, suggestions = results) }
        }
    }

    // Toma una sugerencia de la lista como ubicación elegida.
    fun onSuggestionClick(location: Location) {
        // Cancela cualquier búsqueda pendiente para que no sobrescriba la elección.
        searchJob?.cancel()
        // Ciudad elegida actualmente, usada para completar datos faltantes.
        val city = CityCatalog.all[_uiState.value.cityIndex]
        // Si la sugerencia no tiene coordenadas, no se puede usar.
        val point = location.toGeoPoint() ?: return
        // Completa ciudad y país con los de la ciudad elegida si la sugerencia no los trae.
        val chosen = location.copy(
            city = location.city.ifBlank { city.name },
            country = location.country.ifBlank { city.country },
        )
        // Marca la ubicación elegida, centra el mapa en ella, muestra su dirección y oculta las sugerencias.
        _uiState.update { it.copy(selected = chosen, cameraCenter = point, query = chosen.address, suggestions = emptyList()) }
    }

    /**
    Tocar el mapa: ese punto es la ubicación; la dirección se busca en segundo plano.
    **/
    fun onMapClick(point: GeoPoint) {
        // Ciudad elegida actualmente, usada como contexto para la dirección.
        val city = CityCatalog.all[_uiState.value.cityIndex]
        // Cancela la búsqueda por texto pendiente.
        searchJob?.cancel()
        // El punto se marca al instante; la dirección llega después.
        _uiState.update {
            it.copy(
                // Indica que se está buscando la dirección del punto.
                isResolving = true,
                // Oculta las sugerencias.
                suggestions = emptyList(),
                // Crea la ubicación con las coordenadas tocadas y conserva temporalmente la dirección anterior.
                selected = Location(point.latitude, point.longitude, city.name, city.country, it.selected?.address.orEmpty()),
            )
        }
        // Busca la dirección del punto en segundo plano.
        viewModelScope.launch {
            // Ejecuta la geocodificación inversa con las coordenadas y la ciudad.
            val resolved = reverseGeocode(point.latitude, point.longitude, city.name, city.country)
            // Guarda la ubicación con su dirección y la muestra en el campo de texto.
            _uiState.update { it.copy(isResolving = false, selected = resolved, query = resolved.address) }
        }
    }
}
