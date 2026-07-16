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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A restrained, deterministic washi texture that adds depth without a bitmap asset. */
@Composable
fun ZenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val fiberColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.035f)
    val fleckColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.028f)

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strandCount = 7
            repeat(strandCount) { index ->
                val y = size.height * (index + 1) / (strandCount + 1)
                val path = Path().apply {
                    moveTo(-24f, y)
                    quadraticBezierTo(
                        size.width * 0.34f,
                        y + if (index % 2 == 0) 11f else -9f,
                        size.width + 24f,
                        y + if (index % 3 == 0) 4f else -3f
                    )
                }
                drawPath(path = path, color = fiberColor, style = Stroke(width = 1f))
            }

            val flecks = listOf(
                Offset(size.width * 0.12f, size.height * 0.17f),
                Offset(size.width * 0.78f, size.height * 0.09f),
                Offset(size.width * 0.91f, size.height * 0.46f),
                Offset(size.width * 0.18f, size.height * 0.74f),
                Offset(size.width * 0.69f, size.height * 0.88f)
            )
            flecks.forEachIndexed { index, center ->
                drawOval(
                    color = fleckColor,
                    topLeft = center,
                    size = Size(9f + index, 2f + index * 0.35f)
                )
            }
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
