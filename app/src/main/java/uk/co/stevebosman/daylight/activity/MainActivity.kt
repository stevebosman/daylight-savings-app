package uk.co.stevebosman.daylight.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import uk.co.stevebosman.daylight.activity.ui.theme.MainActivityTheme
import uk.co.stevebosman.daylight.angles.Angle
import uk.co.stevebosman.daylight.formatLatitude
import uk.co.stevebosman.daylight.formatLongDate
import uk.co.stevebosman.daylight.formatLongitude
import uk.co.stevebosman.daylight.formatShortDate
import uk.co.stevebosman.daylight.formatTime
import uk.co.stevebosman.daylight.moon.MoonPhase
import uk.co.stevebosman.daylight.sleepCalculation
import uk.co.stevebosman.daylight.sunrise.DaylightType
import uk.co.stevebosman.daylight.sunrise.calculateSunriseDetails
import uk.co.stevebosman.daylight.wakeCalculation
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        enableEdgeToEdge()

        setContent {
            MainActivityTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text("Continuous Daylight Savings")
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { innerPadding ->
                    Dates(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    fun requestPermissions() {
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

    @Composable
    fun Dates(modifier: Modifier = Modifier) {
        var longitude by remember { mutableDoubleStateOf(0.0) }
        var latitude by remember { mutableDoubleStateOf(0.0) }
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    longitude = location.longitude
                    latitude = location.latitude
                }
            }
        }
        DateColumn(latitude, longitude, modifier)
    }

    @Composable
    private fun DateColumn(
        latitude: Double,
        longitude: Double,
        modifier: Modifier
    ) {
        Column {
//            Text(
//                text = formatLatitude(latitude) + " " + formatLongitude(longitude),
//                modifier= Modifier.height(12.dp).fillMaxWidth()
//            )
            LazyColumn(modifier = modifier) {
                items(count = 365) { i ->
                    if (i == 0) {
                        Text(text = formatLatitude(latitude) + " " + formatLongitude(longitude))
                    } else {
                        Date(
                            date = ZonedDateTime.now().withHour(12).plusDays(i.toLong()-1),
                            latitude = latitude,
                            longitude = longitude
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun Date(
        date: ZonedDateTime,
        latitude: Number,
        longitude: Number,
        modifier: Modifier = Modifier
    ) {
        val yesterday =
            calculateSunriseDetails(
                date.minusDays(1L),
                Angle.fromDegrees(longitude),
                Angle.fromDegrees(latitude)
            )
        val currentDay =
            calculateSunriseDetails(date, Angle.fromDegrees(longitude), Angle.fromDegrees(latitude))
        val tomorrow =
            calculateSunriseDetails(
                date.plusDays(1L),
                Angle.fromDegrees(longitude),
                Angle.fromDegrees(latitude)
            )

        val sunrise =
            when (currentDay.sunriseType) {
                DaylightType.MIDNIGHT_SUN -> "\u2600\ufe0f" + formatShortDate(currentDay.sunriseTime)
                DaylightType.POLAR_NIGHT -> "\u2b1b" + formatShortDate(currentDay.sunsetTime)
                else -> "\uD83C\uDF05" + formatTime(currentDay.sunriseTime)
            }
        val sunset =
            MoonPhase.of(currentDay.moonPhase).icon + when (currentDay.sunsetType) {
                DaylightType.MIDNIGHT_SUN -> formatShortDate(currentDay.sunsetTime)
                DaylightType.POLAR_NIGHT -> formatShortDate(currentDay.sunriseTime)
                else -> formatTime(currentDay.sunsetTime)
            }

        val wake = formatTime(wakeCalculation(yesterday.sunsetTime,
            currentDay.sunriseTime, currentDay.sunriseType))
        val sleep = formatTime(sleepCalculation(currentDay.sunsetTime, currentDay.sunsetType, tomorrow.sunriseTime))

        Row(modifier
            .padding(0.dp, 4.dp)
            .fillMaxWidth()) {
            Column(modifier.weight(0.2f)) {
                Text(
                    text = sunrise,
                    softWrap = false,
                    modifier = modifier
                )
                Text(
                    text = "\u23f0" + wake,
                    softWrap = false,
                    modifier = modifier
                )
            }
            Text(
                text = formatLongDate(currentDay.solarNoonTime),
                minLines = 2,
                textAlign = TextAlign.Center,
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .weight(0.6f)
            )
            Column(modifier.weight(0.2f)) {
                Text(
                    text = sunset,
                    softWrap = false,
                    modifier = modifier
                )
                Text(
                    text = "\uD83D\uDCA4" + sleep,
                    softWrap = false,
                    modifier = modifier
                )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun ColumnPreview() {
        MainActivityTheme {
            DateColumn(
                52.61,
                -1.92,
                Modifier.border(BorderStroke(1.dp, Color.Red))
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun HomePreview() {
        MainActivityTheme {
            Date(
                ZonedDateTime.now(),
                52.61,
                -1.92,
                Modifier.border(BorderStroke(1.dp, Color.Red))
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun ArcticPreview() {
        MainActivityTheme {
            Date(ZonedDateTime.now(), 85, -1.92)
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun AntarcticPreview() {
        MainActivityTheme {
            Date(ZonedDateTime.now(), -85, -1.92)
        }
    }
}