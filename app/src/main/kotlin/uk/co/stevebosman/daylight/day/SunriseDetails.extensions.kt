package uk.co.stevebosman.daylight.day

import uk.co.stevebosman.daylight.PreferenceValues
import uk.co.stevebosman.sunrise.DaylightType
import uk.co.stevebosman.sunrise.SunriseDetails
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

fun SunriseDetails.latestWakeUpTime(
    preferences: PreferenceValues
): ZonedDateTime =
    this.solarNoonTime
        .withHour(preferences.latestWakeupTimeHours)
        .withMinute(preferences.latestWakeupTimeMinutes)
        .truncatedTo(ChronoUnit.MINUTES)

fun SunriseDetails.idealWakeUpTime(
    preferences: PreferenceValues
): ZonedDateTime {
    val latestWakeupTime = this.latestWakeUpTime(preferences)
    return if (this.sunriseTime.isBefore(latestWakeupTime) && this.sunriseType == DaylightType.NORMAL)
        this.sunriseTime
    else
        latestWakeupTime
}

fun SunriseDetails.earliestSleepTime(
    preferences: PreferenceValues
): ZonedDateTime {
    val earliestSleepTime = this.solarNoonTime
        .withHour(preferences.earliestSleepTimeHours)
        .withMinute(preferences.earliestSleepTimeMinutes)
        .truncatedTo(ChronoUnit.MINUTES)
    return if (this.sunsetTime.isAfter(earliestSleepTime) && this.sunriseType == DaylightType.NORMAL)
        this.sunsetTime
    else
        earliestSleepTime
}
