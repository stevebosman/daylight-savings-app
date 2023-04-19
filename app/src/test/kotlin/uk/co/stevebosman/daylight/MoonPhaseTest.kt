package uk.co.stevebosman.daylight

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.co.stevebosman.daylight.day.MoonPhase


/**
 * Test moon phase enum
 */
class MoonPhaseTest {
    @ParameterizedTest
    @CsvSource(value = [
        "0.00000, NEW",
        "0.06249, NEW",
        "0.06250, CRESCENT",
        "0.18749, CRESCENT",
        "0.18750, QUARTER",
        "0.31249, QUARTER",
        "0.31250, GIBBOUS",
        "0.43749, GIBBOUS",
        "0.43750, FULL",
        "0.56249, FULL",
        "0.56250, WAXING_GIBBOUS",
        "0.68749, WAXING_GIBBOUS",
        "0.68750, LAST_QUARTER",
        "0.81249, LAST_QUARTER",
        "0.81250, WAXING_CRESCENT",
        "0.93749, WAXING_CRESCENT",
        "0.93750, NEW",
        "0.99999, NEW"
    ])
    fun phaseIsCorrect(phaseValue: Double, expectedPhase: MoonPhase) {
        assertEquals(expectedPhase, MoonPhase.of(phaseValue))
    }

    @ParameterizedTest
    @CsvSource(value = [
        "NEW, \uD83C\uDF11",
        "CRESCENT, \uD83C\uDF12",
        "QUARTER, \uD83C\uDF13",
        "GIBBOUS, \uD83C\uDF14",
        "FULL, \uD83C\uDF15",
        "WAXING_GIBBOUS, \uD83C\uDF16",
        "LAST_QUARTER, \uD83C\uDF17",
        "WAXING_CRESCENT, \uD83C\uDF18"
    ])
    fun iconIsCorrect(phase: MoonPhase, expectedIcon: String) {
        assertEquals(expectedIcon, phase.icon)
    }
}
