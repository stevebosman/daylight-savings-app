package uk.co.stevebosman.daylight

class TestPreferenceValues(
    override val latestWakeupTimeHours: Int = 7,
    override val latestWakeupTimeMinutes: Int = 0,
    override val earliestSleepTimeHours: Int = 21,
    override val earliestSleepTimeMinutes: Int = 0,
    override val sleepDurationMinutes: Long = 60*8
) : PreferenceValues