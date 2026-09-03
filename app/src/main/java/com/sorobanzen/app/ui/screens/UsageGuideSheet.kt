package com.sorobanzen.app.ui.screens

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorobanzen.app.R
import com.sorobanzen.app.ui.components.GUIDE_ROD_COUNT
import com.sorobanzen.app.ui.components.SorobanGuideBoard
import com.sorobanzen.app.ui.components.sorobanBoardAspect

/*
 * 使い方 — the chaptered usage guide.
 *
 * Like the instrument itself, this sheet is a designed surface with a fixed palette rather than a
 * themed one: paper, ink and ochre, identical in light and dark. Every number below is in the
 * handoff's own 980 x 520 space and the whole sheet is scaled to whatever the turned frame has
 * room for, so the proportions hold on a phone and the constants can still be read against the
 * design.
 */
private val GuidePaper = Color(0xFFFAF7F0)
private val GuideInk = Color(0xFF25231F)
private val GuideBodyInk = Color(0xFF4A453D)
private val GuideMuted = GuideInk.copy(alpha = 0.42f)
private val GuideDisabled = GuideInk.copy(alpha = 0.22f)
private val GuideHairline = GuideInk.copy(alpha = 0.12f)
private val GuideFooterHairline = GuideInk.copy(alpha = 0.1f)
private val GuideAccent = Color(0xFF8A6528)
private val GuideNoteInk = Color(0xFF7A5A22)
private val GuideNoteBackground = Color(0xFFC4A05A).copy(alpha = 0.13f)
private val ScrimStops = arrayOf(
    0f to Color(0xFF3A342C),
    0.6f to Color(0xFF221D18),
    1f to Color(0xFF14100D)
)

private val Mincho = FontFamily.Serif
private val Gothic = FontFamily.SansSerif

private const val SHEET_WIDTH = 980f
private const val BOARD_WIDTH = 360f
// The reference sizes the sheet to its content, at most about 600. One height for every step
// instead: the footer must not move as a learner walks through a chapter, and the tallest step
// here is the board plus its caption.
private const val SHEET_HEIGHT = 560f

/** One idea. [rods] is the exact board state, most-significant digit first. */
private class GuideStep(
    @StringRes val title: Int,
    @StringRes val body: Int,
    val rods: IntArray,
    @StringRes val note: Int? = null,
    /** Numerals only, so it reads the same in any locale. */
    val formula: String? = null,
    val highlight: Int? = null
) {
    init {
        require(rods.size == GUIDE_ROD_COUNT) { "a lesson step needs $GUIDE_ROD_COUNT rods" }
    }
}

private class GuideChapter(
    @StringRes val label: Int,
    val steps: List<GuideStep>
)

