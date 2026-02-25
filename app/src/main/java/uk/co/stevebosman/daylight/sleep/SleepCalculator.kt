package uk.co.stevebosman.daylight.sleep

import uk.co.stevebosman.daylight.sunrise.DaylightType
import java.time.ZonedDateTime

val idealSleepMinutes = (7.5 * 60).toLong()

fun defaultWake(date: ZonedDateTime) = date.withHour(6).withMinute(30).withSecond(0).withNano(0)
fun defaultSleep(date: ZonedDateTime) = date.withHour(23).withMinute(0).withSecond(0).withNano(0)

fun wakeCalculation(
    yesterdaySunset: ZonedDateTime,
    todaySunrise: ZonedDateTime, todaySunriseType: DaylightType
): ZonedDateTime {
    val defaultWake = defaultWake(todaySunrise)
    return when (todaySunriseType) {
        DaylightType.MIDNIGHT_SUN -> defaultWake
        DaylightType.POLAR_NIGHT -> defaultWake
        else -> {
            if (yesterdaySunset.plusMinutes(30)
                    .isAfter(todaySunrise.minusMinutes(idealSleepMinutes))
            ) {
                yesterdaySunset.plusMinutes(30).plusMinutes(idealSleepMinutes)
            } else if (todaySunrise.isBefore(defaultWake)) {
                todaySunrise
            } else {
                defaultWake
            }
        }
    }
}

fun sleepCalculation(
    todaySunset: ZonedDateTime, todaySunsetType: DaylightType,
    tomorrowSunrise: ZonedDateTime
): ZonedDateTime {
    val default = defaultSleep(todaySunset)
    return when (todaySunsetType) {
        DaylightType.MIDNIGHT_SUN -> default
        DaylightType.POLAR_NIGHT -> default
        else -> {
            if (todaySunset.plusMinutes(30)
                    .isAfter(tomorrowSunrise.minusMinutes(idealSleepMinutes))
            ) {
                todaySunset.plusMinutes(30)
            } else if (tomorrowSunrise.isBefore(defaultWake(tomorrowSunrise))) {
                tomorrowSunrise.minusHours(7).minusMinutes(30)
            } else {
                default
            }
        }
    }
}