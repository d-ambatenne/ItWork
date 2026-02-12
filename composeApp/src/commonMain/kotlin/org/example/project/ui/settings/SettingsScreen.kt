package org.example.project.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.example.project.data.SettingsRepository
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    viewModel: SettingsViewModel,
    onNotificationTimeChange: (Int, Int, Set<Int>) -> Unit,
    onNotificationEnabledChange: ((Boolean, Int, Int, Set<Int>) -> Unit)? = null,
    onTestNotification: (() -> Unit)? = null,
    onRequestBatteryOptimization: (() -> Unit)? = null,
    onRequestExactAlarm: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val notificationHour by viewModel.notificationHour.collectAsState()
    val notificationMinute by viewModel.notificationMinute.collectAsState()
    val notificationDays by viewModel.notificationDays.collectAsState()
    
    var showTimePicker by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Reminder",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Get reminded to log your weight",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.setNotificationEnabled(enabled)
                            onNotificationEnabledChange?.invoke(
                                enabled,
                                notificationHour,
                                notificationMinute,
                                notificationDays
                            )
                        }
                    )
                }
                
                if (notificationEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reminder Time",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = String.format("%02d:%02d", notificationHour, notificationMinute),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = { showTimePicker = true }
                        ) {
                            Text("Change Time")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Reminder Days",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    DaySelectionRow(
                        selectedDays = notificationDays,
                        onDaysChanged = { days ->
                            viewModel.setNotificationDays(days)
                            onNotificationTimeChange(notificationHour, notificationMinute, days)
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { onTestNotification?.invoke() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send Test Notification")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (onRequestBatteryOptimization != null) {
                    OutlinedButton(
                        onClick = { onRequestBatteryOptimization.invoke() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Optimize for Background Notifications")
                    }
                    Text(
                        text = "Disable battery optimization to ensure notifications work when app is closed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (onRequestExactAlarm != null) {
                    OutlinedButton(
                        onClick = { onRequestExactAlarm.invoke() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enable Exact Alarm Permission")
                    }
                    Text(
                        text = "Required for precise notification timing when app is swiped away",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
    
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = notificationHour,
            initialMinute = notificationMinute,
            onTimeSelected = { hour, minute ->
                viewModel.setNotificationTime(hour, minute)
                onNotificationTimeChange(hour, minute, notificationDays)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hour")
                        NumberPicker(
                            value = selectedHour,
                            onValueChange = { selectedHour = it },
                            range = 0..23
                        )
                    }
                    Text(":", style = MaterialTheme.typography.headlineMedium)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Minute")
                        NumberPicker(
                            value = selectedMinute,
                            onValueChange = { selectedMinute = it },
                            range = 0..59,
                            step = 5
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onTimeSelected(selectedHour, selectedMinute) }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    step: Int = 1,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                val newValue = value + step
                if (newValue <= range.last) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier.width(80.dp)
        ) {
            Text("▲")
        }
        Text(
            text = String.format("%02d", value),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Button(
            onClick = {
                val newValue = value - step
                if (newValue >= range.first) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier.width(80.dp)
        ) {
            Text("▼")
        }
    }
}

@Composable
fun DaySelectionRow(
    selectedDays: Set<Int>,
    onDaysChanged: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf(
        Calendar.MONDAY to "M",
        Calendar.TUESDAY to "T",
        Calendar.WEDNESDAY to "W",
        Calendar.THURSDAY to "T",
        Calendar.FRIDAY to "F",
        Calendar.SATURDAY to "S",
        Calendar.SUNDAY to "S"
    )
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        dayLabels.forEach { (dayOfWeek, label) ->
            val isSelected = selectedDays.contains(dayOfWeek)
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .toggleable(
                        value = isSelected,
                        onValueChange = { selected ->
                            val newDays = if (selected) {
                                selectedDays + dayOfWeek
                            } else {
                                selectedDays - dayOfWeek
                            }
                            onDaysChanged(newDays)
                        },
                        role = Role.Checkbox
                    ),
                shape = CircleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