// The chapters are the skills a beginner picks up, in the order each one needs the last: what a
// bead is worth, how to place a number, the moves that need no trick, the 5-complement, the
// 10-complement, and only then the two operations built out of all of it. Naming the chapters
// after the four arithmetic operations instead would teach the 5- and 10-complements twice, under
// names that hide them, which is what the first draft of this guide did.
//
// The copy is drafted from standard technique and still wants a review pass from someone who
// teaches it. It lives here as data so that pass lands in strings.xml without touching layout.
private val LESSONS = listOf(
    GuideChapter(R.string.guide_chapter_read, listOf(
        GuideStep(
            title = R.string.soroban_guide_heaven_title,
            body = R.string.soroban_guide_heaven_description,
            rods = intArrayOf(0, 0, 0, 0, 5),
            highlight = 4
        ),
        GuideStep(
            title = R.string.soroban_guide_earth_title,
            body = R.string.soroban_guide_earth_description,
            rods = intArrayOf(0, 0, 0, 0, 2),
            highlight = 4
        ),
        GuideStep(
            title = R.string.soroban_guide_dots_title,
            body = R.string.soroban_guide_dots_description,
            rods = intArrayOf(0, 0, 0, 0, 0)
        ),
        // Two rods, because every chapter after this one carries between them.
        GuideStep(
            title = R.string.soroban_guide_value_title,
            body = R.string.soroban_guide_value_description,
            rods = intArrayOf(0, 0, 0, 2, 7),
            formula = "27 = 20 + 7"
        )
    )),
    GuideChapter(R.string.guide_chapter_place, listOf(
        GuideStep(
            title = R.string.guide_place_1_title,
            body = R.string.guide_place_1_body,
            rods = intArrayOf(0, 0, 0, 0, 3),
            formula = "3",
            highlight = 4
        ),
        GuideStep(
            title = R.string.guide_place_2_title,
            body = R.string.guide_place_2_body,
            rods = intArrayOf(0, 0, 0, 0, 8),
            formula = "8 = 5 + 3",
            highlight = 4
        ),
        GuideStep(
            title = R.string.guide_place_3_title,
            body = R.string.guide_place_3_body,
            rods = intArrayOf(0, 0, 0, 0, 0),
            formula = "0",
            highlight = 4
        )
    )),
    GuideChapter(R.string.guide_chapter_direct, listOf(
        GuideStep(
            title = R.string.guide_direct_1_title,
            body = R.string.guide_direct_1_body,
            rods = intArrayOf(0, 0, 0, 0, 4),
            note = R.string.guide_direct_1_note,
            formula = "1 + 3 = 4",
            highlight = 4
        ),
        GuideStep(
            title = R.string.guide_direct_2_title,
            body = R.string.guide_direct_2_body,
            rods = intArrayOf(0, 0, 0, 0, 8),
            formula = "6 + 2 = 8",
            highlight = 4
        ),
        GuideStep(
            title = R.string.guide_direct_3_title,
            body = R.string.guide_direct_3_body,
            rods = intArrayOf(0, 0, 0, 0, 5),
            formula = "7 − 2 = 5",
            highlight = 4
        )
    )),
    GuideChapter(R.string.guide_chapter_five, listOf(
        GuideStep(
            title = R.string.guide_five_1_title,
            body = R.string.guide_five_1_body,
            rods = intArrayOf(0, 0, 0, 0, 4),
            formula = "4 + 3",
            highlight = 4
        ),
        GuideStep(
            title = R.string.guide_five_2_title,
            body = R.string.guide_five_2_body,
            rods = intArrayOf(0, 0, 0, 0, 9),
            note = R.string.guide_five_2_note,
            formula = "4 + 5",
            highlight = 4
        ),
        GuideStep(
            title = R.string.guide_five_3_title,
            body = R.string.guide_five_3_body,
            rods = intArrayOf(0, 0, 0, 0, 7),
            formula = "4 + 3 = 7",
            highlight = 4
        ),
        GuideStep(
            title = R.string.guide_five_4_title,
            body = R.string.guide_five_4_body,
            rods = intArrayOf(0, 0, 0, 0, 2),
            note = R.string.guide_five_4_note,
            formula = "7 − 5",
            highlight = 4
        ),
        GuideStep(
            title = R.string.guide_five_5_title,
            body = R.string.guide_five_5_body,
            rods = intArrayOf(0, 0, 0, 0, 4),
            formula = "7 − 3 = 4",
            highlight = 4
        )
    )),
    GuideChapter(R.string.guide_chapter_ten, listOf(
        GuideStep(
            title = R.string.guide_ten_1_title,
            body = R.string.guide_ten_1_body,
            rods = intArrayOf(0, 0, 0, 0, 8),
            formula = "8 + 7",
            highlight = 4
        ),
        GuideStep(
            title = R.string.guide_ten_2_title,
            body = R.string.guide_ten_2_body,
            rods = intArrayOf(0, 0, 0, 1, 8),
            note = R.string.guide_ten_2_note,
            formula = "8 + 10",
            highlight = 3
        ),
        GuideStep(
            title = R.string.guide_ten_3_title,
            body = R.string.guide_ten_3_body,
            rods = intArrayOf(0, 0, 0, 1, 5),
            formula = "8 + 7 = 15",
            highlight = 4
        ),
        GuideStep(
            title = R.string.guide_ten_4_title,
            body = R.string.guide_ten_4_body,
            rods = intArrayOf(0, 0, 0, 0, 5),
            note = R.string.guide_ten_4_note,
            formula = "15 − 10",
            highlight = 3
        ),
        GuideStep(
            title = R.string.guide_ten_5_title,
            body = R.string.guide_ten_5_body,
            rods = intArrayOf(0, 0, 0, 0, 8),
            formula = "15 − 7 = 8",
            highlight = 4
        )
    )),
    GuideChapter(R.string.guide_chapter_muldiv, listOf(
        GuideStep(
            title = R.string.guide_muldiv_1_title,
            body = R.string.guide_muldiv_1_body,
            rods = intArrayOf(0, 0, 0, 2, 3),
            formula = "23 × 4",
            highlight = 4
        ),
        GuideStep(
            title = R.string.guide_muldiv_2_title,
            body = R.string.guide_muldiv_2_body,
            rods = intArrayOf(0, 0, 0, 9, 2),
            note = R.string.guide_muldiv_2_note,
            formula = "23 × 4 = 92",
            highlight = 3
        ),
        GuideStep(
            title = R.string.guide_muldiv_3_title,
            body = R.string.guide_muldiv_3_body,
            rods = intArrayOf(0, 0, 0, 8, 4),
            formula = "84 ÷ 4",
            highlight = 3
        ),
        GuideStep(
            title = R.string.guide_muldiv_4_title,
            body = R.string.guide_muldiv_4_body,
            rods = intArrayOf(0, 0, 0, 2, 1),
            note = R.string.guide_muldiv_4_note,
            formula = "84 ÷ 4 = 21",
            highlight = 3
        )
    ))
)

