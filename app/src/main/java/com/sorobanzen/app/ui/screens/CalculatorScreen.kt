package com.sorobanzen.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sorobanzen.app.R
import com.sorobanzen.app.data.HistoryEntity
import com.sorobanzen.app.ui.components.CalculatorGrid
import com.sorobanzen.app.ui.components.ZenBackground
import com.sorobanzen.app.ui.components.ZenMark
import com.sorobanzen.app.ui.components.ZenScreenHeader
import com.sorobanzen.app.viewmodel.ZenViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: ZenViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expr by viewModel.expression.collectAsState()
    val displayVal by viewModel.displayText.collectAsState()
    val history by viewModel.historyList.collectAsState(initial = emptyList())
    val hapticsEnabled by viewModel.hapticEnabled.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeToolSheet by remember { mutableStateOf<String?>(null) }

    fun performHapticFeedback() {
        if (hapticsEnabled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    ZenBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            CalculatorTopBar(
                onHistoryClick = {
                    performHapticFeedback()
                    activeToolSheet = "history"
                },
                onSettingsClick = {
                    performHapticFeedback()
                    onNavigateToSettings()
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = stringResource(id = R.string.calculator_eyebrow),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = expr.replace("*", "×").replace("/", "÷").ifBlank { "—" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = displayVal,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToolBadge(
                    mark = "税",
                    label = stringResource(id = R.string.tax),
                    modifier = Modifier.weight(1f)
                ) {
                    performHapticFeedback()
                    activeToolSheet = "tax"
                }
                ToolBadge(
                    mark = "尺",
                    label = stringResource(id = R.string.traditional_units_short),
                    modifier = Modifier.weight(1f)
                ) {
                    performHapticFeedback()
                    activeToolSheet = "unit"
                }
                ToolBadge(
                    mark = "算",
                    label = stringResource(id = R.string.practice_short),
                    modifier = Modifier.weight(1f)
                ) {
                    performHapticFeedback()
                    activeToolSheet = "practice"
                }
            }

            CalculatorGrid(
                onKeyPress = viewModel::onCalculatorKeyPress,
                onTaxClick = {
                    performHapticFeedback()
                    activeToolSheet = "tax"
                },
                taxLabel = stringResource(id = R.string.tax),
                clearLabel = stringResource(id = R.string.clear),
                allClearLabel = stringResource(id = R.string.all_clear),
                hapticsEnabled = hapticsEnabled,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 420.dp)
                    .padding(bottom = 6.dp)
                    .navigationBarsPadding()
            )
        }

        if (activeToolSheet != null) {
            ModalBottomSheet(
                onDismissRequest = { activeToolSheet = null },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                scrimColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.32f),
                dragHandle = {
                    Surface(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 8.dp)
                            .size(width = 38.dp, height = 4.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.outline
                    ) {}
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.88f)
                ) {
                    when (activeToolSheet) {
                        "tax" -> TaxScreen(viewModel = viewModel)
                        "unit" -> UnitConverterScreen(viewModel = viewModel)
                        "practice" -> PracticeScreen(viewModel = viewModel)
                        "history" -> HistoryBottomSheetContent(
                            history = history,
                            onClearHistory = viewModel::clearHistory,
                            onItemClick = { item ->
                                viewModel.loadCalculatorResult(item.result)
                                activeToolSheet = null
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorTopBar(
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .heightIn(min = 48.dp)
    ) {
        TextButton(
            onClick = onHistoryClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(
                text = stringResource(id = R.string.history),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZenMark(modifier = Modifier.size(25.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.brand_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(id = R.string.settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ToolBadge(
    mark: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = mark,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HistoryBottomSheetContent(
    history: List<HistoryEntity>,
    onClearHistory: () -> Unit,
    onItemClick: (HistoryEntity) -> Unit
) {
    val dateFormatter = remember {
        SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.JAPAN)
    }
    var confirmClear by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZenScreenHeader(
                title = stringResource(id = R.string.history),
                eyebrow = stringResource(id = R.string.archive_eyebrow),
                subtitle = stringResource(id = R.string.history_subtitle),
                modifier = Modifier.weight(1f)
            )
            if (history.isNotEmpty()) {
                IconButton(onClick = { confirmClear = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.clear_history),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (history.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ZenMark(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.no_history),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    Surface(
                        onClick = { onItemClick(item) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(17.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (item.mode == "Tax" || item.mode == "消費税計算") {
                                        stringResource(id = R.string.history_mode_tax)
                                    } else {
                                        stringResource(id = R.string.history_mode_calculation)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = dateFormatter.format(Date(item.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = item.expression.replace("*", "×").replace("/", "÷"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "= ${item.result}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(28.dp)) }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(id = R.string.clear_history)) },
            text = { Text(stringResource(id = R.string.clear_history_confirmation)) },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        confirmClear = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text(stringResource(id = R.string.delete))
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}
