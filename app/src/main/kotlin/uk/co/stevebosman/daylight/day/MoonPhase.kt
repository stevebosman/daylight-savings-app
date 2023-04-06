package uk.co.stevebosman.daylight.day

enum class MoonPhase(val icon: String) {
    NEW("\uD83C\uDF11"),
    CRESCENT("\uD83C\uDF12"),
    QUARTER("\uD83C\uDF13"),
    GIBBOUS("\uD83C\uDF14"),
    FULL("\uD83C\uDF15"),
    WAXING_GIBBOUS("\uD83C\uDF16"),
    LAST_QUARTER("\uD83C\uDF17"),
    WAXING_CRESCENT("\uD83C\uDF18");
    companion object {
        fun of(phaseValue: Double): MoonPhase {
            val phaseCount = values().size
            return values()[(phaseValue * phaseCount + 0.5).toInt() % phaseCount]
        }
    }
}