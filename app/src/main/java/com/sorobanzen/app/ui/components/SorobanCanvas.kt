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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import com.sorobanzen.app.R
import kotlin.math.roundToInt
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

    val frameHighlight = if (isDark) Color(0xFF9A8466) else Color(0xFFF5E8CC)
    val frameLight = if (isDark) Color(0xFF78654C) else Color(0xFFE9D5AE)
    val frameMid = if (isDark) Color(0xFF5E4D3A) else Color(0xFFDEC79B)
    val frameDark = if (isDark) Color(0xFF3C3127) else Color(0xFFC5A474)
    val fieldTop = if (isDark) Color(0xFF211F1B) else Color(0xFFF2EEE5)
    val fieldBottom = if (isDark) Color(0xFF171612) else Color(0xFFE9E3D8)
    val rodLight = if (isDark) Color(0xFFE3E0D8) else Color(0xFFF4F2ED)
    val rodColor = if (isDark) Color(0xFFAAA69D) else Color(0xFF9D9B94)
    val rodDark = if (isDark) Color(0xFF56534E) else Color(0xFF5E5D58)
    val beamLight = if (isDark) Color(0xFF8B7456) else Color(0xFFF1DFC0)
    val beamDark = if (isDark) Color(0xFF4E4031) else Color(0xFFD0B483)
    val beadColor = if (isDark) Color(0xFF5876A1) else Color(0xFF254A7A)
    val dotColor = if (isDark) Color(0xFFADC2DF) else Color(0xFF315A8D)
    val surroundColor = MaterialTheme.colorScheme.surfaceVariant

    BoxWithConstraints(
        modifier = modifier
            .background(surroundColor)
            .semantics { contentDescription = accessibilityDescription }
    ) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()
        val frameThickness = (minOf(canvasWidth, canvasHeight) * 0.070f).coerceIn(26f, 60f)
        val fieldInset = frameThickness * 0.92f
        val innerLeft = frameThickness * 1.42f
        val innerRight = canvasWidth - frameThickness * 1.42f
        val innerTop = fieldInset
        val innerBottom = canvasHeight - fieldInset
        val innerHeight = innerBottom - innerTop
        val rodSpacing = (innerRight - innerLeft) / (rodsCount - 1).coerceAtLeast(1)
        val beamHeight = (canvasHeight * 0.050f).coerceIn(20f, 40f)
        val beamTopY = innerTop + innerHeight * 0.38f
        val beamBottomY = beamTopY + beamHeight
        val lowerHeight = innerBottom - beamBottomY
        val beadWidth = (rodSpacing * 0.77f)
            .coerceAtMost(canvasHeight * 0.14f)
            .coerceAtLeast(20f)
        val beadHeight = minOf(beadWidth * 0.60f, innerHeight * 0.085f)
            .coerceAtLeast(13f)
        val beadGap = (innerHeight * 0.008f).coerceIn(3f, 8f)

        fun rodX(index: Int): Float = innerLeft + rodSpacing * index

        fun commitRodValue(rodIndex: Int, nextValue: Int): Boolean {
            val currentValue = rodValues.getOrElse(rodIndex) { 0 }
            val coercedValue = nextValue.coerceIn(0, 9)
            if (coercedValue == currentValue) return false

            onRodValueChange(rodIndex, coercedValue)
            if (hapticsEnabled) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            if (soundEnabled) {
                view.playSoundEffect(SoundEffectConstants.CLICK)
            }
            return true
        }

        fun handleTouch(touchX: Float, touchY: Float) {
            val rodIndex = ((touchX - innerLeft) / rodSpacing)
                .roundToInt()
                .coerceIn(0, rodsCount - 1)
            val currentValue = rodValues.getOrElse(rodIndex) { 0 }
            val heavenActive = currentValue >= 5
            val earthActiveCount = currentValue % 5
            var nextValue = currentValue

            if (touchY < beamTopY) {
                val targetActive = touchY > (innerTop + beamTopY) / 2
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

            commitRodValue(rodIndex, nextValue)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(rodsCount, rodValues, soundEnabled, hapticsEnabled) {
                    detectTapGestures { handleTouch(it.x, it.y) }
                }
                .pointerInput(rodsCount, rodValues, soundEnabled, hapticsEnabled) {
                    detectDragGestures { change, _ ->
                        handleTouch(change.position.x, change.position.y)
                    }
                }
        ) {
            val cornerRadius = CornerRadius(frameThickness * 0.48f)

            // Pale hinoki frame, recessed paper field, and restrained wood grain.
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(frameHighlight, frameLight, frameMid, frameLight, frameDark),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                cornerRadius = cornerRadius
            )
            drawRoundRect(
                color = Color.Black.copy(alpha = if (isDark) 0.36f else 0.18f),
                topLeft = Offset(frameThickness * 0.70f, frameThickness * 0.76f),
                size = Size(
                    size.width - frameThickness * 1.40f,
                    size.height - frameThickness * 1.52f
                ),
                cornerRadius = CornerRadius(frameThickness * 0.23f)
            )

            drawRoundRect(
                brush = Brush.verticalGradient(listOf(fieldTop, fieldBottom)),
                topLeft = Offset(fieldInset, fieldInset),
                size = Size(
                    size.width - fieldInset * 2,
                    size.height - fieldInset * 2
                ),
                cornerRadius = CornerRadius(frameThickness * 0.16f)
            )

            val grainColor = frameDark.copy(alpha = if (isDark) 0.20f else 0.12f)
            repeat(3) { index ->
                val fraction = 0.25f + index * 0.23f
                drawLine(
                    color = grainColor,
                    start = Offset(frameThickness * 0.78f, frameThickness * fraction),
                    end = Offset(size.width - frameThickness * 0.78f, frameThickness * (fraction + 0.04f)),
                    strokeWidth = 1f
                )
                drawLine(
                    color = grainColor,
                    start = Offset(frameThickness * 0.78f, size.height - frameThickness * fraction),
                    end = Offset(size.width - frameThickness * 0.78f, size.height - frameThickness * (fraction + 0.04f)),
                    strokeWidth = 1f
                )
            }

            repeat(rodsCount) { index ->
                val x = rodX(index)
                drawLine(
                    color = Color.Black.copy(alpha = if (isDark) 0.40f else 0.22f),
                    start = Offset(x + 2.2f, innerTop),
                    end = Offset(x + 2.2f, innerBottom),
                    strokeWidth = 7f
                )
                drawLine(
                    color = rodDark,
                    start = Offset(x, innerTop),
                    end = Offset(x, innerBottom),
                    strokeWidth = 5.8f
                )
                drawLine(
                    color = rodColor,
                    start = Offset(x - 0.7f, innerTop),
                    end = Offset(x - 0.7f, innerBottom),
                    strokeWidth = 4.1f
                )
                drawLine(
                    color = rodLight.copy(alpha = if (isDark) 0.52f else 0.86f),
                    start = Offset(x - 1.4f, innerTop),
                    end = Offset(x - 1.4f, innerBottom),
                    strokeWidth = 1.2f
                )
            }

            drawRoundRect(
                color = Color.Black.copy(alpha = if (isDark) 0.34f else 0.18f),
                topLeft = Offset(fieldInset, beamTopY + 3.5f),
                size = Size(size.width - fieldInset * 2, beamHeight),
                cornerRadius = CornerRadius(beamHeight * 0.16f)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(beamLight, beamDark)),
                topLeft = Offset(fieldInset, beamTopY),
                size = Size(size.width - fieldInset * 2, beamHeight),
                cornerRadius = CornerRadius(beamHeight * 0.16f)
            )
            drawLine(
                color = frameHighlight.copy(alpha = if (isDark) 0.28f else 0.68f),
                start = Offset(fieldInset + 4f, beamTopY + 1.2f),
                end = Offset(size.width - fieldInset - 4f, beamTopY + 1.2f),
                strokeWidth = 1.2f
            )

            repeat(rodsCount) { index ->
                if ((rodsCount - 1 - index) % 4 == 3) {
                    drawCircle(
                        color = dotColor,
                        radius = (beamHeight * 0.20f).coerceAtLeast(2.5f),
                        center = Offset(
                            x = rodX(index),
                            y = beamTopY + beamHeight / 2
                        )
                    )
                }
            }

            repeat(rodsCount) { index ->
                val x = rodX(index)
                val heavenFactor = heavenAnimatables[index].value
                val minHeavenY = innerTop + beadHeight / 2 + 12f
                val maxHeavenY = beamTopY - beadHeight / 2 - 9f
                val heavenY = minHeavenY + (maxHeavenY - minHeavenY) * heavenFactor

                drawSorobanBead(
                    centerX = x,
                    centerY = heavenY,
                    beadWidth = beadWidth,
                    beadHeight = beadHeight,
                    color = beadColor,
                    rodColor = rodColor,
                    dark = isDark
                )

                repeat(4) { beadIndex ->
                    val factor = earthAnimatables[index][beadIndex].value
                    val activeY = beamBottomY + beadHeight / 2 + 9f +
                        beadIndex * (beadHeight + beadGap)
                    val inactiveY = innerBottom - beadHeight / 2 - 10f -
                        (3 - beadIndex) * (beadHeight + beadGap)
                    val beadY = inactiveY + (activeY - inactiveY) * factor

                    drawSorobanBead(
                        centerX = x,
                        centerY = beadY,
                        beadWidth = beadWidth,
                        beadHeight = beadHeight,
                        color = beadColor,
                        rodColor = rodColor,
                        dark = isDark
                    )
                }
            }
        }

        val clearRodAction = stringResource(id = R.string.soroban_rod_clear_action)
        Row(modifier = Modifier.fillMaxSize()) {
            repeat(rodsCount) { index ->
                val currentValue = rodValues.getOrElse(index) { 0 }
                val positionFromRight = rodsCount - index
                val rodDescription = stringResource(
                    id = R.string.soroban_rod_accessibility,
                    positionFromRight,
                    currentValue
                )
                val rodState = stringResource(id = R.string.soroban_rod_state, currentValue)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics {
                            contentDescription = rodDescription
                            stateDescription = rodState
                            progressBarRangeInfo = ProgressBarRangeInfo(
                                current = currentValue.toFloat(),
                                range = 0f..9f,
                                steps = 8
                            )
                            setProgress { targetValue ->
                                commitRodValue(index, targetValue.roundToInt())
                            }
                            customActions = listOf(
                                CustomAccessibilityAction(clearRodAction) {
                                    commitRodValue(index, 0)
                                }
                            )
                        }
                )
            }
        }
    }
}

