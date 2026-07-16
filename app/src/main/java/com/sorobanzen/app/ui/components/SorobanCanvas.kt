package com.sorobanzen.app.ui.components

import android.view.SoundEffectConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.sorobanzen.app.ui.theme.LightAccentMoss
import kotlinx.coroutines.launch

@Composable
fun SorobanCanvas(
    rodsCount: Int,
    rodValues: IntArray,
    onRodValueChange: (rodIndex: Int, newValue: Int) -> Unit,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    // We represent the animated positions of beads.
    // For each rod, we have 1 heaven bead Y factor (0.0 = top/inactive, 1.0 = bottom/active)
    // and 4 earth beads Y factors (0.0 = bottom/inactive, 1.0 = top/active).
    // Let's create Animatable states for all rods to animate their values smoothly.
    
    val heavenAnimatables = remember(rodsCount) {
        List(rodsCount) { Animatable(0f) }
    }
    
    val earthAnimatables = remember(rodsCount) {
        List(rodsCount) {
            List(4) { Animatable(0f) }
        }
    }

    // Synchronize animatable targets with current rod values
    LaunchedEffect(rodValues, rodsCount) {
        for (i in 0 until rodsCount.coerceAtMost(rodValues.size)) {
            val valForRod = rodValues[i]
            val heavenActive = valForRod >= 5
            val earthActiveCount = valForRod % 5
            
            // Animate heaven bead
            scope.launch {
                heavenAnimatables[i].animateTo(
                    targetValue = if (heavenActive) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            
            // Animate 4 earth beads
            for (b in 0..3) {
                val beadActive = b < earthActiveCount
                scope.launch {
                    earthAnimatables[i][b].animateTo(
                        targetValue = if (beadActive) 1f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            }
        }
    }

    // Outer frame coloring
    val themeColor = MaterialTheme.colorScheme.primary
    val isDark = MaterialTheme.colorScheme.background.value == 0xFF121212.toULong().value
    
    val frameColor = if (isDark) Color(0xFF2E241E) else Color(0xFF5C4033) // Wood brown
    val rodColor = if (isDark) Color(0xFF4A4A4A) else Color(0xFF8A8A8A)
    val beamColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFE5E2D9)
    val beadColorPrimary = if (isDark) Color(0xFFD3A370) else Color(0xFF8B5A2B) // Polished wood beads
    val dotColor = if (isDark) Color(0xFFECEAE4) else Color(0xFF1E1E1E)

    BoxWithConstraints(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        
        val rodWidth = width / rodsCount
        
        // Define Y coordinates
        val frameThickness = 24f
        val upperHeight = height * 0.25f
        val beamHeight = 16f
        
        val topY = frameThickness
        val beamTopY = topY + upperHeight
        val beamBottomY = beamTopY + beamHeight
        val bottomY = height - frameThickness
        
        val beadHeight = (upperHeight - 20f) * 0.8f // size of bead
        
        // Function to handle touches and update rod value
        fun handleTouch(touchX: Float, touchY: Float) {
            val rodIndex = (touchX / rodWidth).toInt().coerceIn(0, rodsCount - 1)
            val currentVal = rodValues.getOrElse(rodIndex) { 0 }
            
            val heavenActive = currentVal >= 5
            val earthActiveCount = currentVal % 5
            
            var newVal = currentVal
            
            if (touchY < beamTopY) {
                // Touched heaven region
                val midHeavenY = (topY + beamTopY) / 2
                val targetHeavenActive = touchY > midHeavenY
                if (heavenActive != targetHeavenActive) {
                    newVal = (if (targetHeavenActive) 5 else 0) + earthActiveCount
                }
            } else if (touchY > beamBottomY) {
                // Touched earth region
                // Map the vertical height in earth region to 0-4 beads active
                val earthRegionHeight = bottomY - beamBottomY
                val relativeY = touchY - beamBottomY
                val fraction = (relativeY / earthRegionHeight).coerceIn(0f, 1f)
                
                // If we touch close to divider, we activate more beads (e.g. 4), close to bottom we activate fewer
                val targetEarthCount = when {
                    fraction < 0.15f -> 4
                    fraction < 0.40f -> 3
                    fraction < 0.65f -> 2
                    fraction < 0.85f -> 1
                    else -> 0
                }
                
                if (earthActiveCount != targetEarthCount) {
                    newVal = (if (heavenActive) 5 else 0) + targetEarthCount
                }
            }
            
            if (newVal != currentVal) {
                onRodValueChange(rodIndex, newVal)
                
                // Satisfying triggers
                if (hapticsEnabled) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                if (soundEnabled) {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(rodsCount) {
                    detectTapGestures { offset ->
                        handleTouch(offset.x, offset.y)
                    }
                }
                .pointerInput(rodsCount) {
                    detectDragGestures { change, _ ->
                        handleTouch(change.position.x, change.position.y)
                    }
                }
        ) {
            // 1. Draw frame background (shadow)
            drawRect(
                color = frameColor,
                topLeft = Offset(0f, 0f),
                size = Size(width, height)
            )
            
            // Draw inner background area (where rods are)
            drawRect(
                color = if (isDark) Color(0xFF181818) else Color(0xFFF4F0E6),
                topLeft = Offset(frameThickness, frameThickness),
                size = Size(width - frameThickness * 2, height - frameThickness * 2)
            )
            
            // 2. Draw Rods
            for (i in 0 until rodsCount) {
                val rodX = rodWidth * i + rodWidth / 2
                drawLine(
                    color = rodColor,
                    start = Offset(rodX, frameThickness),
                    end = Offset(rodX, height - frameThickness),
                    strokeWidth = 6f
                )
            }
            
            // 3. Draw Divider Beam (Horizontal)
            drawRect(
                color = beamColor,
                topLeft = Offset(frameThickness, beamTopY),
                size = Size(width - frameThickness * 2, beamHeight)
            )
            
            // 4. Draw Traditional Alignment Dots (index points)
            // On standard 13-rod abacus, dots are placed on every 3rd or 4th rod
            // E.g., for 13 rods, let's place dots on rods 2, 6, 10
            for (i in 0 until rodsCount) {
                if ((rodsCount - 1 - i) % 4 == 3) {
                    val dotX = rodWidth * i + rodWidth / 2
                    val dotY = beamTopY + beamHeight / 2
                    drawCircle(
                        color = dotColor,
                        radius = 4f,
                        center = Offset(dotX, dotY)
                    )
                }
            }
            
            // 5. Draw Beads
            val beadW = rodWidth * 0.85f
            
            for (i in 0 until rodsCount) {
                val rodX = rodWidth * i + rodWidth / 2
                
                // --- Draw Heaven Bead ---
                // Animated factor ranges from 0f (top) to 1f (divider)
                val heavenFactor = heavenAnimatables[i].value
                val minHeavenY = topY + beadHeight / 2 + 10f
                val maxHeavenY = beamTopY - beadHeight / 2 - 8f
                val heavenY = minHeavenY + (maxHeavenY - minHeavenY) * heavenFactor
                
                drawSorobanBead(
                    centerX = rodX,
                    centerY = heavenY,
                    beadWidth = beadW,
                    beadHeight = beadHeight,
                    color = beadColorPrimary
                )
                
                // --- Draw Earth Beads ---
                // Animated factor for each of 4 earth beads: 0f (bottom/inactive) to 1f (top/active)
                val earthFactors = earthAnimatables[i]
                
                for (b in 0..3) {
                    val factor = earthFactors[b].value
                    
                    // Positions if active (resting up near divider):
                    // Bead 0 (top-most): close to divider
                    // Bead 3 (bottom-most): further down
                    val activeY = beamBottomY + beadHeight / 2 + 8f + (b * (beadHeight + 4f))
                    
                    // Positions if inactive (resting down at bottom):
                    // Bead 3 (bottom-most): close to frame
                    // Bead 0 (top-most): higher up
                    val inactiveY = bottomY - beadHeight / 2 - 8f - ((3 - b) * (beadHeight + 4f))
                    
                    val beadY = inactiveY + (activeY - inactiveY) * factor
                    
                    drawSorobanBead(
                        centerX = rodX,
                        centerY = beadY,
                        beadWidth = beadW,
                        beadHeight = beadHeight,
                        color = beadColorPrimary
                    )
                }
            }
        }
    }
}

/**
 * Draws a traditional bi-conical Soroban bead (soroban-dama).
 * It resembles two cones joined at their bases, looking like a diamond with a flat sharp horizontal edge.
 */
private fun DrawScope.drawSorobanBead(
    centerX: Float,
    centerY: Float,
    beadWidth: Float,
    beadHeight: Float,
    color: Color
) {
    val left = centerX - beadWidth / 2
    val right = centerX + beadWidth / 2
    val top = centerY - beadHeight / 2
    val bottom = centerY + beadHeight / 2
    val midY = centerY
    
    // Draw diamond path
    val path = Path().apply {
        moveTo(centerX, top) // Top point
        lineTo(right, midY - beadHeight * 0.1f) // Join base right upper
        lineTo(right, midY + beadHeight * 0.1f) // Join base right lower (slight thickness)
        lineTo(centerX, bottom) // Bottom point
        lineTo(left, midY + beadHeight * 0.1f) // Join base left lower
        lineTo(left, midY - beadHeight * 0.1f) // Join base left upper
        close()
    }
    
    // Gradient brush for realistic wood polish
    val brush = Brush.linearGradient(
        colors = listOf(
            color.copy(alpha = 0.9f),
            color,
            color.copy(alpha = 0.6f)
        ),
        start = Offset(left, top),
        end = Offset(right, bottom)
    )
    
    drawPath(path = path, brush = brush)
    
    // Inner shadow highlight line
    drawLine(
        color = Color.White.copy(alpha = 0.15f),
        start = Offset(left, midY),
        end = Offset(right, midY),
        strokeWidth = 2f
    )
}
