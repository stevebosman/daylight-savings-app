package uk.co.stevebosman.daylight.notifications

import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.provider.Settings
import android.util.Log

fun checkNotificationPermissions(context: Context): Boolean {
    // Check if notification permissions are granted
    val notificationManager =
        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    val isEnabled = notificationManager.areNotificationsEnabled()

    if (!isEnabled) {
        Log.d("Daylight", "notifications not enabled - ask to enable")
        // Open the app notification settings if notifications are not enabled
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        context.startActivity(intent)

        return false
    }

    Log.d("Daylight", "notifications enabled")
    // Permissions are granted
    return true
}
