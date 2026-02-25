package uk.co.stevebosman.daylight.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import uk.co.stevebosman.daylight.R

// Constants for notification
const val sleepNotificationID = 121
const val channelID = "85d45169-c01f-4cea-bd6f-7b5084fc8d60"
const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"

// BroadcastReceiver for handling notifications
class SleepNotification : BroadcastReceiver() {

    // Method called when the broadcast is received
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Daylight", "broadcast received")

        // Build the notification using NotificationCompat.Builder
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(intent.getStringExtra(titleExtra)) // Set title from intent
            .setContentText(intent.getStringExtra(messageExtra)) // Set content text from intent
            .build()

        // Get the NotificationManager service
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Show the notification using the manager
        manager.notify(sleepNotificationID, notification)
    }
}