@Composable
fun SorobanGuidePreview(
    accessibilityDescription: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val fieldTop = if (isDark) Color(0xFF211F1B) else Color(0xFFF0ECE3)
    val fieldBottom = if (isDark) Color(0xFF171612) else Color(0xFFE5DFD3)
    val frameColor = if (isDark) Color(0xFF78654C) else Color(0xFFE5CE9F)
    val rodColor = if (isDark) Color(0xFFAAA69D) else Color(0xFF92918B)
    val beamColor = if (isDark) Color(0xFF755F47) else Color(0xFFDEC69B)
    val beadColor = if (isDark) Color(0xFF5876A1) else Color(0xFF254A7A)
    val dotColor = if (isDark) Color(0xFFADC2DF) else Color(0xFF315A8D)

    Canvas(
        modifier = modifier.semantics {
            contentDescription = accessibilityDescription
        }
    ) {
        val inset = 3f
        val beamHeight = (size.height * 0.055f).coerceAtLeast(7f)
        val beamTop = size.height * 0.42f
        val centerX = size.width / 2f
        val beadWidth = size.width * 0.54f
        val beadHeight = (size.height * 0.105f).coerceAtMost(beadWidth * 0.56f)

        drawRoundRect(
            brush = Brush.verticalGradient(listOf(fieldTop, fieldBottom)),
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = CornerRadius(16f)
        )
        drawRoundRect(
            color = frameColor,
            cornerRadius = CornerRadius(16f),
            style = Stroke(width = 6f)
        )
        drawLine(
            color = rodColor,
            start = Offset(centerX, inset + 4f),
            end = Offset(centerX, size.height - inset - 4f),
            strokeWidth = 3f
        )
        drawRect(
            color = beamColor,
            topLeft = Offset(inset + 3f, beamTop),
            size = Size(size.width - inset * 2 - 6f, beamHeight)
        )
        drawCircle(
            color = dotColor,
            radius = (beamHeight * 0.22f).coerceAtLeast(2.5f),
            center = Offset(centerX, beamTop + beamHeight / 2f)
        )

        drawSorobanBead(
            centerX = centerX,
            centerY = beamTop - beadHeight / 2f - 6f,
            beadWidth = beadWidth,
            beadHeight = beadHeight,
            color = beadColor,
            rodColor = rodColor,
            dark = isDark
        )

        repeat(4) { beadIndex ->
            val active = beadIndex < 2
            val centerY = if (active) {
                beamTop + beamHeight + beadHeight / 2f + 6f +
                    beadIndex * (beadHeight + 4f)
            } else {
                size.height - beadHeight / 2f - 10f -
                    (3 - beadIndex) * (beadHeight + 4f)
            }
            drawSorobanBead(
                centerX = centerX,
                centerY = centerY,
                beadWidth = beadWidth,
                beadHeight = beadHeight,
                color = beadColor,
                rodColor = rodColor,
                dark = isDark
            )
        }
    }
}

