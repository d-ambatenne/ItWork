package org.example.project.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.example.project.data.WeightEntry
import org.example.project.data.WeightRepository
import org.example.project.ui.chart.WeightProgressChart
import org.example.project.ui.chart.ChartDataPoint
import org.example.project.fitness.StepCountProvider
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    repository: WeightRepository,
    viewModel: ProgressViewModel,
    stepCountProvider: StepCountProvider? = null,
    onRequestGoogleFitPermission: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val weightEntries by viewModel.weightEntries.collectAsState()
    
    val latestWeight = viewModel.getLatestWeight()
    val weightChange = viewModel.getWeightChange()
    
    val waterCupsCount by viewModel.waterCupsCount.collectAsState()
    val newCupAdded by viewModel.newCupAdded.collectAsState()
    
    var showAddWeightDialog by remember { mutableStateOf(false) }
    
    // Step count state
    val stepCount by stepCountProvider?.dailyStepCount?.collectAsState(initial = null) ?: remember { mutableStateOf(null) }
    var hasPermission by remember { mutableStateOf(false) }
    
    // Check permission status and refresh step count when screen is displayed
    LaunchedEffect(stepCountProvider) {
        stepCountProvider?.let { provider ->
            hasPermission = provider.hasPermission()
            if (hasPermission) {
                provider.refresh()
            }
        }
    }
    
    // Periodically recheck permission (useful after returning from sign-in)
    // This helps detect when user grants permission after sign-in
    LaunchedEffect(Unit) {
        repeat(Int.MAX_VALUE) {
            kotlinx.coroutines.delay(3000) // Check every 3 seconds
            stepCountProvider?.let { provider ->
                val currentPermission = provider.hasPermission()
                if (currentPermission != hasPermission) {
                    hasPermission = currentPermission
                    if (hasPermission) {
                        provider.refresh()
                    }
                }
            }
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Stats Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Weight",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = latestWeight?.let { "%.1f kg".format(it) } ?: "N/A",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Change",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = weightChange?.let { 
                            val sign = if (it >= 0) "+" else ""
                            "$sign%.1f kg".format(it)
                        } ?: "N/A",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        
        // Step Count Card (only shown if stepCountProvider is available)
        stepCountProvider?.let { provider ->
            val hasPermissionState = remember { mutableStateOf(false) }
            LaunchedEffect(provider) {
                hasPermissionState.value = provider.hasPermission()
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .then(
                        if (!hasPermissionState.value && onRequestGoogleFitPermission != null) {
                            Modifier.clickable {
                                onRequestGoogleFitPermission()
                            }
                        } else {
                            Modifier
                        }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Steps Today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (hasPermissionState.value) {
                                stepCount?.let { 
                                    "%,d".format(it)
                                } ?: "Loading..."
                            } else {
                                "Tap to connect Google Fit"
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = "Steps",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
        
        // Weight Progress Chart
        if (weightEntries.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(300.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                val chartDataPoints = remember(weightEntries) {
                    weightEntries.reversed().map { entry ->
                        ChartDataPoint(id = entry.id, date = entry.date, weight = entry.weight, note = entry.note)
                    }
                }
                
                WeightProgressChart(
                    dataPoints = chartDataPoints,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    lineColor = MaterialTheme.colorScheme.primary,
                    backgroundColor = Color.Transparent,
                    showGrid = true,
                    onDeleteEntry = { entryId ->
                        val entry = weightEntries.find { it.id == entryId }
                        entry?.let { viewModel.deleteEntry(it) }
                    }
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(300.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data yet. Add your first weight entry!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Water Tracker Section
        WaterTrackerSection(
            cupsCount = waterCupsCount,
            onAddCup = { viewModel.addWaterCup() },
            onDeleteCup = { viewModel.removeWaterCup() },
            isNewCupAdded = newCupAdded,
            modifier = Modifier.fillMaxWidth()
        )
        }
        
        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddWeightDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Weight",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
    
    // Add Weight Dialog
    if (showAddWeightDialog) {
        AddWeightDialog(
            onDismiss = { showAddWeightDialog = false },
            onSave = { weight: Float, date: Long, note: String? ->
                viewModel.saveWeight(weight, date, note)
                showAddWeightDialog = false
            }
        )
    }
}

@Composable
fun CalendarDatePicker(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = remember(selectedYear, selectedMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = getDaysInMonth(selectedMonth, selectedYear)
    
    // Calculate which day of week the 1st falls on (Calendar.SUNDAY = 1, Calendar.MONDAY = 2, etc.)
    // We'll convert to Monday = 0, Sunday = 6
    val startOffset = (firstDayOfWeek - Calendar.MONDAY + 7) % 7
    
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Month/Year Header with Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (selectedMonth == 0) {
                            onMonthChange(11)
                            onYearChange(selectedYear - 1)
                        } else {
                            onMonthChange(selectedMonth - 1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month"
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = monthNames[selectedMonth],
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    // Year selector
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onYearChange(selectedYear - 1) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous Year",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = selectedYear.toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(
                            onClick = { onYearChange(selectedYear + 1) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Year",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                IconButton(
                    onClick = {
                        if (selectedMonth == 11) {
                            onMonthChange(0)
                            onYearChange(selectedYear + 1)
                        } else {
                            onMonthChange(selectedMonth + 1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Month"
                    )
                }
            }
            
            // Day names header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dayNames.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Calendar grid
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Generate weeks (always show 6 weeks for consistent layout)
                repeat(6) { weekIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (dayOfWeek in 0..6) {
                            val dayNumber = weekIndex * 7 + dayOfWeek - startOffset + 1
                            val isCurrentMonth = dayNumber in 1..daysInMonth
                            val isSelected = isCurrentMonth && dayNumber == selectedDay
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable(enabled = isCurrentMonth) {
                                        if (isCurrentMonth) {
                                            onDayChange(dayNumber)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isCurrentMonth) dayNumber.toString() else "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        !isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddWeightDialog(
    onDismiss: () -> Unit,
    onSave: (Float, Long, String?) -> Unit
) {
    var weightText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    
    // Initialize date to current date
    val initialCalendar = remember { Calendar.getInstance() }
    var selectedYear by remember { mutableStateOf(initialCalendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(initialCalendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(initialCalendar.get(Calendar.DAY_OF_MONTH)) }
    
    // Calculate the selected date timestamp
    val selectedDate = remember(selectedYear, selectedMonth, selectedDay) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, selectedDay)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    // Format date for display
    val dateDisplayText = remember(selectedYear, selectedMonth, selectedDay) {
        val months = listOf("January", "February", "March", "April", "May", "June", 
                           "July", "August", "September", "October", "November", "December")
        "${months[selectedMonth]} $selectedDay, $selectedYear"
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Weight Entry",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            weightText = it
                        }
                    },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                )
                
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    minLines = 1
                )
                
                // Date Picker Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Calendar Date Picker
                    CalendarDatePicker(
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        selectedDay = selectedDay,
                        onYearChange = { selectedYear = it },
                        onMonthChange = { 
                            selectedMonth = it
                            // Update day if needed
                            val maxDays = getDaysInMonth(selectedMonth, selectedYear)
                            if (selectedDay > maxDays) {
                                selectedDay = maxDays
                            }
                        },
                        onDayChange = { selectedDay = it }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val weight = weightText.toFloatOrNull()
                            if (weight != null && weight > 0) {
                                onSave(weight, selectedDate, noteText.takeIf { it.isNotBlank() })
                                weightText = ""
                                noteText = ""
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = weightText.toFloatOrNull() != null && weightText.toFloatOrNull()!! > 0
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun WeightEntryItem(
    entry: WeightEntry,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Simple date formatting without external dependencies
    val date = java.util.Date(entry.date)
    val dateStr = remember(date) {
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                           "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val calendar = java.util.Calendar.getInstance().apply { time = date }
        "${months[calendar.get(java.util.Calendar.MONTH)]} ${calendar.get(java.util.Calendar.DAY_OF_MONTH)}, ${calendar.get(java.util.Calendar.YEAR)}"
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "%.1f kg".format(entry.weight),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                entry.note?.takeIf { it.isNotBlank() }?.let { note ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Helper function to get days in a month
fun getDaysInMonth(month: Int, year: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

