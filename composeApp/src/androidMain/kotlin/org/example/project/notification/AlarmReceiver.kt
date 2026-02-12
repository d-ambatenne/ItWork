package org.example.project.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm triggered for notification, action: ${intent.action}")
        
        try {
            // Check if this is a test notification
            if (intent.action == "org.example.project.TEST_NOTIFICATION") {
                Log.d("AlarmReceiver", "Test notification alarm triggered")
                NotificationWorker.showTestNotification(context)
                return
            }
            
            // Show the notification using the same method as WorkManager
            NotificationWorker.showScheduledNotification(context)
            
            // Reschedule for next week (7 days from now)
            val hour = intent.getIntExtra("hour", 8)
            val minute = intent.getIntExtra("minute", 0)
            val dayOfWeek = intent.getIntExtra("dayOfWeek", Calendar.MONDAY)
            
            Log.d("AlarmReceiver", "Rescheduling alarm for day $dayOfWeek at $hour:$minute")
            AlarmNotificationScheduler.scheduleWeeklyAlarm(context, hour, minute, dayOfWeek)
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error handling alarm", e)
            e.printStackTrace()
        }
    }
}

