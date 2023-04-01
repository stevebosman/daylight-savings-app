package uk.co.stevebosman.daylight.day

import uk.co.stevebosman.sunrise.SunriseDetails

class MoonPhase {
    companion object {
        private val newMoon = "\uD83C\uDF11"
        private val crescentMoon = "\uD83C\uDF12"
        private val quarterMoon = "\uD83C\uDF13"
        private val gibbousMoon = "\uD83C\uDF14"
        private val fullMoon = "\uD83C\uDF15"
        private val waxingGibbousMoon = "\uD83C\uDF16"
        private val lastQuarterMoon = "\uD83C\uDF17"
        private val waxingCrescentMoon = "\uD83C\uDF18"
        private val moonPhaseIcons = arrayOf(
            newMoon,
            crescentMoon,
            quarterMoon,
            gibbousMoon,
            fullMoon,
            waxingGibbousMoon,
            lastQuarterMoon,
            waxingCrescentMoon
        )

        fun getIcon(day: SunriseDetails) = moonPhaseIcons[(day.moonPhase * 8 + 0.5).toInt() % 8]
    }
}