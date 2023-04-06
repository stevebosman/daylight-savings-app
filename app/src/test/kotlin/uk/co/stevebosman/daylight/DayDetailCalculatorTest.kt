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

        val preferences: PreferenceValues = TestPreferenceValues(7, 0, 21, 0, (60 * 8).toLong())
        val instance = DayDetailCalculator(preferences)

        val solarNoonTime = ZonedDateTime.of(2023, 4, 1, 12, 0, 0, 0, zoneId)
        val sunriseTime = solarNoonTime.withHour(6)
        val sunsetTime = solarNoonTime.withHour(18)

        val yesterdaysDetails = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime.minusDays(1),
            sunriseTime.minusDays(1).plusMinutes(2),
            sunsetTime.minusDays(1).minusMinutes(2),
            0.1)

        val todaysDetails = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime, sunriseTime, sunsetTime,
            0.1)

        val tomorrowsDetails = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime.plusDays(1),
            sunriseTime.plusDays(1).minusMinutes(2),
            sunsetTime.plusDays(1).plusMinutes(2),
            0.1)

        // When
        val dayDetails = instance.calculate(arrayOf(yesterdaysDetails, todaysDetails, tomorrowsDetails))

        // Then
        assertAll(
            { assertEquals(todaysDetails, dayDetails[0].day) { "Unexpected day" } },
            { assertEquals(todaysDetails.sunriseTime, dayDetails[0].wakeUp) {"Unexpected wake up"} },
            { assertEquals(tomorrowsDetails.sunriseTime.minusMinutes(preferences.sleepDurationMinutes), dayDetails[0].sleep) {"Unexpected sleep"} }
        )
    }

    @Test
    fun winterTimeGivesExpectedWakeup() {
        // Given
        val zoneId = ZoneId.of("Europe/London")

        val preferences: PreferenceValues = TestPreferenceValues(7, 0, 21, 0, (60 * 8).toLong())
        val instance = DayDetailCalculator(preferences)

        val solarNoonTime = ZonedDateTime.of(2023, 12, 21, 12, 0, 0, 0, zoneId)
        val sunriseTime = solarNoonTime.withHour(8)
        val sunsetTime = solarNoonTime.withHour(16)

        val yesterdaysDetails = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime.minusDays(1),
            sunriseTime.minusDays(1).minusMinutes(2),
            sunsetTime.minusDays(1).plusMinutes(2),
            0.1)

        val todaysDetails = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime, sunriseTime, sunsetTime,
            0.1)

        val tomorrowsDetails = SunriseDetails(
            DaylightType.NORMAL, DaylightType.NORMAL,
            solarNoonTime.plusDays(1),
            sunriseTime.plusDays(1).minusMinutes(2),
            sunsetTime.plusDays(1).plusMinutes(2),
            0.1)

        // When
        val dayDetails = instance.calculate(arrayOf(yesterdaysDetails, todaysDetails, tomorrowsDetails))

        // Then
        val expectedSleep = tomorrowsDetails.sunriseTime
            .withHour(preferences.latestWakeupTimeHours)
            .withMinute(preferences.latestWakeupTimeMinutes)
            .minusMinutes(preferences.sleepDurationMinutes)
        val expectedWakeUp = solarNoonTime
            .withHour(preferences.latestWakeupTimeHours)
            .withMinute(preferences.latestWakeupTimeMinutes)
        assertSunriseDetails(todaysDetails, expectedWakeUp, expectedSleep, dayDetails[0])
    }
}

private fun assertSunriseDetails(
    expectedDay: SunriseDetails,
    expectedWakeUp: ZonedDateTime?,
    expectedSleep: ZonedDateTime?,
    actualDayDetails: DayDetails
) {
    assertAll(
        { assertEquals(expectedDay, actualDayDetails.day) { "Unexpected day - ${actualDayDetails.day}" } },
        { assertEquals(expectedWakeUp, actualDayDetails.wakeUp) { "Unexpected wake up - ${actualDayDetails.wakeUp}" } },
        { assertEquals(expectedSleep, actualDayDetails.sleep) { "Unexpected sleep - ${actualDayDetails.sleep}" } }
    )
}
