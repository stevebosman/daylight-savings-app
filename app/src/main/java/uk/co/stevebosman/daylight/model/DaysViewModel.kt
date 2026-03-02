package uk.co.stevebosman.daylight.model

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uk.co.stevebosman.daylight.notifications.checkNotificationPermissions
import uk.co.stevebosman.daylight.notifications.scheduleNotifications

data class LocationState(
    val longitude: Double = 0.79,
    val latitude: Double = 51.47,
    val name: String = "Essex"
)

class DaysViewModel : ViewModel() {
    // Expose screen UI state
    private val _uiState = MutableStateFlow(LocationState())
    val uiState: StateFlow<LocationState> = _uiState.asStateFlow()

    fun setLocation(longitude: Double, latitude: Double, context: Context) {
        _uiState.update { currentState ->
            currentState.copy(
                longitude = longitude,
                latitude = latitude,
            )
        }
        if (checkNotificationPermissions(context)) {
            scheduleNotifications(context, 10, longitude, latitude)
        }
    }

    fun setLocationName(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                name = name,
            )
        }
    }
}