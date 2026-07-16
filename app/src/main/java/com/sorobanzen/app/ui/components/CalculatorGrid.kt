package com.sorobanzen.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CalculatorGrid(
    onKeyPress: (String) -> Unit,
    onTaxClick: () -> Unit,
    taxLabel: String,
    clearLabel: String,
    allClearLabel: String,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    val buttons = listOf(
        listOf(allClearLabel, clearLabel, taxLabel, "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                row.forEach { char ->
                    val isOperator = char in listOf("÷", "×", "-", "+", "=")
                    val isAction = char in listOf(allClearLabel, clearLabel, taxLabel)
                    val weight = if (char == "0") 2f else 1f

                    val containerColor = when {
                        char == "=" -> MaterialTheme.colorScheme.primary
                        isOperator -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                        isAction -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                    val contentColor = when {
                        char == "=" -> MaterialTheme.colorScheme.onPrimary
                        isOperator -> MaterialTheme.colorScheme.secondary
                        isAction -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Surface(
                        onClick = {
                            if (hapticsEnabled) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            when (char) {
                                taxLabel -> onTaxClick()
                                clearLabel -> onKeyPress("C")
                                allClearLabel -> onKeyPress("AC")
                                else -> onKeyPress(char)
                            }
                        },
                        modifier = Modifier
                            .weight(weight)
                            .aspectRatio(if (char == "0") 2.05f else 1f),
                        shape = MaterialTheme.shapes.large,
                        color = containerColor,
                        contentColor = contentColor,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (char == "=") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        ),
                        tonalElevation = if (char == "=") 2.dp else 1.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = char,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = if (isOperator || isAction) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Medium
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
