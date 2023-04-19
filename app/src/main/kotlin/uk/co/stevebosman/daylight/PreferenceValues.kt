package uk.co.stevebosman.daylight

interface PreferenceValues {
    val latestWakeupTimeHours: Int
    val latestWakeupTimeMinutes: Int
    val earliestSleepTimeHours: Int
    val earliestSleepTimeMinutes: Int
    val sleepDurationMinutes: Long
}