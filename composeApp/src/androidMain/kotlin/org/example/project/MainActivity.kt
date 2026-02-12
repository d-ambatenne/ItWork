package org.example.project

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.example.project.data.AndroidSettingsRepository
import org.example.project.data.SettingsRepository
import org.example.project.data.WeightDatabase
import org.example.project.data.WeightRepository
import org.example.project.data.WeightDaoAdapter
import org.example.project.notification.NotificationPermissionHelper
import org.example.project.notification.NotificationScheduler
import org.example.project.fitness.StepCountProvider

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "Notification permission granted")
        } else {
            android.util.Log.w("MainActivity", "Notification permission denied")
        }
    }
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        android.util.Log.d("MainActivity", "Google Sign-In result: ${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            android.util.Log.d("MainActivity", "Google Sign-In successful")
            // Check if permissions were granted and request if needed
            checkAndRequestFitnessPermissions()
        } else {
            android.util.Log.w("MainActivity", "Google Sign-In failed or cancelled: ${result.resultCode}")
        }
    }
    
    
    private fun checkAndRequestFitnessPermissions() {
        val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val fitnessOptions = com.google.android.gms.fitness.FitnessOptions.builder()
                .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA, com.google.android.gms.fitness.FitnessOptions.ACCESS_READ)
                .addDataType(com.google.android.gms.fitness.data.DataType.AGGREGATE_STEP_COUNT_DELTA, com.google.android.gms.fitness.FitnessOptions.ACCESS_READ)
                .build()
            
            val hasPermissions = com.google.android.gms.auth.api.signin.GoogleSignIn.hasPermissions(account, fitnessOptions)
            android.util.Log.d("MainActivity", "Has fitness permissions: $hasPermissions")
            
            if (!hasPermissions) {
                // Request permissions explicitly - this will start an activity
                android.util.Log.d("MainActivity", "Requesting fitness permissions")
                com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions(
                    this,
                    9001,
                    account,
                    fitnessOptions
                )
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001) {
            android.util.Log.d("MainActivity", "Permission request completed: $resultCode")
            if (resultCode == RESULT_OK) {
                android.util.Log.d("MainActivity", "Fitness permissions granted")
            } else {
                android.util.Log.w("MainActivity", "Fitness permissions denied")
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AndroidApp(
                onRequestNotificationPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onRequestGoogleFitPermission = { stepCountProvider ->
                    try {
                        // First check if user is already signed in
                        val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(this)
                        if (account != null) {
                            // User is signed in, check and request permissions if needed
                            checkAndRequestFitnessPermissions()
                        } else {
                            // User is not signed in, start sign-in flow with fitness scopes
                            val signInIntent = stepCountProvider.getSignInIntent()
                            googleSignInLauncher.launch(signInIntent)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Error requesting Google Fit permission", e)
                        // Fallback to regular sign-in intent
                        val signInIntent = stepCountProvider.getSignInIntent()
                        googleSignInLauncher.launch(signInIntent)
                    }
                }
            )
        }
    }
}

@Composable
fun AndroidApp(
    onRequestNotificationPermission: () -> Unit,
    onRequestGoogleFitPermission: (StepCountProvider) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val database = remember { WeightDatabase.getDatabase(context) }
    val weightRepository = remember { 
        WeightRepository(WeightDaoAdapter(database.weightDao()))
    }
    val settingsRepository = remember { 
        AndroidSettingsRepository(context) as SettingsRepository
    }
    val notificationScheduler = remember { NotificationScheduler(context) }
    val stepCountProvider = remember { StepCountProvider(context) }
    
    // Refresh step count if permission is already granted (but don't request permission automatically)
    LaunchedEffect(Unit) {
        if (stepCountProvider.hasPermission()) {
            stepCountProvider.refresh()
        }
    }
    
    // Observe notification settings to schedule/cancel notifications
    val notificationEnabled by settingsRepository.notificationEnabled.collectAsState(initial = false)
    val notificationHour by settingsRepository.notificationHour.collectAsState(initial = 8)
    val notificationMinute by settingsRepository.notificationMinute.collectAsState(initial = 0)
    val notificationDays by settingsRepository.notificationDays.collectAsState(initial = setOf(2, 3, 4, 5, 6, 7, 1))
    
    LaunchedEffect(notificationEnabled, notificationHour, notificationMinute, notificationDays) {
        if (notificationEnabled && notificationDays.isNotEmpty()) {
            notificationScheduler.scheduleNotification(notificationHour, notificationMinute, notificationDays)
        } else {
            notificationScheduler.cancelNotification()
        }
    }
    
    App(
        weightRepository = weightRepository,
        settingsRepository = settingsRepository,
        stepCountProvider = stepCountProvider,
        onRequestGoogleFitPermission = {
            onRequestGoogleFitPermission(stepCountProvider)
        },
        onNotificationTimeChange = { hour, minute, days ->
            if (notificationEnabled) {
                notificationScheduler.scheduleNotification(hour, minute, days)
            }
        },
        onNotificationEnabledChange = { enabled, hour, minute, days ->
            if (enabled && days.isNotEmpty()) {
                notificationScheduler.scheduleNotification(hour, minute, days)
            } else {
                notificationScheduler.cancelNotification()
            }
        },
        onTestNotification = {
            notificationScheduler.showTestNotification(coroutineScope) {
                onRequestNotificationPermission()
            }
        },
        onRequestBatteryOptimization = {
            notificationScheduler.requestBatteryOptimizationExemption()
        },
        onRequestExactAlarm = {
            notificationScheduler.requestExactAlarmPermission()
        },
        coroutineScope = coroutineScope
    )
}