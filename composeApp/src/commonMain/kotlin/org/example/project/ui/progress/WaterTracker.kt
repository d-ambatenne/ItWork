package org.example.project.ui.progress

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WaterTrackerSection(
    cupsCount: Int,
    onAddCup: () -> Unit,
    onDeleteCup: () -> Unit,
    isNewCupAdded: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Water Intake",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$cupsCount cups",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Cups visualization
            val scrollState = rememberScrollState()
            LaunchedEffect(cupsCount) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(cupsCount) { index ->
                    WaterCup(
                        isAnimating = isNewCupAdded && index == cupsCount - 1,
                        onClick = onDeleteCup,
                        modifier = Modifier.size(50.dp)
                    )
                }
                
                // Add cup button
                if (cupsCount < 20) { // Limit to 20 cups for UI
                    AddCupButton(
                        onClick = onAddCup,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WaterCup(
    isAnimating: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cupColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
    
    // Animation key to restart fill animation for new cups
    val animationKey = remember(isAnimating) { 
        if (isAnimating) System.currentTimeMillis() else 0L 
    }
    
    // Water fill progress: 0 = empty, 1 = full
    var fillProgress by remember { mutableStateOf(if (isAnimating) 0f else 1f) }
    
    // Simple water fill animation: bottom to top
    LaunchedEffect(animationKey) {
        if (animationKey > 0) {
            fillProgress = 0f
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 5000,
                    easing = LinearEasing
                )
            ) { value, _ ->
                fillProgress = value
            }
        } else {
            fillProgress = 1f
        }
    }
    
    Box(
        modifier = modifier
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            
            // Coffee cup dimensions
            val cupTopWidth = width * 0.65f
            val cupBottomWidth = width * 0.45f
            val cupHeight = height * 0.7f
            val cupTopY = height * 0.15f
            val cupBottomY = cupTopY + cupHeight
            val rimThickness = 2.dp.toPx()
            
            // Draw cup body with rounded sides (coffee cup shape)
            val cupPath = Path().apply {
                // Left side - curved outward
                moveTo(centerX - cupBottomWidth / 2, cupBottomY)
                cubicTo(
                    x1 = centerX - cupBottomWidth / 2 - width * 0.05f,
                    y1 = cupBottomY - cupHeight * 0.3f,
                    x2 = centerX - cupTopWidth / 2 - width * 0.02f,
                    y2 = cupTopY + cupHeight * 0.2f,
                    x3 = centerX - cupTopWidth / 2,
                    y3 = cupTopY
                )
                
                // Top rim
                lineTo(centerX + cupTopWidth / 2, cupTopY)
                
                // Right side - curved outward
                cubicTo(
                    x1 = centerX + cupTopWidth / 2 + width * 0.02f,
                    y1 = cupTopY + cupHeight * 0.2f,
                    x2 = centerX + cupBottomWidth / 2 + width * 0.05f,
                    y2 = cupBottomY - cupHeight * 0.3f,
                    x3 = centerX + cupBottomWidth / 2,
                    y3 = cupBottomY
                )
                
                // Bottom
                lineTo(centerX - cupBottomWidth / 2, cupBottomY)
                close()
            }
            
            // Draw cup outline
            drawPath(
                path = cupPath,
                color = cupColor,
                style = Stroke(width = 1.5.dp.toPx())
            )
            
            // Draw top rim
            drawLine(
                color = cupColor,
                start = Offset(centerX - cupTopWidth / 2, cupTopY),
                end = Offset(centerX + cupTopWidth / 2, cupTopY),
                strokeWidth = rimThickness
            )
            
            // Simple water fill: bottom to top
            if (fillProgress > 0f) {
                val waterHeight = cupHeight * fillProgress
                val waterTopY = cupBottomY - waterHeight
                val waterSurfaceWidth = cupBottomWidth + (cupTopWidth - cupBottomWidth) * fillProgress
                
                val waterPath = Path().apply {
                    moveTo(centerX - cupBottomWidth / 2, cupBottomY)
                    lineTo(centerX + cupBottomWidth / 2, cupBottomY)
                    lineTo(centerX + waterSurfaceWidth / 2, waterTopY)
                    lineTo(centerX - waterSurfaceWidth / 2, waterTopY)
                    close()
                }
                
                drawPath(
                    path = waterPath,
                    color = Color(0xFF4A90E2).copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun AddCupButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Cup",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

