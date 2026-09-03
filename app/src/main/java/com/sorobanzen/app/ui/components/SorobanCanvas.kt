package com.sorobanzen.app.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.launch

/*
 * 黒檀と骨 — Ebony & Bone.
 *
 * Every number below is in the design reference's own coordinate space: a 768 x 352 board. At
 * draw time one design unit becomes `boardHeight / BOARD_HEIGHT` canvas pixels, so the whole
 * instrument scales as one piece and the handoff can still be read line by line against the code.
 * Only the rod pitch depends on how many rods there are.
 */
private const val BOARD_HEIGHT = 352f
private const val BOARD_RADIUS = 9f

private const val INLAY_INSET_X = 20f
private const val INLAY_INSET_Y = 12f
private const val INLAY_RADIUS = 4f

private const val FIELD_INSET_X = 34f
private const val FIELD_INSET_Y = 26f
private const val FIELD_RADIUS = 2f
/** How far the field's recessed inner shadow reaches down from its top edge. */
private const val FIELD_RECESS = 13f

/*
 * The one place this departs from the handoff, which puts the beam at 140.
 *
 * The lower deck has to hold a 135-unit stack of four beads; the upper deck holds one. At 140 the
 * lower deck's 168 units left each earth bead 15 units of travel while the heaven bead had 66, so
 * the lower deck read as packed wall to wall and the upper as empty. Raising the beam moves the
 * slack to the deck that needs it: the earth beads now travel 45 and the heaven bead 36, which is
 * still more than its own height. Everything else about the beam is unchanged.
 */
private const val BEAM_TOP = 110f
private const val BEAM_HEIGHT = 18f
/** How far the beam's shadow falls onto the field below it. */
private const val BEAM_SHADOW = 16f

private const val BEAD_WIDTH = 80f
private const val BEAD_HEIGHT = 30f
private const val BEAD_GAP = 5f
private const val BEAD_TO_BEAM = 8f
private const val BEAD_TO_FIELD = 10f

private const val ROD_WIDTH = 2f
private const val MARKER_SIZE = 8f

/** Outer bead half-width plus the margin the design keeps between it and the field edge. */
private const val BEAD_EDGE = BEAD_WIDTH / 2f + 8f

/** The reference board's pitch, `(700 - 2 * BEAD_EDGE) / 6` across its seven rods. */
private const val ROD_PITCH = 100.667f

// Ebony frame.
private val EbonyLight = Color(0xFF2A231C)
private val EbonyMid = Color(0xFF181310)
private val EbonyDark = Color(0xFF0C0908)
private val BeamLight = Color(0xFF241D17)
private val BeamDark = Color(0xFF120E0B)

// Bone reckoning field.
private val BoneLight = Color(0xFFEFE9DA)
private val BoneDark = Color(0xFFE2DAC6)

// Brass.
private val Brass = Color(0xFFC9A96A)
private val BrassSheen = Color(0xFFD6BA8C)
private val BrassMarkerLight = Color(0xFFF0DEB0)
private val BrassMarkerDark = Color(0xFFB08A4C)
private val RodStops = arrayOf(
    0f to Color(0xFF6A5632),
    0.42f to Brass,
    0.60f to Color(0xFFF4E6C4),
    0.78f to Color(0xFFAF8E4C),
    1f to Color(0xFF5C4A2A)
)

// Black-lacquer bead.
private val LacquerLight = Color(0xFF3A342E)
private val LacquerMid = Color(0xFF1A1714)
private val LacquerDark = Color(0xFF0B0A09)
private val BeadRidge = Color(0xFFE2CCA4)
private val BeadSpecular = Color(0xFFF8E8C6)
private val BeadBounce = Color(0xFFFFF6E0)

// Shadows.
private val BeamShadowColor = Color(0xFF120E0A)
private val BeadShadowColor = Color(0xFF18120C)
private val FieldRecessColor = Color(0xFF1E160E)

// The guide's focus column.
private val FocusWash = Color(0xFFC4A05A)

/**
 * Ebony is dense and tight-grained, and the frame only reads as a made thing once a little of that
 * shows. One streak of it, positioned from a fixed seed so the board is identical on every frame
 * and every device.
 */
private class EbonyGrain(
    /** Across the member's thickness, 0..1. */
    val offset: Float,
    /** Where the streak begins along the member's length, 0..1. */
    val start: Float,
    /** Fraction of the member's length. Overruns are clipped to the member. */
    val length: Float,
    /** How far the streak wanders across the thickness over its run. */
    val drift: Float,
    /** 0..1 across the stroke-width range. */
    val weight: Float,
    /** Ebony's grain is mostly darker than the body, with the occasional warmer streak. */
    val warm: Boolean
)

