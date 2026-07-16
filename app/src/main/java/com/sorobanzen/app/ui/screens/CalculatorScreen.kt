package com.sorobanzen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorobanzen.app.R
import com.sorobanzen.app.ui.components.CalculatorGrid
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
    
    val hapticFeedback = LocalHapticFeedback.current

    // Bottom sheet states
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeToolSheet by remember { mutableStateOf<String?>(null) } // "tax", "unit", "tatami", "practice", "history"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- Top Status Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    activeToolSheet = "history"
                }) {
                    Text(
                        text = stringResource(id = R.string.history),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Text(
                    text = "そろばん禅",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )

                IconButton(onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToSettings()
                }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(id = R.string.settings),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            // --- Display Result Area ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                // Expression
                Text(
                    text = expr.replace("*", "×").replace("/", "÷"),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Big Result
                Text(
                    text = displayVal,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 60.sp
                )
            }

            // --- Horizontal Tools List ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ToolBadge(label = stringResource(id = R.string.tax)) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    activeToolSheet = "tax"
                }
                ToolBadge(label = stringResource(id = R.string.traditional_units)) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    activeToolSheet = "unit"
                }
                ToolBadge(label = stringResource(id = R.string.tatami_planner)) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    activeToolSheet = "tatami"
                }
                ToolBadge(label = stringResource(id = R.string.practice_mode)) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    activeToolSheet = "practice"
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Calculator Grid ---
            CalculatorGrid(
                onKeyPress = { key ->
                    if (key == "税率") {
                        activeToolSheet = "tax"
                    } else {
                        viewModel.onCalculatorKeyPress(key)
                    }
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // --- Bottom Sheets for tools ---
        if (activeToolSheet != null) {
            ModalBottomSheet(
                onDismissRequest = { activeToolSheet = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                ) {
                    when (activeToolSheet) {
                        "tax" -> TaxScreen(viewModel = viewModel)
                        "unit" -> UnitConverterScreen(viewModel = viewModel)
                        "tatami" -> TatamiPlannerScreen(viewModel = viewModel)
                        "practice" -> PracticeScreen(viewModel = viewModel)
                        "history" -> HistoryBottomSheetContent(
                            history = history,
                            onClearHistory = { viewModel.clearHistory() },
                            onItemClick = { item ->
                                // Populate expression/result
                                viewModel.onCalculatorKeyPress("AC")
                                // Load the result into input
                                for (ch in item.result) {
                                    viewModel.onCalculatorKeyPress(ch.toString())
                                }
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
fun ToolBadge(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun HistoryBottomSheetContent(
    history: List<com.sorobanzen.app.data.HistoryEntity>,
    onClearHistory: () -> Unit,
    onItemClick: (com.sorobanzen.app.data.HistoryEntity) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.history),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (history.isNotEmpty()) {
                Button(
                    onClick = onClearHistory,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(id = R.string.clear_history), color = MaterialTheme.colorScheme.tertiary, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.no_history),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { onItemClick(item) }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.mode,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = dateFormatter.format(Date(item.timestamp)),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.expression.replace("*", "×").replace("/", "÷"),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )

                        Text(
                            text = "= ${item.result}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
