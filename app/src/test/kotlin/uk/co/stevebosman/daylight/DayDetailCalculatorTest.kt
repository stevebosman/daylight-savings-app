package uk.co.stevebosman.daylight

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import uk.co.stevebosman.daylight.day.DayDetailCalculator
import uk.co.stevebosman.daylight.day.DayDetails
import uk.co.stevebosman.sunrise.DaylightType
import uk.co.stevebosman.sunrise.SunriseDetails
import java.time.ZoneId
import java.time.ZonedDateTime


/**
 * Test moon phase enum
 */
class DayDetailCalculatorTest {
    @Test
    fun regularTimeGivesExpectedWakeup() {
        // Given
        val zoneId = ZoneId.of("Europe/London")

        val preferences: PreferenceValues = TestPreferenceValues()
        val instance = DayDetailCalculator(preferences)

        val solarNoonTime = ZonedDateTime.of(2023, 4, 1, 12, 0, 0, 0, zoneId)
        val sunriseTime = solarNoonTime.withHour(6)
        val sunsetTime = solarNoonTime.withHour(18)

        val yesterday = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime.minusDays(1),
            sunriseTime.minusDays(1).plusMinutes(2),
            sunsetTime.minusDays(1).minusMinutes(2),
            0.1
        )

        val today = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime, sunriseTime, sunsetTime,
            0.1
        )

        val tomorrow = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime.plusDays(1),
            sunriseTime.plusDays(1).minusMinutes(2),
            sunsetTime.plusDays(1).plusMinutes(2),
            0.1
        )

        // When
        val dayDetails =
            instance.calculate(arrayOf(yesterday, today, tomorrow))

        // Then
        assertSunriseDetails(
            today,
            today.sunriseTime,
            tomorrow.sunriseTime.minusMinutes(preferences.sleepDurationMinutes),
            dayDetails[0]
        )
    }

    @Test
    fun sunsetAfterEarliestSleepGivesExpectedWakeup() {
        // Given
        val zoneId = ZoneId.of("Europe/London")

        val preferences: PreferenceValues = TestPreferenceValues()
        val instance = DayDetailCalculator(preferences)

        val solarNoonTime = ZonedDateTime.of(2023, 6, 21, 12, 0, 0, 0, zoneId)
        val sunriseTime = solarNoonTime.withHour(4)
        val sunsetTime = solarNoonTime.withHour(21).withMinute(55)

        val yesterday = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime.minusDays(1),
            sunriseTime.minusDays(1).plusMinutes(2),
            sunsetTime.minusDays(1).minusMinutes(2),
            0.1
        )

        val today = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime, sunriseTime, sunsetTime,
            0.1
        )

        val tomorrow = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime.plusDays(1),
            sunriseTime.plusDays(1).plusMinutes(2),
            sunsetTime.plusDays(1).minusMinutes(2),
            0.1
        )

        // When
        val dayDetails =
            instance.calculate(arrayOf(yesterday, today, tomorrow))

        // Then
        assertSunriseDetails(
            today,
            yesterday.sunsetTime
                .plusMinutes(preferences.sleepDurationMinutes),
            today.sunsetTime,
            dayDetails[0]
        )
    }

    @Test
    fun winterTimeGivesExpectedWakeup() {
        // Given
        val zoneId = ZoneId.of("Europe/London")

        val preferences: PreferenceValues = TestPreferenceValues()
        val instance = DayDetailCalculator(preferences)

        val solarNoonTime = ZonedDateTime.of(2023, 12, 21, 12, 0, 0, 0, zoneId)
        val sunriseTime = solarNoonTime.withHour(8)
        val sunsetTime = solarNoonTime.withHour(16)

        val yesterday = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime.minusDays(1),
            sunriseTime.minusDays(1).minusMinutes(2),
            sunsetTime.minusDays(1).plusMinutes(2),
            0.1
        )

        val today = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime, sunriseTime, sunsetTime,
            0.1
        )

        val tomorrow = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime.plusDays(1),
            sunriseTime.plusDays(1).minusMinutes(2),
            sunsetTime.plusDays(1).plusMinutes(2),
            0.1
        )

        // When
        val dayDetails =
            instance.calculate(arrayOf(yesterday, today, tomorrow))

        // Then
        assertSunriseDetails(
            today,
            solarNoonTime
                .withHour(preferences.latestWakeupTimeHours)
                .withMinute(preferences.latestWakeupTimeMinutes),
            tomorrow.sunriseTime
                .withHour(preferences.latestWakeupTimeHours)
                .withMinute(preferences.latestWakeupTimeMinutes)
                .minusMinutes(preferences.sleepDurationMinutes),
            dayDetails[0]
        )
    }

    @Test
    fun startOfPolarNightGivesExpectedWakeup() {
        // Given
        val zoneId = ZoneId.of("Europe/Oslo")

        val preferences: PreferenceValues = TestPreferenceValues()
        val instance = DayDetailCalculator(preferences)

        val solarNoonTime = ZonedDateTime.of(2023, 12, 1, 12, 0, 0, 0, zoneId)
        val sunriseTime = solarNoonTime.withHour(11).withMinute(55)
        val sunsetTime = solarNoonTime.withHour(12).withMinute(5)

        val yesterday = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime.minusDays(1),
            sunriseTime.minusMinutes(30),
            sunsetTime.plusMinutes(30),
            0.1
        )

        val today = SunriseDetails(
            DaylightType.NORMAL, DaylightType.POLAR_NIGHT,
            solarNoonTime, sunriseTime, sunsetTime,
            0.1
        )

        val tomorrow = SunriseDetails(
            DaylightType.POLAR_NIGHT, DaylightType.POLAR_NIGHT,
            solarNoonTime.plusDays(1),
            ZonedDateTime.of(2024, 3, 1, 12, 0, 0, 0, zoneId),
            sunsetTime,
            0.1
        )

        // When
        val dayDetails =
            instance.calculate(arrayOf(yesterday, today, tomorrow))

        // Then
        assertSunriseDetails(
            today,
            solarNoonTime
                .withHour(preferences.latestWakeupTimeHours)
                .withMinute(preferences.latestWakeupTimeMinutes),
            solarNoonTime
                .withHour(preferences.latestWakeupTimeHours)
                .withMinute(preferences.latestWakeupTimeMinutes)
                .plusDays(1)
                .minusMinutes(preferences.sleepDurationMinutes),
            dayDetails[0]
        )
    }
}

private fun assertSunriseDetails(
    expectedDay: SunriseDetails,
    expectedWakeUp: ZonedDateTime?,
    expectedSleep: ZonedDateTime?,
    actualDayDetails: DayDetails
) {
    assertAll(
        {
            assertEquals(
                expectedDay,
                actualDayDetails.day
            ) { "Unexpected day - ${actualDayDetails.day}" }
        },
        {
            assertEquals(
                expectedWakeUp,
                actualDayDetails.wakeUp
            ) { "Unexpected wake up - ${actualDayDetails.wakeUp}" }
        },
        {
            assertEquals(
                expectedSleep,
                actualDayDetails.sleep
            ) { "Unexpected sleep - ${actualDayDetails.sleep}" }
        }
    )
}
