package org.example.project.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    private val workNamePrefix = "weight_reminder_notification"
    
    fun scheduleNotification(hour: Int, minute: Int, days: Set<Int>) {
        // Cancel all existing notification work
        workManager.cancelAllWorkByTag(workNamePrefix)
        AlarmNotificationScheduler.cancelAllAlarms(context)
        
        if (days.isEmpty()) {
            return
        }
        
        // Log battery optimization status
        if (!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)) {
            android.util.Log.w("NotificationScheduler", "Battery optimization is not disabled. Notifications may not work reliably when app is closed.")
        } else {
            android.util.Log.d("NotificationScheduler", "Battery optimization is disabled. Notifications should work reliably.")
        }
        
        // Use AlarmManager for exact timing (works even when app is killed)
        days.forEach { dayOfWeek ->
            AlarmNotificationScheduler.scheduleWeeklyAlarm(context, hour, minute, dayOfWeek)
        }
        
        // Start foreground service to keep app alive (helps prevent alarms from being cancelled)
        // Note: This shows a persistent notification but ensures alarms work reliably
        // NotificationForegroundService.start(context)
        
        // Also keep WorkManager as backup (for Android versions that don't support exact alarms well)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val now = Calendar.getInstance()
        val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        
        // Schedule work for each selected day
        days.forEach { dayOfWeek ->
            val workName = "${workNamePrefix}_$dayOfWeek"
            
            // Calculate delay until next occurrence of this day
            val targetCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            // Find next occurrence of this day
            var daysUntilTarget = (dayOfWeek - currentDayOfWeek + 7) % 7
            if (daysUntilTarget == 0) {
                // Today is the target day, check if time has passed
                if (targetCalendar.timeInMillis <= System.currentTimeMillis()) {
                    daysUntilTarget = 7 // Schedule for next week
                }
            }
            
            targetCalendar.add(Calendar.DAY_OF_MONTH, daysUntilTarget)
            val delay = targetCalendar.timeInMillis - System.currentTimeMillis()
            
            // Create work request with day information
            val inputData = Data.Builder()
                .putInt("hour", hour)
                .putInt("minute", minute)
                .putIntArray("days", days.toIntArray())
                .build()
            
            // Schedule recurring work for this day (weekly)
            val weeklyWork = PeriodicWorkRequestBuilder<NotificationWorker>(
                7, TimeUnit.DAYS
            )
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag(workNamePrefix)
                .addTag(workName)
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                workName,
                ExistingPeriodicWorkPolicy.REPLACE,
                weeklyWork
            )
        }
    }
    
    fun cancelNotification() {
        workManager.cancelAllWorkByTag(workNamePrefix)
        AlarmNotificationScheduler.cancelAllAlarms(context)
    }
    
    fun requestExactAlarmPermission() {
        AlarmNotificationScheduler.requestExactAlarmPermission(context)
    }
    
    fun requestBatteryOptimizationExemption() {
        BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
    }
    
    fun isBatteryOptimizationIgnored(): Boolean {
        return BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
    }
    
    fun showTestNotification(coroutineScope: CoroutineScope, onPermissionNeeded: (() -> Unit)? = null) {
        android.util.Log.d("NotificationScheduler", "showTestNotification called, will show in 5 seconds")
        
        // Check permission first
        if (!NotificationPermissionHelper.hasNotificationPermission(context)) {
            android.util.Log.w("NotificationScheduler", "Notification permission not granted, requesting...")
            onPermissionNeeded?.invoke()
            return
        }
        
        // Use AlarmManager for test notification to verify it works when app is killed
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            add(Calendar.SECOND, 5)
        }
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("hour", 8)
            putExtra("minute", 0)
            putExtra("dayOfWeek", Calendar.MONDAY)
            action = "org.example.project.TEST_NOTIFICATION"
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                android.util.Log.d("NotificationScheduler", "Test alarm scheduled for 5 seconds from now")
            } catch (e: Exception) {
                android.util.Log.e("NotificationScheduler", "Failed to schedule test alarm", e)
                // Fallback to coroutine delay
                coroutineScope.launch {
                    delay(5000)
                    NotificationWorker.showTestNotification(context)
                }
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }
}
