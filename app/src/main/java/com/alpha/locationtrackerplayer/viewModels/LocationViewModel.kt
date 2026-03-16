package com.alpha.locationtrackerplayer.viewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.locationtrackerplayer.data.model.LocationHistory
import com.alpha.locationtrackerplayer.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {

    private val repository = LocationRepository()

    private val _locations = MutableStateFlow<List<LocationHistory>>(emptyList())
    val locations: StateFlow<List<LocationHistory>> = _locations

    fun loadLocations(userId: String) {

        viewModelScope.launch {

            repository.getLocations(userId)
                .collect {
                    _locations.value = it
                }

        }

    }

}