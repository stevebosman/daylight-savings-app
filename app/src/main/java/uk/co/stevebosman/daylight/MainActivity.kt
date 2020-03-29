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
import uk.co.stevebosman.sunrise.calculateSunriseDetails
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


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

    fun showSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    fun showAbout() {
        startActivity(Intent(this, AboutActivity::class.java))
    }

    private fun setSunriseSunset(location: Location) {
        val latitude: Angle = Angle.fromDegrees(location.latitude)
        val longitude: Angle = Angle.fromDegrees(location.longitude)
        val today = ZonedDateTime.now()
        populateSunriseSunset(today, longitude, latitude, daylight1)
        populateSunriseSunset(today.plusDays(1), longitude, latitude, daylight2)
        populateSunriseSunset(today.plusDays(2), longitude, latitude, daylight3)
        populateSunriseSunset(today.plusDays(3), longitude, latitude, daylight4)
        populateSunriseSunset(today.plusDays(4), longitude, latitude, daylight5)
        populateSunriseSunset(today.plusDays(5), longitude, latitude, daylight6)
        populateSunriseSunset(today.plusDays(6), longitude, latitude, daylight7)
    }

    private fun populateSunriseSunset(
        time: ZonedDateTime,
        longitude: Angle,
        latitude: Angle,
        daylight: DaylightView
    ) {
        daylight.date = time.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        val sunriseDetails =
            calculateSunriseDetails(time, longitude, latitude)
        println("*****")
        println("Noon: ${sunriseDetails.solarNoonTime}")
        println("Sunrise: ${sunriseDetails.sunriseTime}")
        println("Sunset: ${sunriseDetails.sunsetTime}")

        if (sunriseDetails.daylightType==DaylightType.POLAR_NIGHT) {
            daylight.sunset = getString(R.string.polar_night)
            daylight.sunrise = getString(R.string.polar_night)
        } else if (sunriseDetails.daylightType==DaylightType.MIDNIGHT_SUN) {
            daylight.sunset = getString(R.string.midnight_sun)
            daylight.sunrise = getString(R.string.midnight_sun)
        } else {
            daylight.sunset = sunriseDetails.sunsetTime.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME)
            daylight.sunrise = sunriseDetails.sunriseTime.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME)
        }
    }
}
