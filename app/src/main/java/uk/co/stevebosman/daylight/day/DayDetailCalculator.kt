package uk.co.stevebosman.daylight.day

import android.util.Log
import uk.co.stevebosman.daylight.Preferences
import uk.co.stevebosman.sunrise.DaylightType
import uk.co.stevebosman.sunrise.SunriseDetails
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class DayDetailCalculator(val preferences: Preferences) {
    fun calculate(yesterday: SunriseDetails,
                  today: SunriseDetails,
                  tomorrow: SunriseDetails
    ): DayDetails {
        Log.i("Daylight", "*****")
        Log.i("Daylight", "Noon: ${today.solarNoonTime}")
        Log.i("Daylight", "Sunrise: ${today.sunriseTime}")
        Log.i("Daylight", "Sunset: ${today.sunsetTime}")

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
        return DayDetails(today, wakeUp, sleep)
    }

    private fun earliest(date1: ZonedDateTime, date2: ZonedDateTime): ZonedDateTime {
        return if (date1.isBefore(date2)) date1 else date2
    }

    private fun calculateEarliestSleepTime(
        sunriseDetails: SunriseDetails,
        preferences: Preferences
    ): ZonedDateTime {
        val earliestSleepTime =
            sunriseDetails.solarNoonTime.withHour(preferences.earliestSleepTimeHours)
                .withMinute(preferences.earliestSleepTimeMinutes).truncatedTo(ChronoUnit.MINUTES)
        return if (sunriseDetails.sunsetTime.isAfter(earliestSleepTime)) sunriseDetails.sunsetTime else earliestSleepTime
    }

    private fun calculateLatestWakeupTime(
        sunriseDetails: SunriseDetails,
        preferences: Preferences
    ) = sunriseDetails.solarNoonTime.withHour(preferences.latestWakeupTimeHours)
        .withMinute(preferences.latestWakeupTimeMinutes).truncatedTo(ChronoUnit.MINUTES)
}