// Grain tunables. Up here rather than baked into the seeded pool below, so an edit lands without
// a process restart: a top-level initializer runs once per class load, a draw body every frame.
// Many fine streaks, not a few strong ones: ebony's grain is tight and high-frequency, and at a
// low enough count and high enough alpha the same lines read as scratches instead.
private const val GRAIN_COUNT = 90
private const val GRAIN_DARK_ALPHA = 0.16f
private const val GRAIN_WARM_ALPHA = 0.07f
private const val GRAIN_MIN_WIDTH = 0.18f
private const val GRAIN_WIDTH_SPAN = 0.45f
private val GrainWarm = Color(0xFF6E5A42)

private val ebonyGrain = Random(2141).let { random ->
    List(GRAIN_COUNT) {
        EbonyGrain(
            offset = random.nextFloat(),
            start = random.nextFloat(),
            length = 0.25f + random.nextFloat() * 0.7f,
            // Barely any: ebony runs straight, and a visible wander reads as a scratch.
            drift = (random.nextFloat() - 0.5f) * 0.05f,
            weight = random.nextFloat(),
            warm = random.nextFloat() < 0.28f
        )
    }
}

/** Rings used to fake a blurred highlight. Fewer than this and the steps band visibly. */
private const val SOFT_ELLIPSE_RINGS = 8

/**
 * Bead travel. The overshoot past 1 is deliberate: the bead arrives with a small mechanical
 * click into place rather than easing to a stop.
 */
private val BeadEasing = CubicBezierEasing(0.2f, 1.5f, 0.34f, 1f)
private const val BEAD_TRAVEL_MILLIS = 360

/**
 * The bi-conical silhouette, as fractions of the bead's box. Straight segments throughout: the
 * profile pinches to a point at the vertical centre of each side and runs flat across the middle
 * 40% of the top and bottom faces.
 */
internal val BeadOutline = listOf(
    0.30f to 0f, 0.14f to 0.15f, 0.02f to 0.42f, 0f to 0.50f,
    0.02f to 0.58f, 0.14f to 0.85f, 0.30f to 1f, 0.70f to 1f,
    0.86f to 0.85f, 0.98f to 0.58f, 1f to 0.50f, 0.98f to 0.42f,
    0.86f to 0.15f, 0.70f to 0f
)

/** Everything the board's geometry says, in canvas pixels, for one size and rod count. */
private class BoardMetrics(width: Float, height: Float, val rodsCount: Int) {
    /** One design unit in canvas pixels. Every number in this file is in design units. */
    val unit = height / BOARD_HEIGHT

    val fieldLeft = FIELD_INSET_X * unit
    val fieldRight = width - FIELD_INSET_X * unit
    val fieldTop = FIELD_INSET_Y * unit
    val fieldBottom = height - FIELD_INSET_Y * unit
    val fieldWidth = fieldRight - fieldLeft

    val beamTopY = BEAM_TOP * unit
    val beamBottomY = beamTopY + BEAM_HEIGHT * unit

    val beadHeight = BEAD_HEIGHT * unit
    val beadGap = BEAD_GAP * unit
    val beadPitch = beadHeight + beadGap

    // The outer bead columns sit fully inside the field with margin, and the remaining width is
    // shared evenly. This is the only thing that depends on the rod count.
    val rodSpacing = (fieldWidth - 2f * BEAD_EDGE * unit) / (rodsCount - 1).coerceAtLeast(1)
    val firstRodX = fieldLeft + BEAD_EDGE * unit
    val beadWidth = minOf(BEAD_WIDTH * unit, rodSpacing - 8f * unit).coerceAtLeast(1f)

    val heavenInactiveY = fieldTop + (BEAD_TO_FIELD + BEAD_HEIGHT / 2f) * unit
    val heavenActiveY = beamTopY - (BEAD_TO_BEAM + BEAD_HEIGHT / 2f) * unit

    fun rodX(index: Int): Float = firstRodX + rodSpacing * index

    fun earthActiveY(beadIndex: Int): Float =
        beamBottomY + (BEAD_TO_BEAM + BEAD_HEIGHT / 2f) * unit + beadIndex * beadPitch

    fun earthInactiveY(beadIndex: Int): Float =
        fieldBottom - (BEAD_TO_FIELD + BEAD_HEIGHT / 2f) * unit - (3 - beadIndex) * beadPitch
}

