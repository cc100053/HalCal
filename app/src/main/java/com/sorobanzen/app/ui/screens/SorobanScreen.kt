package com.sorobanzen.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorobanzen.app.R
import com.sorobanzen.app.domain.SorobanEngine
import com.sorobanzen.app.ui.components.ShakeResetListener
import com.sorobanzen.app.ui.components.ShareUtility
import com.sorobanzen.app.ui.components.SorobanCanvas
import com.sorobanzen.app.viewmodel.ZenViewModel

@Composable
fun SorobanScreen(
    viewModel: ZenViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    
    val rodsCount by viewModel.rodsCount.collectAsState()
    val rodValues by viewModel.rodValues.collectAsState()
    val sorobanValue by viewModel.sorobanValue.collectAsState()
    
    val soundEnabled by viewModel.soundEffectsEnabled.collectAsState()
    val hapticsEnabled by viewModel.hapticEnabled.collectAsState()

    val kanjiReading = remember(sorobanValue) { SorobanEngine.convertToKanji(sorobanValue) }
    val romajiReading = remember(sorobanValue) { SorobanEngine.convertToRomaji(sorobanValue) }

    var showInfoDialog by remember { mutableStateOf(false) }

    // Shake detector binding
    ShakeResetListener(enabled = hapticsEnabled) {
        viewModel.clearSoroban()
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        Toast.makeText(context, context.getString(R.string.clear_beads), Toast.LENGTH_SHORT).show()
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        // --- Sidebar (Values and readings) ---
        Column(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Text values section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%,d", sorobanValue),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = kanjiReading,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Text(
                    text = romajiReading,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }

            // Quick speech readout
            IconButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.speakJapaneseNumber(sorobanValue)
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Read out",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "読み上げ (Read)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Control Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Clear button
                Button(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.clearSoroban()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.clear_beads))
                }

                // Share & Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            ShareUtility.shareSorobanState(
                                context = context,
                                value = sorobanValue,
                                kanjiReading = kanjiReading,
                                romajiReading = romajiReading,
                                rodsCount = rodsCount,
                                rodValues = rodValues
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onBackground)
                    }

                    Button(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            showInfoDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }

                // Shake indicator instruction
                Text(
                    text = stringResource(id = R.string.shake_to_clear),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- Interactive Abacus ---
        SorobanCanvas(
            rodsCount = rodsCount,
            rodValues = rodValues,
            onRodValueChange = { index, value ->
                viewModel.updateRodValue(index, value)
            },
            soundEnabled = soundEnabled,
            hapticsEnabled = hapticsEnabled,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        )
    }

    // --- Info Dialog ---
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("そろばんの読み方と使い方 (How to Read)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. 五玉（天珠）: 上部にある1つの珠。下に下げると「5」を表します。",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "2. 一玉（地珠）: 下部にある4つの珠。上に上げるとそれぞれ「1」を表します。",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "3. 定位点: 梁（真ん中の棒）にある小さな点。一の位、千の位、百万の位などの基準点（桁の区切り）になります。",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "4. 数値の決定: 梁の近くに寄せられた珠の合計値が、その桁の値（0〜9）になります。",
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }) {
                    Text("閉じる (Close)")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