/**
 * The guide lives inside the soroban's own frame rather than in a dialog window. A dialog gets a
 * window of its own, and that window would come up in the app's portrait orientation instead of
 * the reader's.
 */
@Composable
fun BoxScope.UsageGuideSheet(onClose: () -> Unit) {
    BackHandler(onBack = onClose)

    var chapterIndex by remember { mutableIntStateOf(0) }
    var stepIndex by remember { mutableIntStateOf(0) }
    val chapter = LESSONS[chapterIndex]
    val step = chapter.steps[stepIndex]

    fun go(chapter: Int, step: Int) {
        chapterIndex = chapter
        stepIndex = step.coerceIn(0, LESSONS[chapter].steps.lastIndex)
    }

    // The chapters are one lesson in order, so 次へ and 戻る run through the whole guide rather
    // than stopping at a chapter's edge. Only the last step of the last chapter turns back.
    fun next() = when {
        stepIndex < chapter.steps.lastIndex -> go(chapterIndex, stepIndex + 1)
        chapterIndex < LESSONS.lastIndex -> go(chapterIndex + 1, 0)
        else -> go(0, 0)
    }

    fun back() = when {
        stepIndex > 0 -> go(chapterIndex, stepIndex - 1)
        chapterIndex > 0 -> go(chapterIndex - 1, LESSONS[chapterIndex - 1].steps.lastIndex)
        else -> Unit
    }

    val context = LocalContext.current
    val reduceMotion = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
    val entry = remember { Animatable(0f) }
    LaunchedEffect(reduceMotion) {
        entry.animateTo(
            targetValue = 1f,
            animationSpec = if (reduceMotion) {
                snap()
            } else {
                tween(400, easing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1f))
            }
        )
    }

    val entryOffset = with(LocalDensity.current) { 6.dp.toPx() }

    Box(
        modifier = Modifier
            .matchParentSize()
            .background(
                Brush.linearGradient(
                    colorStops = ScrimStops,
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .pointerInput(Unit) { detectTapGestures { onClose() } },
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // The sheet is laid out at its design size and scaled as one piece, so a phone in the
            // turned frame gets the design's proportions rather than a reflowed version of it.
            val scale = minOf(
                (maxWidth - 24.dp) / SHEET_WIDTH.dp,
                (maxHeight - 24.dp) / SHEET_HEIGHT.dp,
                1f
            ).coerceAtLeast(0.1f)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = entry.value
                        translationY = (1f - entry.value) * entryOffset
                    }
                    // Required, not plain, size: the sheet is laid out at its design size
                    // whatever room the turned frame has, and the scale above brings it back.
                    .requiredWidth(SHEET_WIDTH.dp)
                    .requiredHeight(SHEET_HEIGHT.dp)
                    .shadow(30.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(GuidePaper)
                    // Swallows taps so a press on the sheet does not read as a press outside it.
                    .pointerInput(Unit) { detectTapGestures { } }
                    .padding(start = 38.dp, end = 38.dp, top = 34.dp, bottom = 26.dp)
            ) {
                GuideHeader(
                    counter = stringResource(
                        id = R.string.guide_step_counter,
                        stepIndex + 1,
                        chapter.steps.size
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))

                GuideChapterTabs(
                    activeIndex = chapterIndex,
                    // A chapter always opens on its first step.
                    onPick = { go(it, 0) }
                )
                Spacer(modifier = Modifier.height(26.dp))

                GuideBody(step = step, modifier = Modifier.weight(1f))

                val atChapterEnd = stepIndex == chapter.steps.lastIndex
                GuideFooter(
                    stepIndex = stepIndex,
                    stepCount = chapter.steps.size,
                    atFirstOfGuide = chapterIndex == 0 && stepIndex == 0,
                    atLastOfGuide = atChapterEnd && chapterIndex == LESSONS.lastIndex,
                    // The last step of a chapter says where 次へ is about to take the reader,
                    // and changes colour, so leaving the chapter is never a surprise.
                    nextChapter = if (atChapterEnd && chapterIndex < LESSONS.lastIndex) {
                        stringResource(id = LESSONS[chapterIndex + 1].label)
                    } else {
                        null
                    },
                    onStep = { go(chapterIndex, it) },
                    onBack = ::back,
                    onNext = ::next,
                    onClose = onClose
                )
            }
        }
    }
}

