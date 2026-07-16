package com.sorobanzen.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorobanzen.app.ui.theme.LightAccentMoss
import com.sorobanzen.app.ui.theme.LightAccentSakura
import com.sorobanzen.app.ui.theme.LightKeypadBg

@Composable
fun CalculatorGrid(
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    val buttons = listOf(
        listOf("AC", "C", "税率", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { char ->
                    val isOperator = char in listOf("÷", "×", "-", "+", "=")
                    val isAction = char in listOf("AC", "C", "税率")
                    
                    val weight = if (char == "0") 2f else 1f
                    
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .aspectRatio(if (char == "0") 2.1f else 1f)
                            .clip(CircleShape)
                            .background(
                                when {
                                    char == "=" -> MaterialTheme.colorScheme.primary
                                    isOperator -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    isAction -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                }
                            )
                            .clickable {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onKeyPress(char)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                char == "=" -> MaterialTheme.colorScheme.onPrimary
                                isOperator -> MaterialTheme.colorScheme.secondary
                                isAction -> MaterialTheme.colorScheme.onBackground
                                else -> MaterialTheme.colorScheme.onBackground
                            }
                        )
                    }
                }
            }
        }
    }
}