private fun DrawScope.drawSorobanBead(
    centerX: Float,
    centerY: Float,
    beadWidth: Float,
    beadHeight: Float,
    color: Color,
    rodColor: Color,
    dark: Boolean
) {
    fun beadPath(yOffset: Float = 0f) = Path().apply {
        val left = centerX - beadWidth / 2
        val right = centerX + beadWidth / 2
        val top = centerY - beadHeight / 2 + yOffset
        val bottom = centerY + beadHeight / 2 + yOffset
        val middle = centerY + yOffset
        moveTo(left + beadWidth * 0.29f, top)
        quadraticBezierTo(left + beadWidth * 0.22f, top, left + beadWidth * 0.16f, top + beadHeight * 0.17f)
        lineTo(left + beadWidth * 0.035f, middle - beadHeight * 0.10f)
        quadraticBezierTo(left, middle, left + beadWidth * 0.035f, middle + beadHeight * 0.10f)
        lineTo(left + beadWidth * 0.16f, bottom - beadHeight * 0.17f)
        quadraticBezierTo(left + beadWidth * 0.22f, bottom, left + beadWidth * 0.29f, bottom)
        lineTo(right - beadWidth * 0.29f, bottom)
        quadraticBezierTo(right - beadWidth * 0.22f, bottom, right - beadWidth * 0.16f, bottom - beadHeight * 0.17f)
        lineTo(right - beadWidth * 0.035f, middle + beadHeight * 0.10f)
        quadraticBezierTo(right, middle, right - beadWidth * 0.035f, middle - beadHeight * 0.10f)
        lineTo(right - beadWidth * 0.16f, top + beadHeight * 0.17f)
        quadraticBezierTo(right - beadWidth * 0.22f, top, right - beadWidth * 0.29f, top)
        close()
    }

    val left = centerX - beadWidth / 2
    val right = centerX + beadWidth / 2
    val top = centerY - beadHeight / 2
    val bottom = centerY + beadHeight / 2

    drawPath(
        path = beadPath(yOffset = 3.4f),
        color = Color.Black.copy(alpha = if (dark) 0.48f else 0.30f)
    )
    drawPath(
        path = beadPath(),
        brush = Brush.verticalGradient(
            colors = listOf(
                color.copy(
                    red = (color.red + 0.12f).coerceAtMost(1f),
                    green = (color.green + 0.12f).coerceAtMost(1f),
                    blue = (color.blue + 0.12f).coerceAtMost(1f)
                ),
                color,
                color.copy(
                    red = color.red * 0.66f,
                    green = color.green * 0.66f,
                    blue = color.blue * 0.66f
                )
            ),
            startY = top,
            endY = bottom
        )
    )
    drawPath(
        path = beadPath(),
        color = Color.Black.copy(alpha = if (dark) 0.34f else 0.25f),
        style = Stroke(width = 1.1f)
    )

    clipPath(beadPath()) {
        drawLine(
            color = Color.White.copy(alpha = if (dark) 0.14f else 0.20f),
            start = Offset(left + beadWidth * 0.18f, top + beadHeight * 0.25f),
            end = Offset(right - beadWidth * 0.18f, top + beadHeight * 0.23f),
            strokeWidth = 1.2f
        )
        repeat(2) { index ->
            val y = centerY + beadHeight * (0.08f + index * 0.13f)
            drawLine(
                color = Color.Black.copy(alpha = 0.11f),
                start = Offset(left + beadWidth * (0.20f + index * 0.03f), y),
                end = Offset(right - beadWidth * (0.18f + index * 0.04f), y - 0.7f),
                strokeWidth = 0.8f
            )
        }
    }

    val holeWidth = (beadWidth * 0.085f).coerceAtLeast(3f)
    val holeHeight = (beadHeight * 0.16f).coerceAtLeast(2f)
    val holeColor = Color.Black.copy(alpha = if (dark) 0.46f else 0.38f)
    drawOval(
        color = holeColor,
        topLeft = Offset(centerX - holeWidth / 2, top - holeHeight * 0.18f),
        size = Size(holeWidth, holeHeight)
    )
    drawOval(
        color = holeColor,
        topLeft = Offset(centerX - holeWidth / 2, bottom - holeHeight * 0.82f),
        size = Size(holeWidth, holeHeight)
    )
    drawLine(
        color = rodColor.copy(alpha = 0.72f),
        start = Offset(centerX, top),
        end = Offset(centerX, top + holeHeight * 0.46f),
        strokeWidth = (holeWidth * 0.24f).coerceAtLeast(1f)
    )
}
