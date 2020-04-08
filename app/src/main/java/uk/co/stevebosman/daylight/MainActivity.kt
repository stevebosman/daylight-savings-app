package uk.co.stevebosman.daylight

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_first.*
import uk.co.stevebosman.angles.Angle
import uk.co.stevebosman.sunrise.DaylightType
import uk.co.stevebosman.sunrise.SunriseDetails
import uk.co.stevebosman.sunrise.calculateSunriseDetails
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit


class MainActivity : AppCompatActivity() {
    private val permissionId: Int = 15169
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { _ ->
            requestNewLocationData()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }

    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        Log.i("Daylight", "$location")
                        setSunriseSunset(location)
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.i("Daylight", "${locationResult.lastLocation}")
            setSunriseSunset(locationResult.lastLocation)
        }
    }

    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            permissionId
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            @Suppress("ControlFlowWithEmptyBody")
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Granted. Start getting the location information
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                showSettings()
                true
            }
            R.id.action_about -> {
                showAbout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val settingsActivity = 123

    fun showSettings() {
        startActivityForResult(Intent(this, SettingsActivity::class.java), settingsActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == settingsActivity) {
            getLastLocation()
        }
    }

    fun showAbout() {
        startActivity(Intent(this, AboutActivity::class.java))
    }

    private fun setSunriseSunset(location: Location) {
        val latitude: Angle = Angle.fromDegrees(location.latitude)
        val longitude: Angle = Angle.fromDegrees(location.longitude)
        val today = ZonedDateTime.now()

        val sunriseDetails = arrayListOf<SunriseDetails>()
        for (i in -1..7) {
            sunriseDetails.add(
                calculateSunriseDetails(
                    today.plusDays(i.toLong()),
                    longitude,
                    latitude
                )
            );
        }

        val preferences = Preferences(this)
        populateSunriseSunset(
            sunriseDetails.get(0),
            sunriseDetails.get(1),
            sunriseDetails.get(2),
            preferences,
            daylight1
        )
        populateSunriseSunset(
            sunriseDetails.get(1),
            sunriseDetails.get(2),
            sunriseDetails.get(3),
            preferences,
            daylight2
        )
        populateSunriseSunset(
            sunriseDetails.get(2),
            sunriseDetails.get(3),
            sunriseDetails.get(4),
            preferences,
            daylight3
        )
        populateSunriseSunset(
            sunriseDetails.get(3),
            sunriseDetails.get(4),
            sunriseDetails.get(5),
            preferences,
            daylight4
        )
        populateSunriseSunset(
            sunriseDetails.get(4),
            sunriseDetails.get(5),
            sunriseDetails.get(6),
            preferences,
            daylight5
        )
        populateSunriseSunset(
            sunriseDetails.get(5),
            sunriseDetails.get(6),
            sunriseDetails.get(7),
            preferences,
            daylight6
        )
        populateSunriseSunset(
            sunriseDetails.get(6),
            sunriseDetails.get(7),
            sunriseDetails.get(8),
            preferences,
            daylight7
        )
    }

    private val sun = "☀"
    private val newMoon = "\uD83C\uDF11"
    private val crescentMoon = "\uD83C\uDF12"
    private val quarterMoon = "\uD83C\uDF13"
    private val gibbousMoon = "\uD83C\uDF14"
    private val fullMoon = "\uD83C\uDF15"
    private val waxingGibbousMoon = "\uD83C\uDF16"
    private val lastQuarterMoon = "\uD83C\uDF17"
    private val waxingCrescentMoon = "\uD83C\uDF18"

    private fun populateSunriseSunset(
        yesterday: SunriseDetails,
        today: SunriseDetails,
        tomorrow: SunriseDetails,
        preferences: Preferences,
        daylight: DaylightView
    ) {
        daylight.date =
            today.solarNoonTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        println("*****")
        println("Noon: ${today.solarNoonTime}")
        println("Sunrise: ${today.sunriseTime}")
        println("Sunset: ${today.sunsetTime}")

        val earliestSleepTimeYesterday = calculateEarliestSleepTime(yesterday, preferences)
        val latestWakeUpTimeToday = calculateLatestWakeupTime(today, preferences)
        val earliestSleepTimeToday = calculateEarliestSleepTime(today, preferences)
        val latestWakeUpTimeTomorrow = calculateLatestWakeupTime(tomorrow, preferences)
        var wakeUp: ZonedDateTime
        var sleep: ZonedDateTime
        if (today.daylightType == DaylightType.POLAR_NIGHT || today.daylightType == DaylightType.MIDNIGHT_SUN) {
            wakeUp = latestWakeUpTimeToday
            sleep = earliestSleepTimeToday
        } else {
            wakeUp = today.sunriseTime
            // is sunrise too close to the earliest sleep time?
            if (ChronoUnit.MINUTES.between(
                    earliestSleepTimeYesterday,
                    wakeUp
                ) < preferences.sleepDurationMinutes
            ) {
                wakeUp = earliestSleepTimeYesterday.plusMinutes(preferences.sleepDurationMinutes)
            }

            sleep = today.sunsetTime
            val tomorrowsWakeUp = earliest(latestWakeUpTimeTomorrow, tomorrow.sunriseTime)
            if (ChronoUnit.MINUTES.between(
                    sleep,
                    tomorrowsWakeUp
                ) > preferences.sleepDurationMinutes
            ) {
                sleep = tomorrowsWakeUp.minusMinutes(preferences.sleepDurationMinutes)
            }
        }
        if (wakeUp.isAfter(latestWakeUpTimeToday)) {
            wakeUp = latestWakeUpTimeToday
        }
        if (sleep.isBefore(earliestSleepTimeToday)) {
            sleep = earliestSleepTimeToday
        }
        daylight.sunrise =
            when(today.daylightType) {
                DaylightType.MIDNIGHT_SUN -> getString(R.string.midnight_sun)
                DaylightType.POLAR_NIGHT -> getString(R.string.polar_night)
                else -> sun + today.sunriseTime.format(DateTimeFormatter.ISO_LOCAL_TIME)
            } + "\n" + wakeUp.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME)
        daylight.sunset =
            when(today.daylightType) {
                DaylightType.MIDNIGHT_SUN -> getString(R.string.midnight_sun)
                DaylightType.POLAR_NIGHT -> getString(R.string.polar_night)
                else -> crescentMoon + today.sunsetTime.format(DateTimeFormatter.ISO_LOCAL_TIME)
            } + "\n" + sleep.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME)
    }

    private fun earliest(date1: ZonedDateTime, date2: ZonedDateTime): ZonedDateTime {
        if (date1.isBefore(date2)) return date1 else return date2
    }

    private fun calculateEarliestSleepTime(
        today: SunriseDetails,
        preferences: Preferences
    ) = today.solarNoonTime.withHour(preferences.earliestSleepTimeHours)
        .withMinute(preferences.earliestSleepTimeMinutes).truncatedTo(ChronoUnit.MINUTES)

    private fun calculateLatestWakeupTime(
        today: SunriseDetails,
        preferences: Preferences
    ) = today.solarNoonTime.withHour(preferences.latestWakeupTimeHours)
        .withMinute(preferences.latestWakeupTimeMinutes).truncatedTo(ChronoUnit.MINUTES)
}
