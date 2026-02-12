package org.example.project.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Data point with date and weight value
 */
data class ChartDataPoint(
    val id: Long = 0,
    val date: Long,
    val weight: Float,
    val note: String? = null
)

/**
 * A custom line chart component using Compose Canvas.
 * Shows data points positioned according to their dates within a one-month scale.
 * Works on all platforms including desktop.
 * Supports tap interaction to display date and weight in a floating tooltip.
 */
@Composable
fun WeightProgressChart(
    dataPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = Color.Transparent,
    padding: androidx.compose.ui.unit.Dp = 16.dp,
    strokeWidth: Float = 4f,
    showGrid: Boolean = true,
    gridColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
    monthRange: Long = 30L * 24L * 60L * 60L * 1000L, // 30 days in milliseconds
    onDeleteEntry: ((Long) -> Unit)? = null
) {
    if (dataPoints.isEmpty()) {
        return
    }

    // Filter and sort data points to show only the last month
    val now = System.currentTimeMillis()
    val monthAgo = now - monthRange
    val filteredData = dataPoints
        .filter { it.date >= monthAgo }
        .sortedBy { it.date }

    if (filteredData.isEmpty()) {
        return
    }

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    
    // State for showing point info tooltip
    var showPointInfo by remember { mutableStateOf(false) }
    var selectedPointIndex by remember { mutableStateOf(-1) }
    var tooltipPosition by remember { mutableStateOf<Offset?>(null) }
    var canvasSize by remember { mutableStateOf<Size?>(null) }
    var chartBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    // Calculate date range for x-axis
    val oldestDate = filteredData.first().date
    val newestDate = maxOf(filteredData.last().date, now)
    val dateRange = newestDate - oldestDate
    val normalizedDateRange = if (dateRange > 0) dateRange.toFloat() else 1f

    // Calculate weight range for y-axis
    val weights = filteredData.map { it.weight }
    val minWeight = weights.minOrNull() ?: 0f
    val maxWeight = weights.maxOrNull() ?: 1f
    val weightRange = maxWeight - minWeight
    val normalizedWeightRange = if (weightRange > 0) weightRange else 1f

    // Calculate point positions
    val pointPositions = remember(filteredData, normalizedDateRange, normalizedWeightRange) {
        filteredData.mapIndexed { index, point ->
            val dateOffset = (point.date - oldestDate).toFloat()
            val xRatio = dateOffset / normalizedDateRange
            val normalizedWeight = (point.weight - minWeight) / normalizedWeightRange
            PointPosition(
                index = index,
                xRatio = xRatio,
                yRatio = 1f - normalizedWeight, // Invert Y so higher values are at top
                dataPoint = point
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    canvasSize = Size(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
                }
                .pointerInput(filteredData.size, padding, maxWeight, minWeight, labelColor, textMeasurer) {
                    val paddingPx = padding.toPx()
                    val labelSpacingPx = 32f // ~8dp at 4x density
                    
                    // Calculate label area width (same as in Canvas drawScope)
                    val labelAreaWidth = if (filteredData.size > 1) {
                        val labelStyle = TextStyle(
                            fontSize = 10.sp,
                            color = labelColor
                        )
                        val sampleLabels = listOf(
                            "%.1f".format(maxWeight),
                            "%.1f".format(minWeight),
                            "%.1f".format((maxWeight + minWeight) / 2)
                        )
                        val maxLabelWidth: Float = sampleLabels.map { label ->
                            textMeasurer.measure(text = label, style = labelStyle).size.width
                        }.maxOrNull()?.toFloat() ?: 0f
                        maxLabelWidth + labelSpacingPx + paddingPx
                    } else {
                        paddingPx
                    }
                    
                    detectTapGestures { tapOffset ->
                        val currentCanvasSize = canvasSize ?: return@detectTapGestures
                        val canvasWidth = currentCanvasSize.width
                        val canvasHeight = currentCanvasSize.height
                        
                        val chartTop = paddingPx
                        val chartBottom = canvasHeight - paddingPx
                        val chartLeft = labelAreaWidth // Use same calculation as drawing
                        val chartRight = canvasWidth - paddingPx
                        val chartWidth = chartRight - chartLeft
                        val chartHeight = chartBottom - chartTop
                        
                        // Find the closest point
                        var closestIndex = -1
                        var closestPointPosition: Offset? = null
                        var minDistance = Float.MAX_VALUE
                        val tapRadius = strokeWidth * 8f // Clickable area around each point (increased for better touch targets)
                        
                        pointPositions.forEach { pos ->
                            val pointX = chartLeft + (pos.xRatio * chartWidth)
                            val pointY = chartTop + (pos.yRatio * chartHeight)
                            
                            val dx = tapOffset.x - pointX
                            val dy = tapOffset.y - pointY
                            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                            
                            if (distance < tapRadius && distance < minDistance) {
                                minDistance = distance
                                closestIndex = pos.index
                                closestPointPosition = Offset(pointX, pointY)
                            }
                        }
                        
                        if (closestIndex >= 0 && closestPointPosition != null) {
                            // Toggle tooltip if clicking the same point, otherwise show new one
                            if (selectedPointIndex == closestIndex && showPointInfo) {
                                showPointInfo = false
                            } else {
                                selectedPointIndex = closestIndex
                                tooltipPosition = closestPointPosition
                                showPointInfo = true
                            }
                        } else {
                            // Click outside points - dismiss tooltip
                            showPointInfo = false
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val paddingPx = padding.toPx()
            // Fixed spacing in pixels (approximately 8dp and 4dp at standard density)
            val labelSpacingPx = 32f // ~8dp at 4x density
            val topLabelSpacingPx = 16f // ~4dp at 4x density
            
            // Calculate label area width first (before drawing grid)
            val labelAreaWidth = if (showGrid && filteredData.size > 1) {
                val labelStyle = TextStyle(
                    fontSize = 10.sp,
                    color = labelColor
                )
                val sampleLabels = listOf(
                    "%.1f".format(maxWeight),
                    "%.1f".format(minWeight),
                    "%.1f".format((maxWeight + minWeight) / 2)
                )
                val maxLabelWidth: Float = sampleLabels.map { label ->
                    textMeasurer.measure(text = label, style = labelStyle).size.width
                }.maxOrNull()?.toFloat() ?: 0f
                maxLabelWidth + labelSpacingPx + paddingPx // Add spacing and padding
            } else {
                paddingPx
            }
            
            val chartTop = paddingPx
            val chartBottom = canvasHeight - paddingPx
            val chartLeft = labelAreaWidth // Start chart after labels
            val chartRight = canvasWidth - paddingPx
            val chartWidth = chartRight - chartLeft
            val chartHeight = chartBottom - chartTop
            
            // Store chart bounds for tooltip positioning
            chartBounds = Rect(
                left = chartLeft,
                top = chartTop,
                right = chartRight,
                bottom = chartBottom
            )

            // Draw background
            if (backgroundColor != Color.Transparent) {
                drawRect(
                    color = backgroundColor,
                    topLeft = Offset(chartLeft, chartTop),
                    size = Size(chartWidth, chartHeight)
                )
            }

            // Draw grid lines and Y axis labels
            if (showGrid && filteredData.size > 1) {
                // Horizontal grid lines (weight scale)
                val gridLineCount = 5
                val labelStyle = TextStyle(
                    fontSize = 10.sp,
                    color = labelColor
                )
                val unitLabelStyle = TextStyle(
                    fontSize = 10.sp,
                    color = labelColor,
                    fontWeight = FontWeight.Bold
                )
                
                // Calculate maximum label width for positioning
                val sampleLabels = listOf(
                    "%.1f".format(maxWeight),
                    "%.1f".format(minWeight),
                    "%.1f".format((maxWeight + minWeight) / 2)
                )
                val maxLabelWidth: Float = sampleLabels.map { label ->
                    textMeasurer.measure(text = label, style = labelStyle).size.width
                }.maxOrNull()?.toFloat() ?: 0f
                
                // Position labels to the left of the chart area
                val yAxisLabelX = chartLeft - maxLabelWidth - labelSpacingPx
                
                // Draw "kg" label at the top of Y axis (positioned to avoid data points)
                val unitLabelText = "kg"
                val unitLabelLayout = textMeasurer.measure(
                    text = unitLabelText,
                    style = unitLabelStyle
                )
                drawText(
                    textLayoutResult = unitLabelLayout,
                    topLeft = Offset(
                        yAxisLabelX + (maxLabelWidth - unitLabelLayout.size.width) / 2f,
                        chartTop - unitLabelLayout.size.height - topLabelSpacingPx
                    )
                )
                
                for (i in 0..gridLineCount) {
                    val y = chartTop + (chartHeight / gridLineCount) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(chartLeft, y),
                        end = Offset(chartRight, y),
                        strokeWidth = 1f
                    )
                    
                    // Draw Y axis label (without "kg") - positioned to the left of chart area
                    val weightValue = maxWeight - (weightRange / gridLineCount) * i
                    val labelText = "%.1f".format(weightValue)
                    val textLayoutResult = textMeasurer.measure(
                        text = labelText,
                        style = labelStyle
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            yAxisLabelX + (maxLabelWidth - textLayoutResult.size.width),
                            y - textLayoutResult.size.height / 2
                        )
                    )
                }
                
                // Vertical grid lines (date scale - one per week)
                val weeksInMonth = 4
                for (i in 0..weeksInMonth) {
                    val x = chartLeft + (chartWidth / weeksInMonth) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(x, chartTop),
                        end = Offset(x, chartBottom),
                        strokeWidth = 1f
                    )
                }
            }

            // Draw line chart
            if (filteredData.size > 1) {
                val path = Path()

                pointPositions.forEachIndexed { index, pos ->
                    val x = chartLeft + (pos.xRatio * chartWidth)
                    val y = chartTop + (pos.yRatio * chartHeight)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                // Draw the line
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw points
                pointPositions.forEach { pos ->
                    val x = chartLeft + (pos.xRatio * chartWidth)
                    val y = chartTop + (pos.yRatio * chartHeight)

                    drawCircle(
                        color = lineColor,
                        radius = strokeWidth * 4f, // Increased size for better visibility and touch targets
                        center = Offset(x, y)
                    )
                }
            } else if (filteredData.size == 1) {
                // Single point
                val pos = pointPositions.first()
                val x = chartLeft + (pos.xRatio * chartWidth)
                val y = chartTop + (pos.yRatio * chartHeight)

                drawCircle(
                    color = lineColor,
                    radius = strokeWidth * 4f, // Increased size for better visibility and touch targets
                    center = Offset(x, y)
                )
            }
        }

        // Show floating tooltip when a point is tapped
        if (showPointInfo && selectedPointIndex >= 0 && selectedPointIndex < filteredData.size && tooltipPosition != null) {
            val selectedPoint = filteredData[selectedPointIndex]
            val date = java.util.Date(selectedPoint.date)
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                               "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val calendar = java.util.Calendar.getInstance().apply { time = date }
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val month = months[calendar.get(java.util.Calendar.MONTH)]
            val dateStr = "$day $month"
            
            val pointOffset = tooltipPosition!!
            val bounds = chartBounds
            val tooltipWidth = 140.dp // Approximate tooltip width
            val tooltipHeight = 40.dp // Approximate tooltip height (adjust based on content)
            val spacing = 12.dp
            
            val tooltipOffsetX = with(density) {
                if (bounds != null) {
                    val chartLeftDp = bounds.left.toDp()
                    val chartRightDp = bounds.right.toDp()
                    val pointXDp = pointOffset.x.toDp()
                    
                    // Try to center tooltip on the point
                    var x = pointXDp - (tooltipWidth / 2)
                    
                    // Keep tooltip within chart bounds
                    x = x.coerceIn(chartLeftDp, chartRightDp - tooltipWidth)
                    
                    x
                } else {
                    // Fallback to canvas bounds if chart bounds not available
                    val canvasWidthDp = canvasSize?.width?.toDp() ?: 0.dp
                    val centerX = pointOffset.x.toDp() - (tooltipWidth / 2)
                    centerX.coerceIn(spacing, canvasWidthDp - tooltipWidth - spacing)
                }
            }
            
            val tooltipOffsetY = with(density) {
                if (bounds != null) {
                    val chartTopDp = bounds.top.toDp()
                    val chartBottomDp = bounds.bottom.toDp()
                    val pointYDp = pointOffset.y.toDp()
                    
                    // Try to position above the point first
                    var yAbove = pointYDp - tooltipHeight - spacing
                    
                    // If not enough space above, position below
                    if (yAbove < chartTopDp) {
                        val yBelow = pointYDp + spacing
                        // If also not enough space below, position at top of chart
                        if (yBelow + tooltipHeight > chartBottomDp) {
                            chartTopDp + spacing
                        } else {
                            yBelow
                        }
                    } else {
                        yAbove
                    }
                } else {
                    // Fallback positioning
                    (pointOffset.y.toDp() - tooltipHeight - spacing).coerceAtLeast(spacing)
                }
            }
            
            FloatingPointTooltip(
                date = dateStr,
                weight = selectedPoint.weight,
                note = selectedPoint.note,
                entryId = selectedPoint.id,
                onDelete = onDeleteEntry?.let { callback ->
                    {
                        showPointInfo = false
                        callback(selectedPoint.id)
                    }
                },
                modifier = Modifier
                    .offset(x = tooltipOffsetX, y = tooltipOffsetY)
                    .wrapContentSize(Alignment.TopCenter)
            )
        }
    }
}

/**
 * Floating tooltip that appears above a data point
 */
@Composable
private fun FloatingPointTooltip(
    date: String,
    weight: Float,
    note: String? = null,
    entryId: Long = 0,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .wrapContentSize(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .wrapContentSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "%.1f kg".format(weight),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (note != null && note.isNotBlank()) {
                Text(
                    text = "• $note",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1
                )
            }
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Internal data class to store point position information
 */
private data class PointPosition(
    val index: Int,
    val xRatio: Float,
    val yRatio: Float,
    val dataPoint: ChartDataPoint
)
