package uk.co.stevebosman.daylight

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.co.stevebosman.daylight.day.earliestSleepTime
import uk.co.stevebosman.daylight.day.idealWakeUpTime
import uk.co.stevebosman.daylight.day.latestWakeUpTime
import uk.co.stevebosman.sunrise.DaylightType
import uk.co.stevebosman.sunrise.SunriseDetails
import java.time.ZoneId
import java.time.ZonedDateTime

class SunriseDetailsExtensionTest {
    @ParameterizedTest
    @CsvSource(value = [
        "6,18,7",
        "5,19,7",
        "4,20,7",
        "3,21,7"
    ])
    fun checkLatestWakeTime(sunriseHours: Int, sunsetHours: Int, sleepHours: Int) {
        // Given
        val zoneId = ZoneId.of("Europe/London")

        val preferences: PreferenceValues = TestPreferenceValues()

        val solarNoonTime = ZonedDateTime.of(2023, 4, 1, 12, 0, 0, 0, zoneId)
        val sunriseTime = solarNoonTime.withHour(sunriseHours)
        val sunsetTime = solarNoonTime.withHour(sunsetHours)

        val day = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime,
            sunriseTime,
            sunsetTime,
            0.1
        )

        // When
        val actual = day.latestWakeUpTime(preferences)

        // Then
        val expected = solarNoonTime.withHour(sleepHours)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(value = [
        "6,18,6",
        "5,19,5",
        "4,20,4",
        "3,21,3"
    ])
    fun checkIdealWakeTime(sunriseHours: Int, sunsetHours: Int, sleepHours: Int) {
        // Given
        val zoneId = ZoneId.of("Europe/London")

        val preferences: PreferenceValues = TestPreferenceValues()

        val solarNoonTime = ZonedDateTime.of(2023, 4, 1, 12, 0, 0, 0, zoneId)
        val sunriseTime = solarNoonTime.withHour(sunriseHours)
        val sunsetTime = solarNoonTime.withHour(sunsetHours)

        val day = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime,
            sunriseTime,
            sunsetTime,
            0.1
        )

        // When
        val actual = day.idealWakeUpTime(preferences)

        // Then
        val expected = solarNoonTime.withHour(sleepHours)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(value = [
        "6,18,7",
        "5,19,7",
        "4,20,7",
        "3,21,7"
    ])
    fun checkIdealWakeTimePolarNight(sunriseHours: Int, sunsetHours: Int, sleepHours: Int) {
        // Given
        val zoneId = ZoneId.of("Europe/London")

        val preferences: PreferenceValues = TestPreferenceValues()

        val solarNoonTime = ZonedDateTime.of(2023, 4, 1, 12, 0, 0, 0, zoneId)
        val sunriseTime = solarNoonTime.withHour(sunriseHours)
        val sunsetTime = solarNoonTime.withHour(sunsetHours)

        val day = SunriseDetails(
            DaylightType.POLAR_NIGHT, DaylightType.NORMAL,
            solarNoonTime,
            sunriseTime,
            sunsetTime,
            0.1
        )

        // When
        val actual = day.idealWakeUpTime(preferences)

        // Then
        val expected = solarNoonTime.withHour(sleepHours)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(value = [
        "6,18,7",
        "5,19,7",
        "4,20,7",
        "3,21,7"
    ])
    fun checkIdealWakeTimeMidnightSun(sunriseHours: Int, sunsetHours: Int, sleepHours: Int) {
        // Given
        val zoneId = ZoneId.of("Europe/London")

        val preferences: PreferenceValues = TestPreferenceValues()

        val solarNoonTime = ZonedDateTime.of(2023, 4, 1, 12, 0, 0, 0, zoneId)
        val sunriseTime = solarNoonTime.withHour(sunriseHours)
        val sunsetTime = solarNoonTime.withHour(sunsetHours)

        val day = SunriseDetails(
            DaylightType.MIDNIGHT_SUN, DaylightType.NORMAL,
            solarNoonTime,
            sunriseTime,
            sunsetTime,
            0.1
        )

        // When
        val actual = day.idealWakeUpTime(preferences)

        // Then
        val expected = solarNoonTime.withHour(sleepHours)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(value = [
        "6,18,20",
        "5,19,20",
        "4,20,20",
        "3,21,21"
    ])
    fun checkEarliestSleepTime(sunriseHours: Int, sunsetHours: Int, sleepHours: Int) {
        // Given
        val zoneId = ZoneId.of("Europe/London")

        val preferences: PreferenceValues = TestPreferenceValues(earliestSleepTimeHours = 20)

        val solarNoonTime = ZonedDateTime.of(2023, 4, 1, 12, 0, 0, 0, zoneId)
        val sunriseTime = solarNoonTime.withHour(sunriseHours)
        val sunsetTime = solarNoonTime.withHour(sunsetHours)

        val day = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime,
            sunriseTime,
            sunsetTime,
            0.1
        )

        // When
        val actual = day.earliestSleepTime(preferences)

        // Then
        val expected = solarNoonTime.withHour(sleepHours)
        assertEquals(expected, actual)
    }
}