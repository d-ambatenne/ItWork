package org.example.project.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.example.project.data.AndroidSettingsRepository
import org.example.project.data.SettingsRepository

class BootReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Device booted, rescheduling notifications")
            
            // Reschedule notifications
            val settingsRepository = AndroidSettingsRepository(context) as SettingsRepository
            val scheduler = NotificationScheduler(context)
            
            scope.launch {
                try {
                    val enabled = settingsRepository.notificationEnabled.first()
                    val hour = settingsRepository.notificationHour.first()
                    val minute = settingsRepository.notificationMinute.first()
                    val days = settingsRepository.notificationDays.first()
                    
                    if (enabled && days.isNotEmpty()) {
                        scheduler.scheduleNotification(hour, minute, days)
                        Log.d("BootReceiver", "Notifications rescheduled successfully")
                    } else {
                        Log.d("BootReceiver", "Notifications not enabled, skipping reschedule")
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling notifications", e)
                }
            }
        }
    }
}


