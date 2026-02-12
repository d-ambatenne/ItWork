package org.example.project.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.util.Calendar

object AlarmNotificationScheduler {
    private const val REQUEST_CODE_PREFIX = 1000
    
    fun scheduleWeeklyAlarm(context: Context, hour: Int, minute: Int, dayOfWeek: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Check if exact alarms are allowed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmNotificationScheduler", "Cannot schedule exact alarms. Permission may be needed.")
                // Fallback to inexact alarm
                scheduleInexactAlarm(context, alarmManager, hour, minute, dayOfWeek)
                return
            }
        }
        
        // Calculate next occurrence
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Find next occurrence of this day
        val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        var daysUntilTarget = (dayOfWeek - currentDayOfWeek + 7) % 7
        
        if (daysUntilTarget == 0) {
            // Today is the target day, check if time has passed
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                daysUntilTarget = 7 // Schedule for next week
            }
        }
        
        calendar.add(Calendar.DAY_OF_MONTH, daysUntilTarget)
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("hour", hour)
            putExtra("minute", minute)
            putExtra("dayOfWeek", dayOfWeek)
            // Add action to help Android identify the intent
            action = "org.example.project.NOTIFICATION_ALARM"
        }
        
        val requestCode = REQUEST_CODE_PREFIX + dayOfWeek
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            pendingIntentFlags
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Use setExactAndAllowWhileIdle for better reliability when app is killed
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d("AlarmNotificationScheduler", "Scheduled exact alarm with allowWhileIdle for day $dayOfWeek at ${calendar.time}")
                Log.d("AlarmNotificationScheduler", "Alarm will fire at: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(calendar.time)}")
            } catch (e: SecurityException) {
                Log.e("AlarmNotificationScheduler", "Failed to set exact alarm, trying inexact", e)
                // Fallback to inexact if exact fails
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                Log.d("AlarmNotificationScheduler", "Scheduled inexact alarm as fallback for day $dayOfWeek")
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            Log.d("AlarmNotificationScheduler", "Scheduled exact alarm (pre-M) for day $dayOfWeek")
        }
        
        Log.d("AlarmNotificationScheduler", "Scheduled alarm for day $dayOfWeek at $hour:$minute, trigger time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(calendar.time)}")
    }
    
    private fun scheduleInexactAlarm(context: Context, alarmManager: AlarmManager, hour: Int, minute: Int, dayOfWeek: Int) {
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Find next occurrence of this day
        val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        var daysUntilTarget = (dayOfWeek - currentDayOfWeek + 7) % 7
        
        if (daysUntilTarget == 0) {
            // Today is the target day, check if time has passed
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                daysUntilTarget = 7 // Schedule for next week
            }
        }
        
        calendar.add(Calendar.DAY_OF_MONTH, daysUntilTarget)
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("hour", hour)
            putExtra("minute", minute)
            putExtra("dayOfWeek", dayOfWeek)
            action = "org.example.project.NOTIFICATION_ALARM"
        }
        
        val requestCode = REQUEST_CODE_PREFIX + dayOfWeek
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            pendingIntentFlags
        )
        
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        Log.d("AlarmNotificationScheduler", "Scheduled inexact alarm for day $dayOfWeek at $hour:$minute")
    }
    
    fun cancelAlarm(context: Context, dayOfWeek: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "org.example.project.NOTIFICATION_ALARM"
        }
        val requestCode = REQUEST_CODE_PREFIX + dayOfWeek
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            pendingIntentFlags
        )
        alarmManager.cancel(pendingIntent)
        Log.d("AlarmNotificationScheduler", "Cancelled alarm for day $dayOfWeek")
    }
    
    fun cancelAllAlarms(context: Context) {
        for (day in 1..7) {
            cancelAlarm(context, day)
        }
        Log.d("AlarmNotificationScheduler", "Cancelled all alarms")
    }
    
    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("AlarmNotificationScheduler", "Failed to request exact alarm permission", e)
                }
            }
        }
    }
}