@Composable
private fun GuideHeader(counter: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = stringResource(id = R.string.guide),
            fontFamily = Mincho,
            fontWeight = FontWeight.Normal,
            fontSize = 27.sp,
            letterSpacing = 0.5.sp,
            color = GuideInk
        )
        Text(
            text = counter,
            fontFamily = Gothic,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp,
            color = GuideMuted
        )
    }
}

@Composable
private fun GuideChapterTabs(activeIndex: Int, onPick: (Int) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(1.dp)
                .background(GuideHairline)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            LESSONS.forEachIndexed { index, chapter ->
                val active = index == activeIndex
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .clickable { onPick(index) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = chapter.label),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 11.dp),
                        fontFamily = Mincho,
                        fontSize = 15.sp,
                        fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                        color = if (active) GuideInk else GuideMuted
                    )
                    // Sits on the container's hairline, inset from each side of the tab.
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(if (active) GuideAccent else Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideBody(step: GuideStep, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(44.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val reading = step.rods.joinToString("").trimStart('0').ifEmpty { "0" }
            // The board's own corner radius and lift, as the soroban screen gives it.
            val boardShape = RoundedCornerShape(8.dp)
            SorobanGuideBoard(
                rods = step.rods,
                highlightRod = step.highlight,
                accessibilityDescription = stringResource(
                    id = R.string.guide_board_description,
                    reading
                ),
                modifier = Modifier
                    .requiredWidth(BOARD_WIDTH.dp)
                    .aspectRatio(sorobanBoardAspect(GUIDE_ROD_COUNT))
                    .shadow(10.dp, boardShape, clip = false)
                    .clip(boardShape)
            )
            if (step.formula != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = step.formula,
                    fontFamily = Mincho,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    letterSpacing = 0.6.sp,
                    color = GuideAccent,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(modifier = Modifier.padding(top = 6.dp)) {
            Text(
                text = stringResource(id = step.title),
                fontFamily = Mincho,
                fontWeight = FontWeight.Medium,
                fontSize = 19.sp,
                color = GuideInk
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = step.body),
                modifier = Modifier.widthIn(max = 400.dp),
                fontFamily = Gothic,
                fontSize = 13.5.sp,
                lineHeight = 27.sp,
                color = GuideBodyInk
            )
            if (step.note != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GuideNoteBackground)
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(GuideAccent)
                    )
                    Text(
                        text = stringResource(id = step.note),
                        fontFamily = Gothic,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp,
                        letterSpacing = 0.3.sp,
                        color = GuideNoteInk
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideFooter(
    stepIndex: Int,
    stepCount: Int,
    atFirstOfGuide: Boolean,
    atLastOfGuide: Boolean,
    nextChapter: String?,
    onStep: (Int) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    Spacer(modifier = Modifier.height(28.dp))
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GuideFooterHairline))
    Spacer(modifier = Modifier.height(18.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            repeat(stepCount) { index ->
                val active = index == stepIndex
                val width by animateDpAsState(
                    targetValue = if (active) 20.dp else 7.dp,
                    animationSpec = tween(300),
                    label = "stepDot"
                )
                val label = stringResource(id = R.string.guide_step_select, index + 1)
                Box(
                    // The bar is small on purpose; the target around it is not.
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onStep(index) }
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(width)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (active) GuideAccent else GuideInk.copy(alpha = 0.2f))
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 戻る stays in place at the very start, dimmed: the footer must not reflow.
            val atStart = atFirstOfGuide
            Text(
                text = stringResource(id = R.string.guide_back),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (atStart) {
                            Modifier
                        } else {
                            Modifier.clickable { onBack() }
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                fontFamily = Gothic,
                fontSize = 13.sp,
                color = if (atStart) GuideDisabled else GuideInk.copy(alpha = 0.6f)
            )
            Text(
                text = when {
                    atLastOfGuide -> stringResource(id = R.string.guide_restart)
                    nextChapter != null ->
                        stringResource(id = R.string.guide_next_chapter, nextChapter)
                    else -> stringResource(id = R.string.guide_next)
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (atLastOfGuide || nextChapter != null) GuideAccent else GuideInk
                    )
                    .clickable { onNext() }
                    .padding(horizontal = 22.dp, vertical = 9.dp),
                fontFamily = Gothic,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp,
                color = GuidePaper
            )
            Text(
                text = stringResource(id = R.string.close),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClose() }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                fontFamily = Gothic,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp,
                color = GuideAccent
            )
        }
    }
}
