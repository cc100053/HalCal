package com.sorobanzen.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.sorobanzen.app.R
import com.sorobanzen.app.domain.SorobanEngine
import com.sorobanzen.app.ui.components.ShakeResetListener
import com.sorobanzen.app.ui.components.ShareUtility
import com.sorobanzen.app.ui.components.SorobanCanvas
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

    fun performHapticFeedback() {
        if (hapticsEnabled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val kanjiReading = remember(sorobanValue) { SorobanEngine.convertToKanji(sorobanValue) }
    var showInfoDialog by remember { mutableStateOf(false) }

    ShakeResetListener(enabled = true) {
        viewModel.clearSoroban()
        performHapticFeedback()
        Toast.makeText(context, context.getString(R.string.clear_beads), Toast.LENGTH_SHORT).show()
    }

    ZenBackground(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val sidebarWidth = (maxWidth * 0.31f).coerceIn(220.dp, 292.dp)

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ZenCard(
                    modifier = Modifier
                        .width(sidebarWidth)
                        .fillMaxHeight(),
                    contentPadding = 14.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
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
                            Text(
                                text = pluralStringResource(
                                    id = R.plurals.rods_value,
                                    count = rodsCount,
                                    rodsCount
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = String.format(Locale.ROOT, "%,d", sorobanValue),
                                style = MaterialTheme.typography.displaySmall,
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
                        }

                        Button(
                            onClick = {
                                performHapticFeedback()
                                viewModel.speakJapaneseNumber(sorobanValue)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                contentColor = MaterialTheme.colorScheme.primary
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

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Button(
                                onClick = {
                                    performHapticFeedback()
                                    viewModel.clearSoroban()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 44.dp),
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
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    label = stringResource(id = R.string.share),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        performHapticFeedback()
                                        coroutineScope.launch {
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
                                text = stringResource(id = R.string.shake_to_clear),
                                style = MaterialTheme.typography.labelSmall,
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
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.large)
                )
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(stringResource(id = R.string.soroban_guide_title)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(stringResource(id = R.string.soroban_guide_heaven), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(id = R.string.soroban_guide_earth), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(id = R.string.soroban_guide_dots), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(id = R.string.soroban_guide_value), style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(stringResource(id = R.string.close))
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

@Composable
private fun SorobanUtilityButton(
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 44.dp),
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
