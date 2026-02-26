package uk.co.stevebosman.daylight

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import uk.co.stevebosman.daylight.notifications.checkNotificationPermissions
import uk.co.stevebosman.daylight.notifications.createNotificationChannel
import uk.co.stevebosman.daylight.notifications.scheduleNotifications
import uk.co.stevebosman.daylight.ui.DaylightTimesScreen
import uk.co.stevebosman.daylight.ui.theme.MainActivityTheme

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLocationPermissions()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        enableEdgeToEdge()
        val context: Context = this

        setContent {
            val advanceMinutes = 10
            var longitude by remember { mutableDoubleStateOf(0.78667) }
            var latitude by remember { mutableDoubleStateOf(51.46778) }
            var name by remember { mutableStateOf("Essex") }
            if (checkNotificationPermissions(this)) {
                scheduleNotifications(context, advanceMinutes, longitude, latitude)
            }

            Geocoder(this).getFromLocation(latitude, longitude, 1) { addresses ->
                Log.d("Daylight", "Ui: ${addresses.get(0)}")
                val address = addresses.get(0).getAddressLine(0) ?: ""
                if (address.contains(',')) {
                    name = address.substring(address.indexOf(',') + 1).trim()
                } else {
                    name = address
                }
            }
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        longitude = location.longitude
                        latitude = location.latitude
                        if (checkNotificationPermissions(this)) {
                            scheduleNotifications(context, advanceMinutes, longitude, latitude)
                        }
                    }
                }
            }
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
                                    Toast.makeText(
                                        context,
                                        R.string.settings,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }) {
                                    Icon(painter = painterResource(id= R.drawable.settings_24px), "")
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { innerPadding ->
                    DaylightTimesScreen(
                        latitude, longitude, name, Modifier
                            .padding(innerPadding)
                    )
                }
            }
        }
        createNotificationChannel(this)
    }

    fun requestLocationPermissions() {
        val locationPermissionRequest = this.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
//                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
//                    // Precise location access granted.
//                }
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
//                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}
