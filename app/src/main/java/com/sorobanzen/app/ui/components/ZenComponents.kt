package com.sorobanzen.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Washi drawn rather than photographed: cloudy pulp density, fine grain, and short kōzo bark
 * fibers caught in the sheet.
 * All positions come from fixed seeds, so the sheet is the same on every frame and every device.
 */
private class WashiFiber(
    val x: Float,
    val y: Float,
    val length: Float,
    val angle: Float,
    /** 0..1 across the thickness range; resolved to dp at draw time. */
    val weight: Float
)

// Texture tunables. They live here, not in the seeded lists below, so an edit lands without a
// process restart: a top-level initializer runs once per class load, a draw body runs every frame.
private const val FIBER_POOL = 64
private const val FiberCount = 22          // fibers drawn, from a pool of FIBER_POOL
private const val FiberAlpha = 0.16f       // how far each fiber stands off the paper
private val FiberMinWidth = 0.7.dp         // thinnest fiber
private val FiberWidthSpan = 0.8.dp        // thickest fiber is Min + Span
private val GrainRadius = 0.4.dp

private val washiFibers = Random(1893).let { random ->
    List(FIBER_POOL) {
        WashiFiber(
            x = random.nextFloat(),
            y = random.nextFloat(),
            length = 0.022f + random.nextFloat() * 0.06f,
            angle = random.nextFloat() * 180f,
            weight = random.nextFloat()
        )
    }
}

private val washiGrain = Random(524).let { random ->
    List(280) { Offset(random.nextFloat(), random.nextFloat()) }
}

@Composable
fun ZenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val ink = MaterialTheme.colorScheme.onBackground
    val ochre = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val span = size.minDimension

            // Pulp pools unevenly as the sheet dries, leaving soft blooms of density.
            listOf(
                Triple(Offset(size.width * 0.22f, size.height * 0.18f), span * 0.95f, ink.copy(alpha = 0.05f)),
                Triple(Offset(size.width * 0.84f, size.height * 0.42f), span * 0.78f, ink.copy(alpha = 0.045f)),
                Triple(Offset(size.width * 0.40f, size.height * 0.78f), span * 1.05f, ochre.copy(alpha = 0.06f)),
                Triple(Offset(size.width * 0.08f, size.height * 0.60f), span * 0.66f, ink.copy(alpha = 0.04f)),
                Triple(Offset(size.width * 0.66f, size.height * 0.06f), span * 0.55f, ochre.copy(alpha = 0.035f))
            ).forEach { (center, radius, color) ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color, Color.Transparent),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
            }

            // Fine grain scattered through the pulp.
            val grainColor = ink.copy(alpha = 0.038f)
            val grainRadius = GrainRadius.toPx()
            washiGrain.forEach { point ->
                drawCircle(
                    color = grainColor,
                    radius = grainRadius,
                    center = Offset(point.x * size.width, point.y * size.height)
                )
            }

            // Kōzo bark fibers: short, pale, lying at every angle.
            val fiberColor = ochre.copy(alpha = FiberAlpha)
            val fiberMinWidth = FiberMinWidth.toPx()
            val fiberWidthSpan = FiberWidthSpan.toPx()
            repeat(FiberCount.coerceAtMost(washiFibers.size)) { index ->
                val fiber = washiFibers[index]
                val radians = fiber.angle * PI.toFloat() / 180f
                val length = fiber.length * span
                val start = Offset(fiber.x * size.width, fiber.y * size.height)
                drawLine(
                    color = fiberColor,
                    start = start,
                    end = Offset(
                        start.x + cos(radians) * length,
                        start.y + sin(radians) * length
                    ),
                    strokeWidth = fiberMinWidth + fiber.weight * fiberWidthSpan,
                    cap = StrokeCap.Round
                )
            }

            // The sheet darkens where it curls away at the top and bottom edges.
            val edge = size.height * 0.05f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(ink.copy(alpha = 0.07f), Color.Transparent),
                    startY = 0f,
                    endY = edge
                ),
                size = Size(size.width, edge)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, ink.copy(alpha = 0.06f)),
                    startY = size.height - edge,
                    endY = size.height
                ),
                topLeft = Offset(0f, size.height - edge),
                size = Size(size.width, edge)
            )
        }
        content()
    }
}

/** Small ensō-inspired brand mark, drawn as a vector so it stays crisp at every density. */
@Composable
fun ZenMark(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(34.dp)) {
        val stroke = size.minDimension * 0.095f
        drawArc(
            color = color,
            startAngle = -42f,
            sweepAngle = 306f,
            useCenter = false,
            topLeft = Offset(stroke, stroke),
            size = Size(size.width - stroke * 2, size.height - stroke * 2),
            style = Stroke(width = stroke)
        )
        drawCircle(
            color = color.copy(alpha = 0.58f),
            radius = stroke * 0.72f,
            center = Offset(size.width * 0.79f, size.height * 0.18f)
        )
    }
}

@Composable
fun ZenScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    centered: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    ) {
        if (eyebrow != null) {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start
            )
            Spacer(modifier = Modifier.height(5.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start
            )
        }
    }
}

@Composable
fun ZenCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun ZenChoicePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        border = if (selected) null else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun ZenMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}
