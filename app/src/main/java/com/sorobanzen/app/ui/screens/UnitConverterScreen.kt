package com.sorobanzen.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sorobanzen.app.R
import com.sorobanzen.app.domain.UnitConverter
import com.sorobanzen.app.ui.components.ZenBackground
import com.sorobanzen.app.ui.components.ZenCard
import com.sorobanzen.app.ui.components.ZenChoicePill
import com.sorobanzen.app.ui.components.ZenScreenHeader
import com.sorobanzen.app.viewmodel.ZenViewModel
import java.util.Locale

@Composable
fun UnitConverterScreen(
    viewModel: ZenViewModel,
    modifier: Modifier = Modifier
) {
    val category by viewModel.unitCategory.collectAsState()
    val metricValueStr by viewModel.metricValue.collectAsState()
    val metricValue = metricValueStr.toDoubleOrNull()
    val isValidValue = metricValue?.let { it.isFinite() && it >= 0.0 } == true
    val showError = metricValueStr.isNotBlank() && !isValidValue

    val categories = listOf(
        "length" to R.string.length,
        "area" to R.string.area,
        "volume" to R.string.volume,
        "weight" to R.string.weight
    )

    ZenBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            ZenScreenHeader(
                title = stringResource(id = R.string.traditional_units),
                eyebrow = stringResource(id = R.string.cultural_tools_eyebrow),
                subtitle = stringResource(id = R.string.units_subtitle)
            )

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (token, labelRes) ->
                    ZenChoicePill(
                        label = stringResource(id = labelRes),
                        selected = category == token,
                        onClick = { viewModel.setUnitCategory(token) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val inputLabel = when (category) {
                "length" -> stringResource(id = R.string.metric_meters)
                "area" -> stringResource(id = R.string.metric_square_meters)
                "volume" -> stringResource(id = R.string.metric_liters)
                "weight" -> stringResource(id = R.string.metric_kilograms)
                else -> stringResource(id = R.string.metric_value)
            }

            OutlinedTextField(
                value = metricValueStr,
                onValueChange = viewModel::setMetricValue,
                label = { Text(inputLabel) },
                supportingText = if (showError) {
                    { Text(stringResource(id = R.string.valid_amount_error)) }
                } else {
                    null
                },
                isError = showError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = isValidValue,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                metricValue?.let { value ->
                    val results = conversionResults(category, value)
                    ZenCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.converted_units),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        results.forEachIndexed { index, result ->
                            ResultRow(label = result.first, value = result.second)
                            if (index != results.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }

            if (!isValidValue) {
                ZenCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.conversion_prompt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun conversionResults(category: String, value: Double): List<Pair<String, String>> {
    return when (category) {
        "length" -> listOf(
            stringResource(id = R.string.shaku) to formatUnit(UnitConverter.metersToShaku(value), "尺"),
            stringResource(id = R.string.sun) to formatUnit(UnitConverter.metersToSun(value), "寸"),
            stringResource(id = R.string.ken) to formatUnit(UnitConverter.metersToKen(value), "間")
        )
        "area" -> {
            val tsubo = UnitConverter.sqmToTsubo(value)
            listOf(
                stringResource(id = R.string.tsubo) to formatUnit(tsubo, "坪"),
                stringResource(id = R.string.jo) to formatUnit(tsubo * 2.0, "畳")
            )
        }
        "volume" -> listOf(
            stringResource(id = R.string.sho) to formatUnit(UnitConverter.litersToSho(value), "升"),
            stringResource(id = R.string.go) to formatUnit(UnitConverter.litersToGo(value), "合")
        )
        "weight" -> listOf(
            stringResource(id = R.string.kan) to formatUnit(UnitConverter.kgToKan(value), "貫"),
            stringResource(id = R.string.momme) to formatUnit(UnitConverter.kgToMomme(value), "匁")
        )
        else -> emptyList()
    }
}

private fun formatUnit(value: Double, suffix: String): String {
    return String.format(Locale.ROOT, "%.4f %s", value, suffix)
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
