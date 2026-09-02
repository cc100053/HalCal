package com.sorobanzen.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** One key: the glyph the user reads, and the token CalculatorEngine receives. */
private data class Key(
    val glyph: String,
    val token: String,
    val glyphSize: Int,
    val accent: Boolean,
    val span: Float = 1f
)

private val ACTION_ROW_HEIGHT = 62.dp
private val KEY_ROW_HEIGHT = 74.dp

@Composable
fun CalculatorGrid(
    onKeyPress: (String) -> Unit,
    signLabel: String,
    clearLabel: String,
    allClearLabel: String,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    fun press(token: String) {
        if (hapticsEnabled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        onKeyPress(token)
    }

    val rows = listOf(
        listOf(
            Key(allClearLabel, "AC", 17, accent = true),
            Key(clearLabel, "C", 17, accent = true),
            Key(signLabel, "±", 17, accent = true),
            Key("÷", "÷", 28, accent = true)
        ),
        listOf(
            Key("7", "7", 30, accent = false),
            Key("8", "8", 30, accent = false),
            Key("9", "9", 30, accent = false),
            Key("×", "×", 28, accent = true)
        ),
        listOf(
            Key("4", "4", 30, accent = false),
            Key("5", "5", 30, accent = false),
            Key("6", "6", 30, accent = false),
            Key("−", "-", 28, accent = true)
        ),
        listOf(
            Key("1", "1", 30, accent = false),
            Key("2", "2", 30, accent = false),
            Key("3", "3", 30, accent = false),
            Key("+", "+", 28, accent = true)
        ),
        listOf(
            Key("0", "0", 30, accent = false, span = 2f),
            Key("・", ".", 30, accent = false)
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        rows.forEachIndexed { index, row ->
            val height = if (index == 0) ACTION_ROW_HEIGHT else KEY_ROW_HEIGHT
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    GlyphKey(key = key, height = height, onClick = { press(key.token) })
                }
                // The equals key closes the last row as a filled ensō-dark disc.
                if (index == rows.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(height),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            onClick = { press("=") },
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "＝",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Borderless key: paper shows through, only the glyph and its press bloom are visible. */
@Composable
private fun RowScope.GlyphKey(
    key: Key,
    height: Dp,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .weight(key.span)
            .height(height),
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
        contentColor = if (key.accent) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onBackground
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = key.glyph,
                style = MaterialTheme.typography.titleLarge,
                fontSize = key.glyphSize.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
