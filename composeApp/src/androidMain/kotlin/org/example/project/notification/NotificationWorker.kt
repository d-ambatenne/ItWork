package org.example.project.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.example.project.MainActivity
import java.util.Calendar

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // Check if today is a selected day
        val days = inputData.getIntArray("days")
        if (days != null) {
            val calendar = Calendar.getInstance()
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (currentDayOfWeek !in days) {
                // Today is not a selected day, skip notification
                return Result.success()
            }
        }
        
        showNotification()
        return Result.success()
    }
    
    private fun showNotification() {
        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channelId = "weight_reminder_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Weight Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to log your weight"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
                val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Weight Reminder")
                .setContentText("Don't forget to log your weight today!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()
        
        notificationManager.notify(1, notification)
    }
    
    companion object {
        fun showScheduledNotification(context: Context) {
            android.util.Log.d("NotificationWorker", "showScheduledNotification called")
            try {
                // Check notification permission for Android 13+
                if (!NotificationPermissionHelper.hasNotificationPermission(context)) {
                    android.util.Log.e("NotificationWorker", "Notification permission not granted.")
                    return
                }
                
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (!notificationManager.areNotificationsEnabled()) {
                        android.util.Log.e("NotificationWorker", "Notifications are not enabled in system settings.")
                        return
                    }
                }
                
                val channelId = "weight_reminder_channel"
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "Weight Reminder",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Reminders to log your weight"
                        enableVibration(true)
                        enableLights(true)
                    }
                    notificationManager.createNotificationChannel(channel)
                }
                
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                
                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Weight Reminder")
                    .setContentText("Don't forget to log your weight today!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .build()
                
                notificationManager.notify(1, notification)
                android.util.Log.d("NotificationWorker", "Scheduled notification sent successfully")
            } catch (e: Exception) {
                android.util.Log.e("NotificationWorker", "Error showing scheduled notification", e)
                e.printStackTrace()
            }
        }
        fun showTestNotification(context: Context) {
            android.util.Log.d("NotificationWorker", "showTestNotification called")
            try {
                // Check notification permission for Android 13+
                if (!NotificationPermissionHelper.hasNotificationPermission(context)) {
                    android.util.Log.e("NotificationWorker", "Notification permission not granted. Please grant notification permission.")
                    return
                }
                
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (!notificationManager.areNotificationsEnabled()) {
                        android.util.Log.e("NotificationWorker", "Notifications are not enabled in system settings.")
                        return
                    }
                }
                
                val channelId = "weight_reminder_channel"
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "Weight Reminder",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Reminders to log your weight"
                        enableVibration(true)
                        enableLights(true)
                    }
                    notificationManager.createNotificationChannel(channel)
                    android.util.Log.d("NotificationWorker", "Notification channel created")
                }
                
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                
                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Weight Reminder")
                    .setContentText("Don't forget to log your weight today!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .build()
                
                notificationManager.notify(999, notification) // Use different ID for test notification
                android.util.Log.d("NotificationWorker", "Test notification sent successfully")
            } catch (e: Exception) {
                android.util.Log.e("NotificationWorker", "Error showing test notification", e)
                e.printStackTrace()
            }
        }
    }
}

