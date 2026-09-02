package com.sorobanzen.app.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.input.pointer.pointerInput
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
import com.sorobanzen.app.ui.components.ShareUtility
import com.sorobanzen.app.ui.components.SorobanCanvas
import com.sorobanzen.app.ui.components.SorobanGuidePreview
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
    val soundEnabled by viewModel.soundEffectsEnabled.collectAsState()
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
    var isSharing by remember { mutableStateOf(false) }
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
            val instrumentShape = RoundedCornerShape(if (spaciousLayout) 18.dp else 14.dp)
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
                        label = stringResource(id = if (isSharing) R.string.sharing else R.string.share),
                        enabled = !isSharing,
                        contentColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            performHapticFeedback()
                            isSharing = true
                            coroutineScope.launch {
                                try {
                                    runCatching {
                                        ShareUtility.createSorobanShareIntent(
                                            context = context,
                                            value = sorobanValue,
                                            kanjiReading = kanjiReading,
                                            rodsCount = rodsCount,
                                            rodValues = rodValues
                                        )
                                    }.onSuccess { shareIntent ->
                                        context.startActivity(
                                            Intent.createChooser(
                                                shareIntent,
                                                context.getString(R.string.share_title)
                                            )
                                        )
                                    }.onFailure {
                                        // Not a Toast: the system draws those in the window's
                                        // own orientation, which is not the reader's here. Its
                                        // own coroutine, so the share button frees up at once
                                        // instead of waiting out the message.
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = context.getString(R.string.share_failed),
                                                withDismissAction = true,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                } finally {
                                    isSharing = false
                                }
                            }
                        }
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

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    SorobanCanvas(
                        rodsCount = rodsCount,
                        rodValues = rodValues,
                        onRodValueChange = viewModel::updateRodValue,
                        soundEnabled = soundEnabled,
                        hapticsEnabled = hapticsEnabled,
                        accessibilityDescription = stringResource(
                            id = R.string.soroban_canvas_description,
                            String.format(Locale.ROOT, "%,d", sorobanValue)
                        ),
                        modifier = Modifier
                            .fillMaxHeight(contentHeightFraction)
                            .aspectRatio(
                                ratio = sorobanBoardAspect(rodsCount),
                                matchHeightConstraintsFirst = true
                            )
                            .shadow(5.dp, instrumentShape, clip = false)
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
                SorobanGuideOverlay(onClose = { showGuide = false })
            }
        }
    }

}

/**
 * The guide lives inside the soroban's own frame rather than in a dialog window. A dialog gets a
 * window of its own, and that window would come up in the app's portrait orientation instead of
 * the reader's.
 */
@Composable
private fun BoxScope.SorobanGuideOverlay(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    Box(
        modifier = Modifier
            .matchParentSize()
            .background(Color.Black.copy(alpha = 0.42f))
            .pointerInput(Unit) { detectTapGestures { onClose() } },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                // Swallows taps so a press on the card does not read as a press outside it.
                .pointerInput(Unit) { detectTapGestures { } },
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(id = R.string.soroban_guide_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.width(116.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SorobanGuidePreview(
                            accessibilityDescription = stringResource(
                                id = R.string.soroban_guide_preview_description
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(184.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.soroban_guide_example),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SorobanGuideRow(
                            index = 1,
                            title = stringResource(id = R.string.soroban_guide_heaven_title),
                            description = stringResource(
                                id = R.string.soroban_guide_heaven_description
                            )
                        )
                        SorobanGuideRow(
                            index = 2,
                            title = stringResource(id = R.string.soroban_guide_earth_title),
                            description = stringResource(
                                id = R.string.soroban_guide_earth_description
                            )
                        )
                        SorobanGuideRow(
                            index = 3,
                            title = stringResource(id = R.string.soroban_guide_dots_title),
                            description = stringResource(
                                id = R.string.soroban_guide_dots_description
                            )
                        )
                        SorobanGuideRow(
                            index = 4,
                            title = stringResource(id = R.string.soroban_guide_value_title),
                            description = stringResource(
                                id = R.string.soroban_guide_value_description
                            )
                        )
                    }
                }
                TextButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(id = R.string.close))
                }
            }
        }
    }
}

@Composable
private fun SorobanGuideRow(
    index: Int,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Surface(
            modifier = Modifier.size(26.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = index.toString(), style = MaterialTheme.typography.labelMedium)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
