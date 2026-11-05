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

data class LocationPickerUiState(
    // Ciudad elegida en el selector (índice en CityCatalog); centra el mapa y acota la búsqueda.
    val cityIndex: Int = 0,
    val query: String = "",
    val suggestions: List<Location> = emptyList(),
    val isSearching: Boolean = false,
    // Punto elegido (tocando el mapa o una sugerencia), con su dirección.
    val selected: Location? = null,
    val cameraCenter: GeoPoint = CityCatalog.all.first().let { GeoPoint(it.latitude, it.longitude) },
    // Buscando la dirección del punto tocado.
    val isResolving: Boolean = false,
)

/**
 Elegir una ubicación: la ciudad (selector) mueve el mapa; la dirección se busca escribiendo
 o se toma del punto que se toque en el mapa. Lo usan el registro y "Seleccionar en el mapa" al publicar.
 */
@HiltViewModel
class LocationPickerViewModel @Inject constructor(
    private val searchAddress: SearchAddressUseCase,
    private val reverseGeocode: ReverseGeocodeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationPickerUiState())
    val uiState: StateFlow<LocationPickerUiState> = _uiState.asStateFlow()

    private var started = false
    private var searchJob: Job? = null

    /** Primera vez: si ya había un punto (ej. al editar), arranca ahí y en su ciudad. */
    fun start(initial: Location?) {
        if (started) return
        started = true
        val point = initial?.toGeoPoint() ?: return
        val cityIndex = CityCatalog.find(initial.city)?.let { CityCatalog.all.indexOf(it) } ?: 0
        _uiState.update { it.copy(cityIndex = cityIndex, selected = initial, cameraCenter = point, query = initial.address) }
    }

    fun onCitySelected(index: Int) {
        val city = CityCatalog.all.getOrNull(index) ?: return
        _uiState.update {
            it.copy(
                cityIndex = index,
                cameraCenter = GeoPoint(city.latitude, city.longitude),
                suggestions = emptyList(),
                // El punto anterior era de otra ciudad: se vuelve a elegir.
                selected = it.selected?.takeIf { s -> s.city.equals(city.name, ignoreCase = true) },
            )
        }
    }

    // Busca mientras se escribe (espera a que deje de escribir para no consultar por cada letra).
    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            _uiState.update { it.copy(isSearching = true) }
            val city = CityCatalog.all[_uiState.value.cityIndex].toLocation()
            val results = searchAddress(value, city).getOrDefault(emptyList())
            _uiState.update { it.copy(isSearching = false, suggestions = results) }
        }
    }

    fun onSuggestionClick(location: Location) {
        searchJob?.cancel()
        val city = CityCatalog.all[_uiState.value.cityIndex]
        val point = location.toGeoPoint() ?: return
        val chosen = location.copy(
            city = location.city.ifBlank { city.name },
            country = location.country.ifBlank { city.country },
        )
        _uiState.update { it.copy(selected = chosen, cameraCenter = point, query = chosen.address, suggestions = emptyList()) }
    }

    /** Tocar el mapa: ese punto es la ubicación; la dirección se busca en segundo plano. */
    fun onMapClick(point: GeoPoint) {
        val city = CityCatalog.all[_uiState.value.cityIndex]
        searchJob?.cancel()
        // El punto se marca al instante; la dirección llega después.
        _uiState.update {
            it.copy(
                isResolving = true,
                suggestions = emptyList(),
                selected = Location(point.latitude, point.longitude, city.name, city.country, it.selected?.address.orEmpty()),
            )
        }
        viewModelScope.launch {
            val resolved = reverseGeocode(point.latitude, point.longitude, city.name, city.country)
            _uiState.update { it.copy(isResolving = false, selected = resolved, query = resolved.address) }
        }
    }
}