/**
 * The instrument itself: frame, grain, field, rods, beam, markers and beads. The interactive board
 * and the guide's five-rod board are the same drawing at different sizes, so they cannot drift
 * apart. [heaven] and [earth] give each bead's travel, 0 at rest and 1 against the beam, and
 * [washX] is the centre of the guide's focus column when there is one.
 */
private fun DrawScope.drawInstrument(
    m: BoardMetrics,
    heaven: (rodIndex: Int) -> Float,
    earth: (rodIndex: Int, beadIndex: Int) -> Float,
    washX: Float? = null,
    washAlpha: Float = 0f
) {
    // ── Ebony frame ────────────────────────────────────────────────────────────────
    val (boardStart, boardEnd) = angledGradient(150f, size)
    drawRoundRect(
        brush = Brush.linearGradient(
            0f to EbonyLight, 0.52f to EbonyMid, 1f to EbonyDark,
            start = boardStart,
            end = boardEnd
        ),
        cornerRadius = CornerRadius(BOARD_RADIUS * m.unit)
    )
    // Grain, in four members: the rails run the full width and the stiles sit between
    // them, so each piece's grain runs along its own length the way a made frame does.
    // Clipped to the board so nothing strays past the rounded corners.
    clipPath(
        Path().apply {
            addRoundRect(
                RoundRect(
                    0f, 0f, size.width, size.height,
                    CornerRadius(BOARD_RADIUS * m.unit)
                )
            )
        }
    ) {
        drawEbonyGrain(0f, 0f, size.width, m.fieldTop, true, 0f, m.unit)
        drawEbonyGrain(0f, m.fieldBottom, size.width, size.height, true, 0.37f, m.unit)
        drawEbonyGrain(0f, m.fieldTop, m.fieldLeft, m.fieldBottom, false, 0.61f, m.unit)
        drawEbonyGrain(m.fieldRight, m.fieldTop, size.width, m.fieldBottom, false, 0.83f, m.unit)
    }

    // Edge definition, then the brass sheen that catches along the top lip.
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.5f),
        cornerRadius = CornerRadius(BOARD_RADIUS * m.unit),
        style = Stroke(width = m.unit)
    )
    drawLine(
        color = BrassSheen.copy(alpha = 0.28f),
        start = Offset(BOARD_RADIUS * m.unit, m.unit * 0.5f),
        end = Offset(size.width - BOARD_RADIUS * m.unit, m.unit * 0.5f),
        strokeWidth = m.unit
    )

    // ── Brass inlay hairline ───────────────────────────────────────────────────────
    drawRoundRect(
        color = Brass.copy(alpha = 0.34f),
        topLeft = Offset(INLAY_INSET_X * m.unit, INLAY_INSET_Y * m.unit),
        size = Size(
            size.width - INLAY_INSET_X * 2f * m.unit,
            size.height - INLAY_INSET_Y * 2f * m.unit
        ),
        cornerRadius = CornerRadius(INLAY_RADIUS * m.unit),
        style = Stroke(width = m.unit)
    )

    // ── Bone reckoning field ───────────────────────────────────────────────────────
    val fieldSize = Size(m.fieldWidth, m.fieldBottom - m.fieldTop)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(BoneLight, BoneDark),
            startY = m.fieldTop,
            endY = m.fieldBottom
        ),
        topLeft = Offset(m.fieldLeft, m.fieldTop),
        size = fieldSize,
        cornerRadius = CornerRadius(FIELD_RADIUS * m.unit)
    )
    // Recessed under the frame: the shadow gathers along the top edge and fades out.
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(FieldRecessColor.copy(alpha = 0.34f), Color.Transparent),
            startY = m.fieldTop,
            endY = m.fieldTop + FIELD_RECESS * m.unit
        ),
        topLeft = Offset(m.fieldLeft, m.fieldTop),
        size = Size(m.fieldWidth, FIELD_RECESS * m.unit)
    )
    drawLine(
        color = Color.White.copy(alpha = 0.6f),
        start = Offset(m.fieldLeft, m.fieldBottom - m.unit * 0.5f),
        end = Offset(m.fieldRight, m.fieldBottom - m.unit * 0.5f),
        strokeWidth = m.unit
    )

    // ── Focus wash, on the field behind rods and beads ─────────────────────────────────
    if (washAlpha > 0f && washX != null) {
        clipRect(m.fieldLeft, m.fieldTop, m.fieldRight, m.fieldBottom) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        FocusWash.copy(alpha = 0.30f * washAlpha),
                        FocusWash.copy(alpha = 0.14f * washAlpha)
                    ),
                    startY = m.fieldTop,
                    endY = m.fieldBottom
                ),
                topLeft = Offset(washX - m.beadWidth / 2f, m.fieldTop),
                size = Size(m.beadWidth, m.fieldBottom - m.fieldTop)
            )
        }
    }

    // ── Brass rods, behind the beam and the beads ──────────────────────────────────
    val rodWidth = ROD_WIDTH * m.unit
    repeat(m.rodsCount) { index ->
        val x = m.rodX(index)
        drawLine(
            color = Color(0xFF785C2C).copy(alpha = 0.35f),
            start = Offset(x, m.fieldTop),
            end = Offset(x, m.fieldBottom),
            strokeWidth = rodWidth + 3f * m.unit
        )
        drawRect(
            brush = Brush.horizontalGradient(
                colorStops = RodStops,
                startX = x - rodWidth / 2f,
                endX = x + rodWidth / 2f
            ),
            topLeft = Offset(x - rodWidth / 2f, m.fieldTop),
            size = Size(rodWidth, m.fieldBottom - m.fieldTop)
        )
    }

    // ── Reckoning beam ─────────────────────────────────────────────────────────────
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(BeamShadowColor.copy(alpha = 0.4f), Color.Transparent),
            startY = m.beamBottomY,
            endY = m.beamBottomY + BEAM_SHADOW * m.unit
        ),
        topLeft = Offset(m.fieldLeft, m.beamBottomY),
        size = Size(m.fieldWidth, BEAM_SHADOW * m.unit)
    )
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(BeamLight, BeamDark),
            startY = m.beamTopY,
            endY = m.beamBottomY
        ),
        topLeft = Offset(m.fieldLeft, m.beamTopY),
        size = Size(m.fieldWidth, BEAM_HEIGHT * m.unit)
    )
    // The beam is the same ebony as the frame, so it carries the same grain.
    drawEbonyGrain(m.fieldLeft, m.beamTopY, m.fieldRight, m.beamBottomY, true, 0.19f, m.unit)
    drawLine(
        color = Brass.copy(alpha = 0.42f),
        start = Offset(m.fieldLeft, m.beamTopY + m.unit * 0.5f),
        end = Offset(m.fieldRight, m.beamTopY + m.unit * 0.5f),
        strokeWidth = m.unit
    )

    // ── Brass m.unit markers, on the app's every-fourth-rod grouping ─────────────────
    val markerReach = MARKER_SIZE * m.unit * 0.7071f
    repeat(m.rodsCount) { index ->
        if ((m.rodsCount - 1 - index) % 4 == 3) {
            val centre = Offset(m.rodX(index), m.beamTopY + BEAM_HEIGHT * m.unit / 2f)
            val diamond = Path().apply {
                moveTo(centre.x, centre.y - markerReach)
                lineTo(centre.x + markerReach, centre.y)
                lineTo(centre.x, centre.y + markerReach)
                lineTo(centre.x - markerReach, centre.y)
                close()
            }
            val (markerStart, markerEnd) = angledGradient(
                degrees = 135f,
                size = Size(markerReach * 2f, markerReach * 2f),
                origin = Offset(centre.x - markerReach, centre.y - markerReach)
            )
            drawPath(
                path = diamond,
                brush = Brush.linearGradient(
                    colors = listOf(BrassMarkerLight, BrassMarkerDark),
                    start = markerStart,
                    end = markerEnd
                )
            )
        }
    }

    // ── Black-lacquer beads ────────────────────────────────────────────────────────
    repeat(m.rodsCount) { index ->
        val x = m.rodX(index)
        val heavenFactor = heaven(index)
        drawLacquerBead(
            centerX = x,
            centerY = m.heavenInactiveY + (m.heavenActiveY - m.heavenInactiveY) * heavenFactor,
            beadWidth = m.beadWidth,
            beadHeight = m.beadHeight,
            unit = m.unit
        )

        repeat(4) { beadIndex ->
            val factor = earth(index, beadIndex)
            val activeY = m.earthActiveY(beadIndex)
            val inactiveY = m.earthInactiveY(beadIndex)
            drawLacquerBead(
                centerX = x,
                centerY = inactiveY + (activeY - inactiveY) * factor,
                beadWidth = m.beadWidth,
                beadHeight = m.beadHeight,
                unit = m.unit
            )
        }
    }
}

