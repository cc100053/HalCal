package com.sorobanzen.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorobanzen.app.R
import com.sorobanzen.app.domain.SorobanEngine
import com.sorobanzen.app.ui.components.ShakeResetListener
import com.sorobanzen.app.ui.components.SorobanCanvas
import com.sorobanzen.app.ui.components.sorobanBoardAspect
import com.sorobanzen.app.ui.components.ZenBackground
import com.sorobanzen.app.ui.components.ZenMark
import com.sorobanzen.app.viewmodel.ZenViewModel
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun SorobanScreen(
    viewModel: ZenViewModel,
    onNavigateToSettings: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val rodsCount = viewModel.rodsCount
    val rodValues by viewModel.rodValues.collectAsState()
    val sorobanValue by viewModel.sorobanValue.collectAsState()
    val hapticsEnabled by viewModel.hapticEnabled.collectAsState()

    fun performHapticFeedback() {
        if (hapticsEnabled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val kanjiReading = remember(sorobanValue) { SorobanEngine.convertToKanji(sorobanValue) }
    val formattedValue = remember(sorobanValue) {
        String.format(Locale.ROOT, "%,d", sorobanValue)
    }
    var showGuide by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    fun clearWithUndo() {
        val previousValues = rodValues.copyOf()
        if (previousValues.all { it == 0 }) return

        viewModel.clearSoroban()
        performHapticFeedback()
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.soroban_cleared),
                actionLabel = context.getString(R.string.undo),
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreSoroban(previousValues)
                performHapticFeedback()
            }
        }
    }

    ShakeResetListener(enabled = true) {
        clearWithUndo()
    }

    BackHandler(onBack = onExit)

    ZenBackground(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val spaciousLayout = maxWidth >= 720.dp && maxHeight >= 560.dp
            val compactHeight = maxHeight < 500.dp
            val outerPadding = if (spaciousLayout) 16.dp else 10.dp
            val railPadding = if (compactHeight) 8.dp else 12.dp
            val sectionGap = if (compactHeight) 8.dp else 16.dp
            val sidebarWidth = if (spaciousLayout) {
                (maxWidth * 0.19f).coerceIn(196.dp, 244.dp)
            } else {
                (maxWidth * 0.25f).coerceIn(176.dp, 216.dp)
            }
            // The ebony board's own corner radius, 9 units of its 352-unit height.
            val instrumentShape = RoundedCornerShape(8.dp)
            val contentHeightFraction = if (spaciousLayout) 0.86f else 1f

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(outerPadding),
                horizontalArrangement = Arrangement.spacedBy(if (spaciousLayout) 12.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .width(sidebarWidth)
                        .fillMaxHeight(contentHeightFraction)
                        .padding(horizontal = railPadding, vertical = if (compactHeight) 4.dp else 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ZenMark(modifier = Modifier.size(if (compactHeight) 23.dp else 28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.brand_name),
                            style = if (compactHeight) {
                                MaterialTheme.typography.titleMedium
                            } else {
                                MaterialTheme.typography.headlineSmall
                            },
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 22.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = pluralStringResource(
                                id = R.plurals.rods_value,
                                count = rodsCount,
                                rodsCount
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(if (compactHeight) 2.dp else 8.dp))
                        val valueStyle = when {
                            formattedValue.length <= 8 -> MaterialTheme.typography.displayMedium
                            formattedValue.length <= 13 -> MaterialTheme.typography.displaySmall
                            else -> MaterialTheme.typography.displaySmall.copy(
                                fontSize = 24.sp,
                                lineHeight = 30.sp
                            )
                        }
                        Text(
                            text = formattedValue,
                            style = valueStyle,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = kanjiReading,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(sectionGap))
                    // Hairline that starts on ochre and fades into the paper.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(if (compactHeight) 2.dp else 6.dp))

                    SorobanRailAction(
                        label = stringResource(id = R.string.calculator),
                        contentColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            performHapticFeedback()
                            onExit()
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    SorobanRailAction(
                        label = stringResource(id = R.string.clear_beads),
                        enabled = sorobanValue != 0L,
                        onClick = ::clearWithUndo
                    )
                    SorobanRailAction(
                        label = stringResource(id = R.string.guide),
                        contentColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            performHapticFeedback()
                            showGuide = true
                        }
                    )
                    SorobanRailAction(
                        label = stringResource(id = R.string.settings),
                        onClick = {
                            performHapticFeedback()
                            onNavigateToSettings()
                        }
                    )
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    // The ebony board is wide and shallow, so on most screens its width runs out
                    // before its height does. Fit the fixed aspect to whichever binds first.
                    val boardAspect = sorobanBoardAspect(rodsCount)
                    val boardHeight = minOf(
                        maxHeight * contentHeightFraction,
                        maxWidth / boardAspect
                    )
                    SorobanCanvas(
                        rodsCount = rodsCount,
                        rodValues = rodValues,
                        onRodValueChange = viewModel::updateRodValue,
                        hapticsEnabled = hapticsEnabled,
                        accessibilityDescription = stringResource(
                            id = R.string.soroban_canvas_description,
                            String.format(Locale.ROOT, "%,d", sorobanValue)
                        ),
                        modifier = Modifier
                            .height(boardHeight)
                            .aspectRatio(boardAspect)
                            .shadow(10.dp, instrumentShape, clip = false)
                            .clip(instrumentShape)
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 420.dp)
                    .padding(bottom = 16.dp)
            )

            if (showGuide) {
                UsageGuideSheet(onClose = { showGuide = false })
            }
        }
    }

}

/** Plain-text rail entry: no chrome, only the word and a comfortable touch target. */
@Composable
private fun SorobanRailAction(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    val resolvedContentColor = if (enabled) {
        contentColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        shape = MaterialTheme.shapes.small,
        color = Color.Transparent,
        contentColor = resolvedContentColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = label, style = MaterialTheme.typography.titleSmall)
        }
    }
}
