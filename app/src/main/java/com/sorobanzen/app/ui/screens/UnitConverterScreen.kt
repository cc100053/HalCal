package com.sorobanzen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorobanzen.app.R
import com.sorobanzen.app.domain.UnitConverter
import com.sorobanzen.app.viewmodel.ZenViewModel

@Composable
fun UnitConverterScreen(
    viewModel: ZenViewModel,
    modifier: Modifier = Modifier
) {
    val category by viewModel.unitCategory.collectAsState()
    val metricValueStr by viewModel.metricValue.collectAsState()

    val categories = listOf("length", "area", "volume", "weight")
    val categoryLabels = listOf(
        R.string.length,
        R.string.area,
        R.string.volume,
        R.string.weight
    )

    val selectedIndex = categories.indexOf(category).coerceAtLeast(0)
    val metricDouble = metricValueStr.toDoubleOrNull() ?: 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.traditional_units),
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Categories Tabs
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            categories.forEachIndexed { index, cat ->
                Tab(
                    selected = category == cat,
                    onClick = { viewModel.setUnitCategory(cat) },
                    text = { Text(stringResource(id = categoryLabels[index]), fontSize = 14.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Input
        val inputLabel = when (category) {
            "length" -> "メートル (m)"
            "area" -> "平方メートル (m²)"
            "volume" -> "リットル (L)"
            "weight" -> "キログラム (kg)"
            else -> "値 (Metric)"
        }

        OutlinedTextField(
            value = metricValueStr,
            onValueChange = { viewModel.setMetricValue(it) },
            label = { Text(inputLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Conversion Results Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "変換結果 (Converted Units)",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            when (category) {
                "length" -> {
                    val shaku = UnitConverter.metersToShaku(metricDouble)
                    val sun = UnitConverter.metersToSun(metricDouble)
                    val ken = UnitConverter.metersToKen(metricDouble)

                    ResultRow(label = stringResource(id = R.string.shaku), value = String.format("%.4f 尺", shaku))
                    ResultRow(label = stringResource(id = R.string.sun), value = String.format("%.4f 寸", sun))
                    ResultRow(label = stringResource(id = R.string.ken), value = String.format("%.4f 間", ken))
                }
                "area" -> {
                    val tsubo = UnitConverter.sqmToTsubo(metricDouble)
                    // Traditional rough conversion: 1 tsubo ≈ 2 Tatami mats (Jo)
                    val jo = tsubo * 2.0

                    ResultRow(label = stringResource(id = R.string.tsubo), value = String.format("%.4f 坪", tsubo))
                    ResultRow(label = "畳 (畳 / じょう)", value = String.format("%.4f 畳", jo))
                }
                "volume" -> {
                    val sho = UnitConverter.litersToSho(metricDouble)
                    val go = UnitConverter.litersToGo(metricDouble)

                    ResultRow(label = stringResource(id = R.string.sho), value = String.format("%.4f 升", sho))
                    ResultRow(label = stringResource(id = R.string.go), value = String.format("%.4f 合", go))
                }
                "weight" -> {
                    val kan = UnitConverter.kgToKan(metricDouble)
                    val momme = UnitConverter.kgToMomme(metricDouble)

                    ResultRow(label = stringResource(id = R.string.kan), value = String.format("%.4f 貫", kan))
                    ResultRow(label = stringResource(id = R.string.momme), value = String.format("%.4f 匁", momme))
                }
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
