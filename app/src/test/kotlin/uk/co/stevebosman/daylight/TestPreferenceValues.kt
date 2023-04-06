package uk.co.stevebosman.daylight

class TestPreferenceValues(
    override val latestWakeupTimeHours: Int,
    override val latestWakeupTimeMinutes: Int,
    override val earliestSleepTimeHours: Int,
    override val earliestSleepTimeMinutes: Int,
    override val sleepDurationMinutes: Long
) : PreferenceValues