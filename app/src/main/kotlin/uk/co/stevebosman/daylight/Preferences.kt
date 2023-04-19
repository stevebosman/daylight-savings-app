package uk.co.stevebosman.daylight

import android.content.Context
import androidx.preference.PreferenceManager

class Preferences(context: Context): PreferenceValues {
    private val allowAlarms: Boolean
    override val latestWakeupTimeHours: Int
    override val latestWakeupTimeMinutes: Int
    override val earliestSleepTimeHours: Int
    override val earliestSleepTimeMinutes: Int
    override val sleepDurationMinutes: Long

    init {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        allowAlarms = sharedPreferences.getBoolean(context.getString(R.string.preference_allow_alarms), false)
        val latestWakeupTime = sharedPreferences.getString(context.getString(R.string.preference_latest_wakeup_time), context.getString(R.string.default_latest_wakeup_time))!!
        val latestWakeupTimeElements = latestWakeupTime.split(":")
        latestWakeupTimeHours = latestWakeupTimeElements[0].toInt()
        latestWakeupTimeMinutes = latestWakeupTimeElements[1].toInt()
        val earliestSleepTime = sharedPreferences.getString(context.getString(R.string.preference_earliest_sleep_time), context.getString(R.string.default_earliest_sleep_time))!!
        val earliestSleepTimeElements = earliestSleepTime.split(":")
        earliestSleepTimeHours = earliestSleepTimeElements[0].toInt()
        earliestSleepTimeMinutes = earliestSleepTimeElements[1].toInt()
        val sleepDuration = sharedPreferences.getString(context.getString(R.string.preference_sleep_duration), context.getString(R.string.default_sleep_duration))!!.toFloat()
        sleepDurationMinutes = (sleepDuration * 60).toLong()
    }
}