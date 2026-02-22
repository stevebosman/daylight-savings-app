package uk.co.stevebosman.daylight.activity

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import uk.co.stevebosman.daylight.SleepNotification
import uk.co.stevebosman.daylight.activity.ui.theme.MainActivityTheme
import uk.co.stevebosman.daylight.angles.Angle
import uk.co.stevebosman.daylight.channelID
import uk.co.stevebosman.daylight.formatLatitude
import uk.co.stevebosman.daylight.formatLongDate
import uk.co.stevebosman.daylight.formatLongitude
import uk.co.stevebosman.daylight.formatShortDate
import uk.co.stevebosman.daylight.formatTime
import uk.co.stevebosman.daylight.messageExtra
import uk.co.stevebosman.daylight.moon.MoonPhase
import uk.co.stevebosman.daylight.sleepCalculation
import uk.co.stevebosman.daylight.sunrise.DaylightType
import uk.co.stevebosman.daylight.sunrise.calculateSunriseDetails
import uk.co.stevebosman.daylight.titleExtra
import uk.co.stevebosman.daylight.wakeCalculation
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLocationPermissions()
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
                    Ui(Modifier.padding(innerPadding).padding(horizontal = 16.dp))
                }
            }
        }
        createNotificationChannel(this)
    }

    @Composable
    fun Ui(modifier: Modifier = Modifier) {
        var longitude by remember { mutableDoubleStateOf(0.78667) }
        var latitude by remember { mutableDoubleStateOf(51.46778) }
        var name by remember { mutableStateOf("Essex") }
        Geocoder(this).getFromLocation(latitude, longitude, 1) {
            addresses ->
                Log.d("Daylight", "Ui: ${addresses.get(0)}")
                val address = addresses.get(0).getAddressLine(0) ?: ""
                if (address.contains(',')) {
                    name = address.substring(address.indexOf(',')+1).trim()
                } else {
                    name = address
                }
        }
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    longitude = location.longitude
                    latitude = location.latitude
                }
            }
        }
        Location(name, latitude, longitude, modifier.height(30.dp))
        DatesColumn(latitude, longitude, modifier.offset(y=30.dp))
    }

    @Composable
    fun Location(name:String, latitude: Double, longitude: Double, modifier: Modifier = Modifier) {
        Text(text = "$name (${formatLatitude(latitude)} ${formatLongitude(longitude)})", modifier)
    }

    @Composable
    private fun DatesColumn(
        latitude: Double,
        longitude: Double,
        modifier: Modifier
    ) {
        Column {
            LazyColumn(modifier = modifier) {
                items(count = 365) { i ->
                        Date(
                            offset = i.toLong(),
                            latitude = latitude,
                            longitude = longitude
                        )
                }
            }
        }
    }

    @Composable
    fun Date(
        offset: Long,
        latitude: Number,
        longitude: Number,
        modifier: Modifier = Modifier
    ) {
        if (offset < 30 && checkNotificationPermissions(this)) {
            scheduleNotification(offset)
        }
        val date = ZonedDateTime.now().withHour(12).plusDays(offset)
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

        val wake = formatTime(
            wakeCalculation(
                yesterday.sunsetTime,
                currentDay.sunriseTime, currentDay.sunriseType
            )
        )
        val sleepTime =
            sleepCalculation(currentDay.sunsetTime, currentDay.sunsetType, tomorrow.sunriseTime)
        val sleep = formatTime(sleepTime)

        Row(
            modifier
                .padding(0.dp, 4.dp)
                .fillMaxWidth()
        ) {
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
            DatesColumn(
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
                0,
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
            Date(0, 85, -1.92)
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun AntarcticPreview() {
        MainActivityTheme {
            Date(0, -85, -1.92)
        }
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

    private fun scheduleNotification(offset: Long) {
        var longitude = 0.78667
        var latitude = 51.46778
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    longitude = location.longitude
                    latitude = location.latitude
                }
            }
        }

        val date = ZonedDateTime.now().withHour(12).plusDays(offset)
        val id = date.year * 1000 + date.dayOfYear

        val currentDay =
            calculateSunriseDetails(date, Angle.fromDegrees(longitude), Angle.fromDegrees(latitude))
        val tomorrow =
            calculateSunriseDetails(
                date.plusDays(1L),
                Angle.fromDegrees(longitude),
                Angle.fromDegrees(latitude)
            )

        val sleepTime =
            sleepCalculation(currentDay.sunsetTime, currentDay.sunsetType, tomorrow.sunriseTime)
        if (sleepTime.isAfter(ZonedDateTime.now())) {
            Log.d("Daylight", "scheduling notification $id for $sleepTime")
            // Create an intent for the Notification BroadcastReceiver
            val intent = Intent(applicationContext, SleepNotification::class.java)

            // Extract title and message from user input
            val title = "Continuous DST"
            val message = "${formatTime(sleepTime)} Boing! Time for bed"

            // Add title and message as extras to the intent
            intent.putExtra(titleExtra, title)
            intent.putExtra(messageExtra, message)

            // Create a PendingIntent for the broadcast
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                id,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Get the AlarmManager service
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

            // Get the selected time and schedule the notification
            Log.d("Daylight", "setting alert manager to wakeup at $sleepTime")
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                sleepTime.toEpochSecond() * 1000,
                pendingIntent
            )
        }
    }
}

private fun createNotificationChannel(context: Context) {
    val name = "Continuous DST Notification Channel"
    val desc = "Notifies user of suggested sleep time"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(channelID, name, importance)
    channel.description = desc

    // Get the NotificationManager service and create the channel
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

fun checkNotificationPermissions(context: Context): Boolean {
    // Check if notification permissions are granted
    val notificationManager =
        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    val isEnabled = notificationManager.areNotificationsEnabled()

    if (!isEnabled) {
        Log.d("Daylight", "notifications not enabled - ask to enable")
        // Open the app notification settings if notifications are not enabled
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        context.startActivity(intent)

        return false
    }

    Log.d("Daylight", "notifications enabled")
    // Permissions are granted
    return true
}
