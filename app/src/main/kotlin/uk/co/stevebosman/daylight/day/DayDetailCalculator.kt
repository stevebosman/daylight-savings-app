package uk.co.stevebosman.daylight.day

import uk.co.stevebosman.daylight.PreferenceValues
import uk.co.stevebosman.sunrise.SunriseDetails
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class DayDetailCalculator(private val preferences: PreferenceValues) {
    fun calculate(days: Array<SunriseDetails>) =
        Array(days.size - 2) { i -> calculate(days[i], days[i + 1], days[i + 2]) }

    private fun calculate(
        yesterday: SunriseDetails,
        today: SunriseDetails,
        tomorrow: SunriseDetails
    ): DayDetails {
        val idealSleepTimeYesterday = yesterday.earliestSleepTime(preferences)
        val latestWakeUpTimeToday = today.latestWakeUpTime(preferences)
        val earliestSleepTimeToday = today.earliestSleepTime(preferences)
        val idealWakeUpTimeTomorrow = tomorrow.idealWakeUpTime(preferences)

        var wakeUp: ZonedDateTime = today.idealWakeUpTime(preferences)
        if (ChronoUnit.MINUTES.between(
                idealSleepTimeYesterday,
                wakeUp
            ) < preferences.sleepDurationMinutes
        ) {
            wakeUp = idealSleepTimeYesterday.plusMinutes(preferences.sleepDurationMinutes)
        }
        if (wakeUp.isAfter(latestWakeUpTimeToday)) {
            // can be triggered if sleep duration is insufficient between earliest sleep and latest wake
            wakeUp = latestWakeUpTimeToday
        }

        var sleep: ZonedDateTime = earliestSleepTimeToday
        if (ChronoUnit.MINUTES.between(
                sleep,
                idealWakeUpTimeTomorrow
            ) > preferences.sleepDurationMinutes
        ) {
            sleep = idealWakeUpTimeTomorrow.minusMinutes(preferences.sleepDurationMinutes)
        }
        if (sleep.isBefore(earliestSleepTimeToday)) {
            // can be triggered if sleep duration is insufficient between earliest sleep and latest wake
            sleep = earliestSleepTimeToday
        }
        return DayDetails(today, wakeUp, sleep)
    }
}