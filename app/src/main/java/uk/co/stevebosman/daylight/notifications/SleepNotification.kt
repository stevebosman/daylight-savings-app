package uk.co.stevebosman.daylight.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.util.Log
import uk.co.stevebosman.daylight.angles.Angle
import uk.co.stevebosman.daylight.sleep.sleepCalculation
import uk.co.stevebosman.daylight.sunrise.calculateSunriseDetails
import uk.co.stevebosman.daylight.ui.formatTime
import java.time.ZonedDateTime

fun scheduleNotifications(
    context: Context,
    advanceMinutes: Int,
    longitude: Double,
    latitude: Double
) {
    Log.d("Daylight", "scheduling notifications for $longitude/$latitude")
    for (offsetDays in 0..30) {
        scheduleNotification(context, advanceMinutes, offsetDays, longitude, latitude)
    }
}

private fun scheduleNotification(
    context: Context, advanceMinutes: Int,
    offsetDays: Int,
    longitude: Number,
    latitude: Number
) {
    val date = ZonedDateTime.now().withHour(12).plusDays(offsetDays.toLong())
    val id = date.year * 1000 + date.dayOfYear

    val currentDay =
        calculateSunriseDetails(date, Angle.fromDegrees(longitude), Angle.fromDegrees(latitude))
    val tomorrow =
        calculateSunriseDetails(
            date.plusDays(1L),
            Angle.fromDegrees(longitude),
            Angle.fromDegrees(latitude)
        )

    val sleepTime =
        sleepCalculation(currentDay.sunsetTime, currentDay.sunsetType, tomorrow.sunriseTime)
    if (sleepTime.isAfter(ZonedDateTime.now())) {
        // Create an intent for the Notification BroadcastReceiver
        val intent = Intent(context, SleepNotification::class.java)

        // Extract title and message from user input
        val title = "Continuous DST"
        val message = "${formatTime(sleepTime)} Boing! Time for bed"

        // Add title and message as extras to the intent
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)

        // Create a PendingIntent for the broadcast
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Get the AlarmManager service
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

        // Get the selected time and schedule the notification
        Log.d("Daylight", "setting alert manager to wakeup $advanceMinutes minutes before $sleepTime")
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            (sleepTime.toEpochSecond() - advanceMinutes * 60) * 1000,
            pendingIntent
        )
    }
}
