package com.sorobanzen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorobanzen.app.R
import com.sorobanzen.app.viewmodel.ZenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ZenViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rodsCount by viewModel.rodsCount.collectAsState()
    val soundEffects by viewModel.soundEffectsEnabled.collectAsState()
    val haptics by viewModel.hapticEnabled.collectAsState()
    val ttsEnabled by viewModel.ttsEnabled.collectAsState()
    val language by viewModel.language.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        // Top Back Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.settings),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Rods Count Selector
        SettingsGroupHeader(title = stringResource(id = R.string.rods_count))
        Text(
            text = "$rodsCount 桁 (Rods)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Slider(
            value = rodsCount.toFloat(),
            onValueChange = { viewModel.setRodsCount(it.toInt()) },
            valueRange = 7f..17f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Sound & Haptics
        SettingsGroupHeader(title = "操作設定 (Interaction)")
        
        // Sound Switch
        SettingsToggleRow(
            title = stringResource(id = R.string.sound_effects),
            checked = soundEffects,
            onCheckedChange = { viewModel.setSoundEffectsEnabled(it) }
        )

        // Haptic Switch
        SettingsToggleRow(
            title = stringResource(id = R.string.haptics),
            checked = haptics,
            onCheckedChange = { viewModel.setHapticEnabled(it) }
        )

        // TTS Switch
        SettingsToggleRow(
            title = stringResource(id = R.string.japanese_readout),
            checked = ttsEnabled,
            onCheckedChange = { viewModel.setTtsEnabled(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Language Selector
        SettingsGroupHeader(title = stringResource(id = R.string.language))
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.setLanguage("ja") }
            ) {
                RadioButton(
                    selected = language == "ja",
                    onClick = { viewModel.setLanguage("ja") },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                )
                Text("日本語 (Japanese)", modifier = Modifier.padding(start = 8.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.setLanguage("en") }
            ) {
                RadioButton(
                    selected = language == "en",
                    onClick = { viewModel.setLanguage("en") },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                )
                Text("English", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        )
    }
}
