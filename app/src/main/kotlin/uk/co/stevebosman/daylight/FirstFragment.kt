package uk.co.stevebosman.daylight

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import uk.co.stevebosman.angles.Angle
import uk.co.stevebosman.daylight.databinding.FragmentFirstBinding
import uk.co.stevebosman.daylight.day.DayDetailCalculator
import uk.co.stevebosman.daylight.day.DayDetails
import uk.co.stevebosman.daylight.day.MoonPhase
import uk.co.stevebosman.sunrise.DaylightType
import uk.co.stevebosman.sunrise.calculateSunriseDetails
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

private const val REFRESH_INTERVAL: Long = 5 * 60 * 1000

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private var locationPermitted: Boolean = true
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var daylightsViews: ArrayList<DaylightView> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("Daylight", "onCreateView")
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        val view = binding.root

        daylightsViews = arrayListOf(
            binding.daylight1,
            binding.daylight2,
            binding.daylight3,
            binding.daylight4,
            binding.daylight5,
            binding.daylight6,
            binding.daylight7,
            binding.daylight8,
            binding.daylight9
        )

        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this.requireContext())

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        Log.i("Daylight", "onResume")
        super.onResume()
        if (locationPermitted) {
            getLastLocation()
        } else {
            setSunriseSunset(null)
        }
    }

    private fun getLastLocation() {
        Log.i("Daylight", "getLastLocation")
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("Daylight", "insufficient permissions")
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            Log.i("Daylight", "sufficient permissions")
            if (isLocationEnabled()) {
                Log.i("Daylight", "location enabled")
                mFusedLocationClient.lastLocation.addOnCompleteListener(this.requireActivity()) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        Log.i("Daylight", "requestNewLocationData")
                        val mLocationRequest =
                            LocationRequest.Builder(Priority.PRIORITY_PASSIVE, REFRESH_INTERVAL)
                                .setWaitForAccurateLocation(true).build()
                        mFusedLocationClient.requestLocationUpdates(
                            mLocationRequest, mLocationCallback,
                            Looper.myLooper()
                        )
                    } else {
                        Log.i("Daylight", "$location")
                        setSunriseSunset(location)
                    }
                }
            } else {
                Log.i("Daylight", "location disabled")
                Toast.makeText(this.requireContext(), "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i(tag, "Location permission agreed")
            getLastLocation()
        } else {
            Log.i(tag, "Location permission not agreed")
            locationPermitted = false
            setSunriseSunset(null)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            this.requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.i("Daylight", "${locationResult.lastLocation}")
            setSunriseSunset(locationResult.lastLocation)
        }
    }

    private fun setSunriseSunset(location: Location?) {
        Log.i("location", "location ${location}")
        val today = ZonedDateTime.now()
        val latitude: Angle = Angle.fromDegrees(location?.latitude ?: 0)
        val longitude: Angle = Angle.fromDegrees(location?.longitude ?: 0)

//        val today = ZonedDateTime.of(2023,3, 25,12,0,0,0, ZoneId.of("Europe/London"))
//        val latitude: Angle = Angle.fromDegrees(50)
//        val longitude: Angle = Angle.fromDegrees(0)

//        val today = ZonedDateTime.of(2023,3, 25,12,0,0,0, ZoneId.of("America/Los_Angeles"))
//        val latitude: Angle = Angle.fromDegrees(-82)
//        val longitude: Angle = Angle.fromDegrees(45)

        val sunriseDetails = Array(daylightsViews.size + 2) { i ->
            calculateSunriseDetails(
                today.plusDays((i - 1).toLong()),
                longitude,
                latitude
            )
        }

        val dayDetailCalculator = DayDetailCalculator(Preferences(this.requireContext()))
        val dayDetails = dayDetailCalculator.calculate(sunriseDetails)

        daylightsViews.forEachIndexed { i, v ->
            val day = dayDetails[i].day
            Log.i("Daylight", "*".repeat(10))
            Log.i("Daylight", "Noon: ${day.solarNoonTime}")
            Log.i("Daylight", "Sunrise: ${day.sunriseTime}")
            Log.i("Daylight", "Sunset: ${day.sunsetTime}")
            populateDaylightView(v, dayDetails[i])
        }
    }

    private val alarmClockIcon = "\u23f0"
    private val sleepIcon = "\uD83D\uDCA4"
    private val sunriseIcon = "\uD83C\uDF05"
    private val midnightSunIcon = "\u2600\ufe0f"
    private val polarNightIcon = "\u2b1b"

    private fun populateDaylightView(
        daylight: DaylightView,
        dayDetails: DayDetails
    ) {
        val currentDay = dayDetails.day
        daylight.date =
            currentDay.solarNoonTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))

        val timeSeparator =
            if (requireContext().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) " " else "\n"

        daylight.sunrise =
            when (currentDay.sunriseType) {
                DaylightType.MIDNIGHT_SUN -> midnightSunIcon + formatDate(currentDay.sunriseTime)
                DaylightType.POLAR_NIGHT -> polarNightIcon + formatDate(currentDay.sunsetTime)
                else -> sunriseIcon + formatTime(currentDay.sunriseTime)
            } + timeSeparator + alarmClockIcon + formatTime(dayDetails.wakeUp)

        daylight.sunset =
            MoonPhase.of(currentDay.moonPhase).icon + when (currentDay.sunsetType) {
                DaylightType.MIDNIGHT_SUN -> formatDate(currentDay.sunsetTime)
                DaylightType.POLAR_NIGHT -> formatDate(currentDay.sunriseTime)
                else -> formatTime(currentDay.sunsetTime)
            } + timeSeparator + sleepIcon + formatTime(dayDetails.sleep)
    }

    private fun formatDate(date: ZonedDateTime): String = date.toLocalDate().format(
        DateTimeFormatter.ofPattern("dd-MMM")
    )

    private fun formatTime(date: ZonedDateTime): String =
        date.toLocalTime().plusSeconds(30).truncatedTo(ChronoUnit.MINUTES).format(
            DateTimeFormatter.ofPattern("HH:mm")
        )
}
