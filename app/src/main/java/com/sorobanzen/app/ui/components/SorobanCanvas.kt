package com.sorobanzen.app.ui.components

import android.view.SoundEffectConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch

@Composable
fun SorobanCanvas(
    rodsCount: Int,
    rodValues: IntArray,
    onRodValueChange: (rodIndex: Int, newValue: Int) -> Unit,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    accessibilityDescription: String,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val isDark = isSystemInDarkTheme()

    val heavenAnimatables = remember(rodsCount) {
        List(rodsCount) { Animatable(0f) }
    }
    val earthAnimatables = remember(rodsCount) {
        List(rodsCount) { List(4) { Animatable(0f) } }
    }

    LaunchedEffect(rodValues, rodsCount) {
        for (index in 0 until rodsCount.coerceAtMost(rodValues.size)) {
            val rodValue = rodValues[index]
            launch {
                heavenAnimatables[index].animateTo(
                    targetValue = if (rodValue >= 5) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            repeat(4) { beadIndex ->
                launch {
                    earthAnimatables[index][beadIndex].animateTo(
                        targetValue = if (beadIndex < rodValue % 5) 1f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            }
        }
    }

    val frameLight = if (isDark) Color(0xFF5D4939) else Color(0xFF9A7452)
    val frameMid = if (isDark) Color(0xFF3B2D24) else Color(0xFF684936)
    val frameDark = if (isDark) Color(0xFF251D18) else Color(0xFF463025)
    val fieldTop = if (isDark) Color(0xFF191815) else Color(0xFFF0EBDD)
    val fieldBottom = if (isDark) Color(0xFF11110F) else Color(0xFFE3DCCB)
    val rodColor = if (isDark) Color(0xFF777268) else Color(0xFF8B8376)
    val beamLight = if (isDark) Color(0xFF39362F) else Color(0xFFE7E0D2)
    val beamDark = if (isDark) Color(0xFF22211D) else Color(0xFFBDB4A3)
    val beadColor = if (isDark) Color(0xFFC08B58) else Color(0xFF8C542F)
    val dotColor = if (isDark) Color(0xFFE6DDCE) else Color(0xFF342F29)
    val surroundColor = MaterialTheme.colorScheme.surfaceVariant

    BoxWithConstraints(
        modifier = modifier.background(surroundColor)
    ) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()
        val rodWidth = canvasWidth / rodsCount
        val frameThickness = (minOf(canvasWidth, canvasHeight) * 0.048f).coerceIn(14f, 26f)
        val beamHeight = (canvasHeight * 0.046f).coerceIn(12f, 19f)
        val upperHeight = canvasHeight * 0.255f
        val topY = frameThickness
        val beamTopY = topY + upperHeight
        val beamBottomY = beamTopY + beamHeight
        val bottomY = canvasHeight - frameThickness
        val lowerHeight = bottomY - beamBottomY
        val beadHeight = minOf(
            (upperHeight - 24f) * 0.66f,
            (lowerHeight - 32f) / 4.55f
        ).coerceAtLeast(12f)
        val beadWidth = (rodWidth * 0.80f).coerceAtLeast(10f)

        fun handleTouch(touchX: Float, touchY: Float) {
            val rodIndex = (touchX / rodWidth).toInt().coerceIn(0, rodsCount - 1)
            val currentValue = rodValues.getOrElse(rodIndex) { 0 }
            val heavenActive = currentValue >= 5
            val earthActiveCount = currentValue % 5
            var nextValue = currentValue

            if (touchY < beamTopY) {
                val targetActive = touchY > (topY + beamTopY) / 2
                if (targetActive != heavenActive) {
                    nextValue = (if (targetActive) 5 else 0) + earthActiveCount
                }
            } else if (touchY > beamBottomY) {
                val fraction = ((touchY - beamBottomY) / lowerHeight).coerceIn(0f, 1f)
                val targetEarthCount = when {
                    fraction < 0.16f -> 4
                    fraction < 0.40f -> 3
                    fraction < 0.64f -> 2
                    fraction < 0.86f -> 1
                    else -> 0
                }
                if (targetEarthCount != earthActiveCount) {
                    nextValue = (if (heavenActive) 5 else 0) + targetEarthCount
                }
            }

            if (nextValue != currentValue) {
                onRodValueChange(rodIndex, nextValue)
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
                .semantics {
                    contentDescription = accessibilityDescription
                }
                .pointerInput(rodsCount, rodValues, soundEnabled, hapticsEnabled) {
                    detectTapGestures { handleTouch(it.x, it.y) }
                }
                .pointerInput(rodsCount, rodValues, soundEnabled, hapticsEnabled) {
                    detectDragGestures { change, _ ->
                        handleTouch(change.position.x, change.position.y)
                    }
                }
        ) {
            val cornerRadius = CornerRadius(frameThickness * 0.72f)

            // Layered wood frame.
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(frameLight, frameMid, frameDark),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                cornerRadius = cornerRadius
            )
            drawRoundRect(
                color = Color.Black.copy(alpha = if (isDark) 0.28f else 0.16f),
                topLeft = Offset(frameThickness * 0.62f, frameThickness * 0.68f),
                size = Size(
                    size.width - frameThickness * 1.24f,
                    size.height - frameThickness * 1.36f
                ),
                cornerRadius = CornerRadius(frameThickness * 0.35f),
                style = Stroke(width = 2f)
            )

            // Recessed paper field.
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(fieldTop, fieldBottom)),
                topLeft = Offset(frameThickness, frameThickness),
                size = Size(
                    size.width - frameThickness * 2,
                    size.height - frameThickness * 2
                ),
                cornerRadius = CornerRadius(frameThickness * 0.26f)
            )

            repeat(rodsCount) { index ->
                val rodX = rodWidth * index + rodWidth / 2
                drawLine(
                    color = Color.Black.copy(alpha = 0.18f),
                    start = Offset(rodX + 1.6f, frameThickness),
                    end = Offset(rodX + 1.6f, size.height - frameThickness),
                    strokeWidth = 5f
                )
                drawLine(
                    color = rodColor,
                    start = Offset(rodX, frameThickness),
                    end = Offset(rodX, size.height - frameThickness),
                    strokeWidth = 3.3f
                )
                drawLine(
                    color = Color.White.copy(alpha = if (isDark) 0.08f else 0.28f),
                    start = Offset(rodX - 0.8f, frameThickness),
                    end = Offset(rodX - 0.8f, size.height - frameThickness),
                    strokeWidth = 0.9f
                )
            }

            drawRect(
                brush = Brush.verticalGradient(listOf(beamLight, beamDark)),
                topLeft = Offset(frameThickness, beamTopY),
                size = Size(size.width - frameThickness * 2, beamHeight)
            )
            drawLine(
                color = Color.White.copy(alpha = if (isDark) 0.08f else 0.36f),
                start = Offset(frameThickness, beamTopY + 1f),
                end = Offset(size.width - frameThickness, beamTopY + 1f),
                strokeWidth = 1.4f
            )

            repeat(rodsCount) { index ->
                if ((rodsCount - 1 - index) % 4 == 3) {
                    drawCircle(
                        color = dotColor,
                        radius = (beamHeight * 0.20f).coerceAtLeast(2.5f),
                        center = Offset(
                            x = rodWidth * index + rodWidth / 2,
                            y = beamTopY + beamHeight / 2
                        )
                    )
                }
            }

            repeat(rodsCount) { index ->
                val rodX = rodWidth * index + rodWidth / 2
                val heavenFactor = heavenAnimatables[index].value
                val minHeavenY = topY + beadHeight / 2 + 8f
                val maxHeavenY = beamTopY - beadHeight / 2 - 7f
                val heavenY = minHeavenY + (maxHeavenY - minHeavenY) * heavenFactor

                drawSorobanBead(
                    centerX = rodX,
                    centerY = heavenY,
                    beadWidth = beadWidth,
                    beadHeight = beadHeight,
                    color = beadColor,
                    dark = isDark
                )

                repeat(4) { beadIndex ->
                    val factor = earthAnimatables[index][beadIndex].value
                    val activeY = beamBottomY + beadHeight / 2 + 7f +
                        beadIndex * (beadHeight + 3.5f)
                    val inactiveY = bottomY - beadHeight / 2 - 7f -
                        (3 - beadIndex) * (beadHeight + 3.5f)
                    val beadY = inactiveY + (activeY - inactiveY) * factor

                    drawSorobanBead(
                        centerX = rodX,
                        centerY = beadY,
                        beadWidth = beadWidth,
                        beadHeight = beadHeight,
                        color = beadColor,
                        dark = isDark
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawSorobanBead(
    centerX: Float,
    centerY: Float,
    beadWidth: Float,
    beadHeight: Float,
    color: Color,
    dark: Boolean
) {
    fun beadPath(yOffset: Float = 0f) = Path().apply {
        val left = centerX - beadWidth / 2
        val right = centerX + beadWidth / 2
        val top = centerY - beadHeight / 2 + yOffset
        val bottom = centerY + beadHeight / 2 + yOffset
        val middle = centerY + yOffset
        moveTo(centerX, top)
        lineTo(right, middle - beadHeight * 0.11f)
        lineTo(right, middle + beadHeight * 0.11f)
        lineTo(centerX, bottom)
        lineTo(left, middle + beadHeight * 0.11f)
        lineTo(left, middle - beadHeight * 0.11f)
        close()
    }

    val left = centerX - beadWidth / 2
    val right = centerX + beadWidth / 2
    val top = centerY - beadHeight / 2
    val bottom = centerY + beadHeight / 2

    drawPath(
        path = beadPath(yOffset = 2.2f),
        color = Color.Black.copy(alpha = if (dark) 0.38f else 0.25f)
    )
    drawPath(
        path = beadPath(),
        brush = Brush.linearGradient(
            colors = listOf(
                color.copy(red = (color.red + 0.16f).coerceAtMost(1f)),
                color,
                color.copy(red = color.red * 0.72f, green = color.green * 0.72f, blue = color.blue * 0.72f)
            ),
            start = Offset(left, top),
            end = Offset(right, bottom)
        )
    )
    drawLine(
        color = Color.White.copy(alpha = if (dark) 0.16f else 0.22f),
        start = Offset(left + beadWidth * 0.14f, centerY - 1f),
        end = Offset(right - beadWidth * 0.14f, centerY - 1f),
        strokeWidth = 1.3f
    )
    drawLine(
        color = Color.Black.copy(alpha = 0.18f),
        start = Offset(left + beadWidth * 0.18f, centerY + beadHeight * 0.08f),
        end = Offset(right - beadWidth * 0.18f, centerY + beadHeight * 0.08f),
        strokeWidth = 1f
    )
}
