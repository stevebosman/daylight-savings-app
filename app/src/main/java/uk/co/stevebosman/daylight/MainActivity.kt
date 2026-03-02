package uk.co.stevebosman.daylight

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import uk.co.stevebosman.daylight.model.DaysViewModel
import uk.co.stevebosman.daylight.notifications.createNotificationChannel
import uk.co.stevebosman.daylight.ui.DaylightTimesScreen
import uk.co.stevebosman.daylight.ui.theme.MainActivityTheme

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val viewModel: DaysViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLocationPermissions()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel(this)
        enableEdgeToEdge()
        val context: Context = this

        getLocation(context)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    setContent {
                        MainActivityTheme {
                            Scaffold(
                                topBar = {
                                    TopAppBar(
                                        title = {
                                            Text("Continuous Daylight Savings")
                                        },
                                        actions = {
                                            // Creating Icon button favorites, on click
                                            // would create a Toast message
                                            IconButton(onClick = {
                                                startActivity(
                                                    Intent(
                                                        context,
                                                        SettingsActivity::class.java
                                                    )
                                                )
                                            }) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.settings_24px),
                                                    ""
                                                )
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { innerPadding ->
//                                val state = viewModel.uiState.collectAsState().value
                                if (state.name == "Essex") {
                                    Text("Loading...")
                                } else {
                                    DaylightTimesScreen(
                                        state, Modifier.padding(innerPadding)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getLocation(context: Context) {
        if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location == null) {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_LOW_POWER,
                        CancellationTokenSource().token,
                    ).addOnSuccessListener { location ->
                        if (location != null) {
                            setModelLocation(context, location)
                        }
                    }
                } else {
                    setModelLocation(context, location)
                }
            }
        }
    }

    private fun setModelLocation(
        context: Context,
        location: Location
    ) {
        val longitude = (location.longitude * 100).toInt() / 100.0
        val latitude = (location.latitude * 100).toInt() / 100.0
        viewModel.setLocation(longitude, latitude, context)
        getLocationName(context, location)
    }

    private fun getLocationName(
        context: Context,
        location: Location
    ) {
        Geocoder(context).getFromLocation(location.latitude, location.longitude, 1) { addresses ->
            Log.d("Daylight", "Ui: ${addresses[0]}")
            val address = addresses[0].getAddressLine(0) ?: ""
            val name = if (address.contains(',')) {
                address.substring(address.indexOf(',') + 1).trim()
            } else {
                address
            }
            viewModel.setLocationName(name)
        }
    }

    fun requestLocationPermissions() {
        val locationPermissionRequest = this.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                }

                else -> {
                    // No location access granted.
                }
            }
        }

        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions:
        // https://developer.android.com/training/permissions/requesting#request-permission
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}
