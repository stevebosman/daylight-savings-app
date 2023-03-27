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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import uk.co.stevebosman.angles.Angle
import uk.co.stevebosman.daylight.databinding.FragmentFirstBinding
import uk.co.stevebosman.daylight.day.DayDetailCalculator
import uk.co.stevebosman.daylight.day.DayDetails
import uk.co.stevebosman.daylight.day.MoonPhase
import uk.co.stevebosman.sunrise.DaylightType
import uk.co.stevebosman.sunrise.SunriseDetails
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        val view = binding.root

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

        val latitude: Angle
        val longitude: Angle
        if (location == null) {
            latitude = Angle.fromDegrees(0)
            longitude = Angle.fromDegrees(0)
        } else {
            latitude = Angle.fromDegrees(location.latitude)
            longitude = Angle.fromDegrees(location.longitude)
        }

        val today = ZonedDateTime.now()

        val sunriseDetails = arrayListOf<SunriseDetails>()
        for (i in -1..7) {
            sunriseDetails.add(
                calculateSunriseDetails(
                    today.plusDays(i.toLong()),
                    longitude,
                    latitude
                )
            )
        }

        val dayDetailCalculator = DayDetailCalculator(Preferences(this.requireContext()))
        populateDaylightView(
            binding.daylight1,
            dayDetailCalculator.calculate(sunriseDetails[0], sunriseDetails[1], sunriseDetails[2])
        )
        populateDaylightView(
            binding.daylight2,
            dayDetailCalculator.calculate(sunriseDetails[1], sunriseDetails[2], sunriseDetails[3])
        )
        populateDaylightView(
            binding.daylight3,
            dayDetailCalculator.calculate(sunriseDetails[2], sunriseDetails[3], sunriseDetails[4])
        )
        populateDaylightView(
            binding.daylight4,
            dayDetailCalculator.calculate(sunriseDetails[3], sunriseDetails[4], sunriseDetails[5])
        )
        populateDaylightView(
            binding.daylight5,
            dayDetailCalculator.calculate(sunriseDetails[4], sunriseDetails[5], sunriseDetails[6])
        )
        populateDaylightView(
            binding.daylight6,
            dayDetailCalculator.calculate(sunriseDetails[5], sunriseDetails[6], sunriseDetails[7])
        )
        populateDaylightView(
            binding.daylight7,
            dayDetailCalculator.calculate(sunriseDetails[6], sunriseDetails[7], sunriseDetails[8])
        )
    }

    private val alarmClockIcon = "\u23f0"
    private val sleepIcon = "\uD83D\uDCA4"
    private val sunriseIcon = "\uD83C\uDF05"

    private fun populateDaylightView(
        daylight: DaylightView,
        dayDetails: DayDetails
    ) {
        val currentDay = dayDetails.day
        daylight.date =
            currentDay.solarNoonTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))

        daylight.sunrise =
            when (currentDay.daylightType) {
                DaylightType.MIDNIGHT_SUN -> getString(R.string.midnight_sun)
                DaylightType.POLAR_NIGHT -> getString(R.string.polar_night)
                else -> sunriseIcon + formatTime(currentDay.sunriseTime)
            } + "\n" + alarmClockIcon + formatTime(dayDetails.wakeUp)

        daylight.sunset =
            when (currentDay.daylightType) {
                DaylightType.MIDNIGHT_SUN -> getString(R.string.midnight_sun)
                DaylightType.POLAR_NIGHT -> getString(R.string.polar_night)
                else -> MoonPhase.getIcon(currentDay) + formatTime(currentDay.sunsetTime)
            } + "\n" + sleepIcon + formatTime(dayDetails.sleep)
    }

    private fun formatTime(date: ZonedDateTime): String {
        return date.toLocalTime().plusSeconds(30).truncatedTo(ChronoUnit.MINUTES).format(
            DateTimeFormatter.ofPattern("HH:mm")
        )
    }
}