/** Aspect ratio a board of [rodsCount] rods wants: the design's frame plus one pitch per rod. */
fun sorobanBoardAspect(rodsCount: Int): Float {
    val fieldWidth = 2f * BEAD_EDGE + ROD_PITCH * (rodsCount - 1).coerceAtLeast(0)
    return (fieldWidth + 2f * FIELD_INSET_X) / BOARD_HEIGHT
}

@Composable
fun SorobanCanvas(
    rodsCount: Int,
    rodValues: IntArray,
    onRodValueChange: (rodIndex: Int, newValue: Int) -> Unit,
    hapticsEnabled: Boolean,
    accessibilityDescription: String,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current

    val latestRods by rememberUpdatedState(rodValues)

    // A device with animations turned off gets the bead's new position with no travel at all.
    val reduceMotion = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
    val beadSpec = remember(reduceMotion) {
        if (reduceMotion) snap() else tween<Float>(BEAD_TRAVEL_MILLIS, easing = BeadEasing)
    }

    val heavenAnimatables = remember(rodsCount) {
        List(rodsCount) { Animatable(0f) }
    }
    val earthAnimatables = remember(rodsCount) {
        List(rodsCount) { List(4) { Animatable(0f) } }
    }

    LaunchedEffect(rodValues, rodsCount, beadSpec) {
        for (index in 0 until rodsCount.coerceAtMost(rodValues.size)) {
            val rodValue = rodValues[index]
            launch {
                heavenAnimatables[index].animateTo(
                    targetValue = if (rodValue >= 5) 1f else 0f,
                    animationSpec = beadSpec
                )
            }
            repeat(4) { beadIndex ->
                launch {
                    earthAnimatables[index][beadIndex].animateTo(
                        targetValue = if (beadIndex < rodValue % 5) 1f else 0f,
                        animationSpec = beadSpec
                    )
                }
            }
        }
    }

    val surroundColor = MaterialTheme.colorScheme.surfaceVariant

    BoxWithConstraints(
        modifier = modifier
            .background(surroundColor)
            .semantics { contentDescription = accessibilityDescription }
    ) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()
        val metrics = remember(canvasWidth, canvasHeight, rodsCount) {
            BoardMetrics(canvasWidth, canvasHeight, rodsCount)
        }
        val beadPitch = metrics.beadPitch

        fun commitRodValue(rodIndex: Int, nextValue: Int): Boolean {
            val currentValue = latestRods.getOrElse(rodIndex) { 0 }
            val coercedValue = nextValue.coerceIn(0, 9)
            if (coercedValue == currentValue) return false

            onRodValueChange(rodIndex, coercedValue)
            if (hapticsEnabled) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            return true
        }

        fun rodAt(touchX: Float): Int = ((touchX - metrics.firstRodX) / metrics.rodSpacing)
            .roundToInt()
            .coerceIn(0, rodsCount - 1)

        fun handleTap(touchX: Float, touchY: Float) {
            val rodIndex = rodAt(touchX)
            val currentValue = latestRods.getOrElse(rodIndex) { 0 }
            val heavenActive = currentValue >= 5
            val earthActiveCount = currentValue % 5
            var nextValue = currentValue

            if (touchY < metrics.beamTopY) {
                // The upper deck holds a single bead, so any touch there flips it.
                nextValue = (if (heavenActive) 0 else 5) + earthActiveCount
            } else if (touchY > metrics.beamBottomY) {
                val targetEarthCount = earthBeadTarget(
                    touchY = touchY,
                    activeCount = earthActiveCount,
                    firstActiveY = metrics.earthActiveY(0),
                    lastInactiveY = metrics.earthInactiveY(3),
                    beadPitch = beadPitch
                )
                if (targetEarthCount != earthActiveCount) {
                    nextValue = (if (heavenActive) 5 else 0) + targetEarthCount
                }
            }

            commitRodValue(rodIndex, nextValue)
        }

        // A drag grabs one bead on one rod and carries it; the target is derived from the finger
        // position alone, so dragging back before releasing puts the bead where it started.
        var dragRod by remember { mutableIntStateOf(-1) }
        var dragStartValue by remember { mutableIntStateOf(0) }
        var dragGrabbedBead by remember { mutableIntStateOf(0) }
        var dragOnHeaven by remember { mutableStateOf(false) }

        fun beginDrag(touchX: Float, touchY: Float) {
            if (touchY in metrics.beamTopY..metrics.beamBottomY) {
                dragRod = -1
                return
            }
            dragRod = rodAt(touchX)
            dragStartValue = latestRods.getOrElse(dragRod) { 0 }
            dragOnHeaven = touchY < metrics.beamTopY
            dragGrabbedBead = if (dragOnHeaven) {
                0
            } else {
                nearestEarthBead(
                    touchY = touchY,
                    activeCount = dragStartValue % 5,
                    firstActiveY = metrics.earthActiveY(0),
                    lastInactiveY = metrics.earthInactiveY(3),
                    beadPitch = beadPitch
                )
            }
        }

        fun handleDrag(touchY: Float) {
            if (dragRod < 0) return
            val startHeaven = dragStartValue >= 5
            val startEarth = dragStartValue % 5

            val nextValue = if (dragOnHeaven) {
                val raisedY = metrics.heavenActiveY
                val loweredY = metrics.heavenInactiveY
                val midpoint = (raisedY + loweredY) / 2f
                val movedOver = if (startHeaven) touchY < midpoint else touchY > midpoint
                (if (startHeaven != movedOver) 5 else 0) + startEarth
            } else {
                val targetEarthCount = earthDragTarget(
                    touchY = touchY,
                    startCount = startEarth,
                    grabbedIndex = dragGrabbedBead,
                    firstActiveY = metrics.earthActiveY(0),
                    lastInactiveY = metrics.earthInactiveY(3),
                    beadPitch = beadPitch
                )
                (if (startHeaven) 5 else 0) + targetEarthCount
            }

            commitRodValue(dragRod, nextValue)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(rodsCount, canvasWidth, canvasHeight, hapticsEnabled) {
                    detectTapGestures { handleTap(it.x, it.y) }
                }
                .pointerInput(rodsCount, canvasWidth, canvasHeight, hapticsEnabled) {
                    detectDragGestures(
                        onDragStart = { offset -> beginDrag(offset.x, offset.y) },
                        onDragEnd = { dragRod = -1 },
                        onDragCancel = { dragRod = -1 }
                    ) { change, _ ->
                        change.consume()
                        handleDrag(change.position.y)
                    }
                }
        ) {
            drawInstrument(
                m = metrics,
                heaven = { heavenAnimatables[it].value },
                earth = { rod, bead -> earthAnimatables[rod][bead].value }
            )
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

/**
 * Lengthwise grain inside one frame member. [alongX] picks the member's long axis, and [phase]
 * shifts where the pool starts so the four members do not repeat one another.
 */
private fun DrawScope.drawEbonyGrain(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    alongX: Boolean,
    phase: Float,
    unit: Float
) {
    val memberWidth = right - left
    val memberHeight = bottom - top
    if (memberWidth <= 0f || memberHeight <= 0f) return
    val run = if (alongX) memberWidth else memberHeight
    val thickness = if (alongX) memberHeight else memberWidth

    clipRect(left, top, right, bottom) {
        ebonyGrain.forEach { grain ->
            val across = grain.offset * thickness
            val acrossEnd = across + grain.drift * thickness
            val begin = ((grain.start + phase) % 1f) * run
            val finish = begin + grain.length * run
            drawLine(
                color = if (grain.warm) {
                    GrainWarm.copy(alpha = GRAIN_WARM_ALPHA)
                } else {
                    Color.Black.copy(alpha = GRAIN_DARK_ALPHA)
                },
                start = if (alongX) {
                    Offset(left + begin, top + across)
                } else {
                    Offset(left + across, top + begin)
                },
                end = if (alongX) {
                    Offset(left + finish, top + acrossEnd)
                } else {
                    Offset(left + acrossEnd, top + finish)
                },
                strokeWidth = (GRAIN_MIN_WIDTH + grain.weight * GRAIN_WIDTH_SPAN) * unit,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * A blurred ellipse: [color] reaching [alpha] in the middle and fading to nothing by the edge.
 *
 * Stacked flat ovals rather than a radial gradient. A gradient would need its own shader per bead
 * per frame, since each one is centred somewhere different, and thirty-five beads' worth of that
 * is enough to stall a software renderer outright. Each ring adds the same small alpha, so the
 * centre accumulates to [alpha] and the rim keeps a single faint pass.
 */
private fun DrawScope.softEllipse(
    centre: Offset,
    width: Float,
    height: Float,
    color: Color,
    alpha: Float
) {
    val step = 1f - (1f - alpha).pow(1f / SOFT_ELLIPSE_RINGS)
    val ringColor = color.copy(alpha = step)
    repeat(SOFT_ELLIPSE_RINGS) { ring ->
        // The outermost ring runs wider than the nominal size, the way the reference's blur does.
        val spread = 1.5f - ring * (1.2f / SOFT_ELLIPSE_RINGS)
        val ringWidth = width * spread
        val ringHeight = height * spread
        drawOval(
            color = ringColor,
            topLeft = Offset(centre.x - ringWidth / 2f, centre.y - ringHeight / 2f),
            size = Size(ringWidth, ringHeight)
        )
    }
}

/**
 * Start and end of a CSS `linear-gradient(<degrees>, ...)` axis across a box of [size] placed at
 * [origin]: zero degrees points up, and the angle runs clockwise.
 */
private fun angledGradient(
    degrees: Float,
    size: Size,
    origin: Offset = Offset.Zero
): Pair<Offset, Offset> {
    val radians = degrees * PI.toFloat() / 180f
    val dx = sin(radians)
    val dy = -cos(radians)
    val length = abs(size.width * dx) + abs(size.height * dy)
    val centre = Offset(origin.x + size.width / 2f, origin.y + size.height / 2f)
    val half = Offset(dx * length / 2f, dy * length / 2f)
    return centre - half to centre + half
}

/** Center of earth bead [beadIndex] for a rod holding [activeCount] raised beads. */
internal fun earthBeadCenterY(
    beadIndex: Int,
    activeCount: Int,
    firstActiveY: Float,
    lastInactiveY: Float,
    beadPitch: Float
): Float = if (beadIndex < activeCount) {
    firstActiveY + beadIndex * beadPitch
} else {
    lastInactiveY - (3 - beadIndex) * beadPitch
}

/** The earth bead drawn closest to [touchY], i.e. the one a finger there is on. */
internal fun nearestEarthBead(
    touchY: Float,
    activeCount: Int,
    firstActiveY: Float,
    lastInactiveY: Float,
    beadPitch: Float
): Int = (0..3).minByOrNull { beadIndex ->
    kotlin.math.abs(
        touchY - earthBeadCenterY(beadIndex, activeCount, firstActiveY, lastInactiveY, beadPitch)
    )
} ?: 0

/**
 * Earth-bead count a tap at [touchY] should produce: the touched bead flips, and pushing a raised
 * bead down carries the beads above it while lifting a lowered bead carries the beads below it.
 */
internal fun earthBeadTarget(
    touchY: Float,
    activeCount: Int,
    firstActiveY: Float,
    lastInactiveY: Float,
    beadPitch: Float
): Int {
    val nearest = nearestEarthBead(touchY, activeCount, firstActiveY, lastInactiveY, beadPitch)
    return if (nearest < activeCount) nearest else nearest + 1
}

/**
 * Earth-bead count while dragging the bead grabbed at drag start. The bead follows the finger and
 * settles into its other slot once the finger has carried it past the midpoint of its travel, so
 * the target only ever depends on the finger position — dragging back undoes it.
 */
internal fun earthDragTarget(
    touchY: Float,
    startCount: Int,
    grabbedIndex: Int,
    firstActiveY: Float,
    lastInactiveY: Float,
    beadPitch: Float
): Int {
    val wasRaised = grabbedIndex < startCount
    val raisedY = firstActiveY + grabbedIndex * beadPitch
    val loweredY = lastInactiveY - (3 - grabbedIndex) * beadPitch
    val midpoint = (raisedY + loweredY) / 2f
    val movedOver = if (wasRaised) touchY > midpoint else touchY < midpoint
    return when {
        !movedOver -> startCount
        wasRaised -> grabbedIndex
        else -> grabbedIndex + 1
    }
}

/*
 * 使い方 — the guide's board. The same instrument as the interactive one, five rods wide: a lesson
 * needs enough places to show a carry and no more. Read-only; the stepper moves the beads, a
 * finger does not.
 */
const val GUIDE_ROD_COUNT = 5

/** Slower and softer than the instrument's own travel, so a learner can follow the movement. */
private val GuideBeadEasing = CubicBezierEasing(0.2f, 1.42f, 0.34f, 1f)
/**
 * One duration for everything a step change moves: beads, wash, the footer's step bar and the
 * crossfade of the words. They are one event, so they must start and land together.
 */
const val GUIDE_STEP_MILLIS = 460

@Composable
fun SorobanGuideBoard(
    rods: IntArray,
    highlightRod: Int?,
    accessibilityDescription: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reduceMotion = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
    val beadSpec = remember(reduceMotion) {
        if (reduceMotion) snap() else tween<Float>(GUIDE_STEP_MILLIS, easing = GuideBeadEasing)
    }
    val washSpec = remember(reduceMotion) {
        if (reduceMotion) snap() else tween<Float>(GUIDE_STEP_MILLIS)
    }

    // Seeded at the first step's state, not at zero: otherwise the board opens empty and the
    // beads fly in a beat after the sheet has arrived. Later steps still animate, because the
    // LaunchedEffect below drives them.
    val heavenAnimatables = remember {
        List(GUIDE_ROD_COUNT) { Animatable(if (rods.getOrElse(it) { 0 } >= 5) 1f else 0f) }
    }
    val earthAnimatables = remember {
        List(GUIDE_ROD_COUNT) { rod ->
            List(4) { bead -> Animatable(if (bead < rods.getOrElse(rod) { 0 } % 5) 1f else 0f) }
        }
    }
    // The wash slides between rods, so it keeps its last rod while it fades out.
    val washRod = remember { Animatable((highlightRod ?: 0).toFloat()) }
    val washAlpha = remember { Animatable(if (highlightRod == null) 0f else 1f) }

    LaunchedEffect(rods, beadSpec) {
        for (index in 0 until GUIDE_ROD_COUNT) {
            val rodValue = rods.getOrElse(index) { 0 }
            launch { heavenAnimatables[index].animateTo(if (rodValue >= 5) 1f else 0f, beadSpec) }
            repeat(4) { beadIndex ->
                launch {
                    earthAnimatables[index][beadIndex]
                        .animateTo(if (beadIndex < rodValue % 5) 1f else 0f, beadSpec)
                }
            }
        }
    }
    LaunchedEffect(highlightRod, washSpec) {
        if (highlightRod != null) launch { washRod.animateTo(highlightRod.toFloat(), washSpec) }
        washAlpha.animateTo(if (highlightRod == null) 0f else 1f, washSpec)
    }

    Canvas(modifier = modifier.semantics { contentDescription = accessibilityDescription }) {
        val metrics = BoardMetrics(size.width, size.height, GUIDE_ROD_COUNT)
        drawInstrument(
            m = metrics,
            heaven = { heavenAnimatables[it].value },
            earth = { rod, bead -> earthAnimatables[rod][bead].value },
            washX = metrics.firstRodX + metrics.rodSpacing * washRod.value,
            washAlpha = washAlpha.value
        )
    }
}

/**
 * One black-lacquer bead: a sharp bi-cone with a brass-lit equator ridge, a specular highlight on
 * the upper face, and a little bounce light off the field below. The ridge is what makes it read
 * as turned lacquer rather than a flat shape, so all three highlights are clipped to the
 * silhouette and none of them may spill past it.
 */
private fun DrawScope.drawLacquerBead(
    centerX: Float,
    centerY: Float,
    beadWidth: Float,
    beadHeight: Float,
    unit: Float
) {
    val left = centerX - beadWidth / 2f
    val top = centerY - beadHeight / 2f

    fun outline(yOffset: Float = 0f) = Path().apply {
        BeadOutline.forEachIndexed { index, (fx, fy) ->
            val x = left + beadWidth * fx
            val y = top + beadHeight * fy + yOffset
            if (index == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    // Contact shadow. Stacked offsets stand in for the reference's 4px blur: the same soft, tight
    // pool under the bi-cone, without a mask filter the hardware canvas may refuse to honour.
    listOf(1.3f to 0.17f, 2.6f to 0.13f, 4f to 0.09f).forEach { (offset, alpha) ->
        drawPath(
            path = outline(yOffset = offset * unit),
            color = BeadShadowColor.copy(alpha = alpha)
        )
    }

    val body = outline()
    drawPath(
        path = body,
        brush = Brush.verticalGradient(
            0f to LacquerLight, 0.46f to LacquerMid, 1f to LacquerDark,
            startY = top,
            endY = top + beadHeight
        )
    )

    clipPath(body) {
        // Equator ridge: a warm brass-lit band fading to a hard shadow under it.
        drawRect(
            brush = Brush.verticalGradient(
                0f to BeadRidge.copy(alpha = 0.26f),
                0.55f to BeadRidge.copy(alpha = 0.06f),
                1f to Color.Black.copy(alpha = 0.45f),
                startY = top + beadHeight * 0.4333f,
                endY = top + beadHeight * 0.5667f
            ),
            topLeft = Offset(left, top + beadHeight * 0.4333f),
            size = Size(beadWidth, beadHeight * 0.1334f)
        )
        // Specular on the upper face. The reference blurs a small ellipse, and a blur that wide
        // relative to the shape is most of the effect: a hard-edged oval of the stated size reads
        // as a blob stuck on the bead. A radial falloff over a slightly larger ellipse gives the
        // same soft sheen, and needs no mask filter the hardware canvas might decline.
        softEllipse(
            centre = Offset(
                x = left + beadWidth * (0.275f + 0.35f / 2f),
                y = top + beadHeight * (0.1333f + 0.20f / 2f)
            ),
            width = beadWidth * 0.35f,
            height = beadHeight * 0.20f,
            color = BeadSpecular,
            alpha = 0.34f
        )
        // Bounce light off the field.
        softEllipse(
            centre = Offset(centerX, top + beadHeight * 0.85f),
            width = beadWidth * 0.65f,
            height = beadHeight * 0.10f,
            color = BeadBounce,
            alpha = 0.12f
        )
    }
}
