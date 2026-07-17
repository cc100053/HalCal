package com.sorobanzen.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.DialogProperties
import com.sorobanzen.app.R
import com.sorobanzen.app.domain.SorobanEngine
import com.sorobanzen.app.ui.components.ShakeResetListener
import com.sorobanzen.app.ui.components.ShareUtility
import com.sorobanzen.app.ui.components.SorobanCanvas
import com.sorobanzen.app.ui.components.SorobanGuidePreview
import com.sorobanzen.app.ui.components.ZenBackground
import com.sorobanzen.app.ui.components.ZenCard
import com.sorobanzen.app.ui.components.ZenMark
import com.sorobanzen.app.viewmodel.ZenViewModel
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun SorobanScreen(
    viewModel: ZenViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val rodsCount by viewModel.rodsCount.collectAsState()
    val rodValues by viewModel.rodValues.collectAsState()
    val sorobanValue by viewModel.sorobanValue.collectAsState()
    val soundEnabled by viewModel.soundEffectsEnabled.collectAsState()
    val hapticsEnabled by viewModel.hapticEnabled.collectAsState()
    val ttsEnabled by viewModel.ttsEnabled.collectAsState()

    fun performHapticFeedback() {
        if (hapticsEnabled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val kanjiReading = remember(sorobanValue) { SorobanEngine.convertToKanji(sorobanValue) }
    val formattedValue = remember(sorobanValue) {
        String.format(Locale.ROOT, "%,d", sorobanValue)
    }
    var showInfoDialog by remember { mutableStateOf(false) }
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

    ZenBackground(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val spaciousLayout = maxWidth >= 720.dp && maxHeight >= 560.dp
            val compactHeight = maxHeight < 500.dp
            val outerPadding = if (spaciousLayout) 16.dp else 10.dp
            val contentPadding = if (compactHeight) 12.dp else 16.dp
            val sectionGap = if (compactHeight) 10.dp else 18.dp
            val sidebarWidth = (maxWidth * 0.31f).coerceIn(224.dp, 304.dp)

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(outerPadding),
                horizontalArrangement = Arrangement.spacedBy(outerPadding)
            ) {
                ZenCard(
                    modifier = Modifier
                        .width(sidebarWidth)
                        .fillMaxHeight(),
                    contentPadding = contentPadding
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ZenMark(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = stringResource(id = R.string.brand_name),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Text(
                                    text = pluralStringResource(
                                        id = R.plurals.rods_value,
                                        count = rodsCount,
                                        rodsCount
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(sectionGap))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val valueStyle = when {
                                formattedValue.length <= 9 -> MaterialTheme.typography.displayMedium
                                formattedValue.length <= 14 -> MaterialTheme.typography.displaySmall
                                else -> MaterialTheme.typography.displaySmall.copy(
                                    fontSize = 24.sp,
                                    lineHeight = 30.sp
                                )
                            }
                            Text(
                                text = formattedValue,
                                style = valueStyle,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = kanjiReading,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 14.dp))

                            OutlinedButton(
                                onClick = {
                                    performHapticFeedback()
                                    viewModel.speakJapaneseNumber(sorobanValue)
                                },
                                enabled = ttsEnabled,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.48f)
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(id = R.string.read_aloud),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.height(sectionGap))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Button(
                                onClick = ::clearWithUndo,
                                enabled = sorobanValue != 0L,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(stringResource(id = R.string.clear_beads))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                SorobanUtilityButton(
                                    icon = {
                                        if (isSharing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(17.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    label = stringResource(
                                        id = if (isSharing) R.string.sharing else R.string.share
                                    ),
                                    modifier = Modifier.weight(1f),
                                    enabled = sorobanValue != 0L && !isSharing,
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
                                                    Toast.makeText(
                                                        context,
                                                        context.getString(R.string.share_failed),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } finally {
                                                isSharing = false
                                            }
                                        }
                                    }
                                )
                                SorobanUtilityButton(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    label = stringResource(id = R.string.guide),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        performHapticFeedback()
                                        showInfoDialog = true
                                    }
                                )
                            }

                            Text(
                                text = stringResource(
                                    id = if (sorobanValue == 0L) {
                                        R.string.soroban_interaction_hint
                                    } else {
                                        R.string.shake_to_clear
                                    }
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

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
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.large)
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 420.dp)
                    .padding(bottom = 16.dp)
            )
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth(),
            title = {
                Text(
                    text = stringResource(id = R.string.soroban_guide_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
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
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(stringResource(id = R.string.close))
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        )
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

@Composable
private fun SorobanUtilityButton(
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
