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
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import uk.co.stevebosman.angles.Angle
import uk.co.stevebosman.daylight.databinding.FragmentFirstBinding
import uk.co.stevebosman.sunrise.DaylightType
import uk.co.stevebosman.sunrise.SunriseDetails
import uk.co.stevebosman.sunrise.calculateSunriseDetails
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private val permissionId: Int = 15169
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

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireContext())

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        getLastLocation()
    }

    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this.requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this.requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                mFusedLocationClient.lastLocation.addOnCompleteListener(this.requireActivity()) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        Log.i("Daylight", "$location")
                        setSunriseSunset(location)
                    }
                }
            } else {
                Toast.makeText(this.requireContext(), "Turn on location", Toast.LENGTH_LONG).show()
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
        val mLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setWaitForAccurateLocation(true).build()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this.requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            permissionId
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            this.requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun setSunriseSunset(location: Location?) {
        val latitude: Angle = Angle.fromDegrees(location?.latitude ?: 0)
        val longitude: Angle = Angle.fromDegrees(location?.longitude ?: 51.4769)
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

        val preferences = Preferences(this.requireContext())
        populateSunriseSunset(
            sunriseDetails[0],
            sunriseDetails[1],
            sunriseDetails[2],
            preferences,
            binding.daylight1
        )
        populateSunriseSunset(
            sunriseDetails[1],
            sunriseDetails[2],
            sunriseDetails[3],
            preferences,
            binding.daylight2
        )
        populateSunriseSunset(
            sunriseDetails[2],
            sunriseDetails[3],
            sunriseDetails[4],
            preferences,
            binding.daylight3
        )
        populateSunriseSunset(
            sunriseDetails[3],
            sunriseDetails[4],
            sunriseDetails[5],
            preferences,
            binding.daylight4
        )
        populateSunriseSunset(
            sunriseDetails[4],
            sunriseDetails[5],
            sunriseDetails[6],
            preferences,
            binding.daylight5
        )
        populateSunriseSunset(
            sunriseDetails[5],
            sunriseDetails[6],
            sunriseDetails[7],
            preferences,
            binding.daylight6
        )
        populateSunriseSunset(
            sunriseDetails[6],
            sunriseDetails[7],
            sunriseDetails[8],
            preferences,
            binding.daylight7
        )
    }

    private val alarmClockIcon = "\u23f0"
    private val sleepIcon = "\uD83D\uDCA4"
    private val sunriseIcon = "\uD83C\uDF05"
    private val newMoon = "\uD83C\uDF11"
    private val crescentMoon = "\uD83C\uDF12"
    private val quarterMoon = "\uD83C\uDF13"
    private val gibbousMoon = "\uD83C\uDF14"
    private val fullMoon = "\uD83C\uDF15"
    private val waxingGibbousMoon = "\uD83C\uDF16"
    private val lastQuarterMoon = "\uD83C\uDF17"
    private val waxingCrescentMoon = "\uD83C\uDF18"
    private val moonPhaseIcons = arrayOf(newMoon, crescentMoon, quarterMoon, gibbousMoon, fullMoon, waxingGibbousMoon, lastQuarterMoon, waxingCrescentMoon)

    private fun populateSunriseSunset(
        yesterday: SunriseDetails,
        today: SunriseDetails,
        tomorrow: SunriseDetails,
        preferences: Preferences,
        daylight: DaylightView
    ) {
        daylight.date =
            today.solarNoonTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
//        println("*****")
//        println("Noon: ${today.solarNoonTime}")
//        println("Sunrise: ${today.sunriseTime}")
//        println("Sunset: ${today.sunsetTime}")

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
                else -> sunriseIcon + formatTime(today.sunriseTime)
            } + "\n" + alarmClockIcon + formatTime(wakeUp)

        daylight.sunset =
            when(today.daylightType) {
                DaylightType.MIDNIGHT_SUN -> getString(R.string.midnight_sun)
                DaylightType.POLAR_NIGHT -> getString(R.string.polar_night)
                else -> moonPhaseIcons[(today.moonPhase * 8+0.5).toInt()%8] + formatTime(today.sunsetTime)
            } + "\n" + sleepIcon + formatTime(sleep)
    }

    private fun formatTime(date: ZonedDateTime): String {
        return date.toLocalTime().plusSeconds(30).truncatedTo(ChronoUnit.MINUTES).format(
            DateTimeFormatter.ofPattern("HH:mm"))
    }

    private fun earliest(date1: ZonedDateTime, date2: ZonedDateTime): ZonedDateTime {
        return if (date1.isBefore(date2)) date1 else date2
    }

    private fun calculateEarliestSleepTime(
        sunriseDetails: SunriseDetails,
        preferences: Preferences
    ): ZonedDateTime {
        val earliestSleepTime = sunriseDetails.solarNoonTime.withHour(preferences.earliestSleepTimeHours)
            .withMinute(preferences.earliestSleepTimeMinutes).truncatedTo(ChronoUnit.MINUTES)
        return if (sunriseDetails.sunsetTime.isAfter(earliestSleepTime)) sunriseDetails.sunsetTime else earliestSleepTime
    }

    private fun calculateLatestWakeupTime(
        sunriseDetails: SunriseDetails,
        preferences: Preferences
    ) = sunriseDetails.solarNoonTime.withHour(preferences.latestWakeupTimeHours)
        .withMinute(preferences.latestWakeupTimeMinutes).truncatedTo(ChronoUnit.MINUTES)
}
