package uk.co.stevebosman.daylight.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE

fun createNotificationChannel(context: Context) {
    val name = "Continuous DST Notification Channel"
    val desc = "Notifies user of suggested sleep time"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(channelID, name, importance)
    channel.description = desc

    // Get the NotificationManager service and create the channel
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}
