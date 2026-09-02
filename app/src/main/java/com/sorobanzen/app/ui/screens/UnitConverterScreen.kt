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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val inputUnit by viewModel.unitInput.collectAsState()
    val inputValueStr by viewModel.unitValue.collectAsState()
    val inputValue = inputValueStr.toDoubleOrNull()
    val isValidValue = inputValue?.let { it.isFinite() && it >= 0.0 } == true
    val showError = inputValueStr.isNotBlank() && !isValidValue

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

            OutlinedTextField(
                value = inputValueStr,
                onValueChange = viewModel::setUnitValue,
                label = { Text(unitLabel(inputUnit)) },
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
                inputValue?.let { value ->
                    val results = UnitConverter.convertFrom(category, inputUnit, value)
                    ZenCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.converted_units),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(id = R.string.tap_unit_to_input),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        results.forEachIndexed { index, (unit, converted) ->
                            ResultRow(
                                label = unitLabel(unit.key),
                                value = formatUnit(converted, unit.suffix),
                                // Tapping keeps the quantity and changes the unit it is typed in.
                                onClick = {
                                    viewModel.setUnitInput(unit.key, formatValue(converted))
                                }
                            )
                            if (index != results.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
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

/** Display name for a unit key, matching the labels used by the category pills. */
@Composable
private fun unitLabel(key: String): String = when (key) {
    "m" -> stringResource(id = R.string.metric_meters)
    "sqm" -> stringResource(id = R.string.metric_square_meters)
    "l" -> stringResource(id = R.string.metric_liters)
    "kg" -> stringResource(id = R.string.metric_kilograms)
    "shaku" -> stringResource(id = R.string.shaku)
    "sun" -> stringResource(id = R.string.sun)
    "ken" -> stringResource(id = R.string.ken)
    "tsubo" -> stringResource(id = R.string.tsubo)
    "jo" -> stringResource(id = R.string.jo)
    "sho" -> stringResource(id = R.string.sho)
    "go" -> stringResource(id = R.string.go)
    "kan" -> stringResource(id = R.string.kan)
    "momme" -> stringResource(id = R.string.momme)
    else -> stringResource(id = R.string.metric_value)
}

private fun formatUnit(value: Double, suffix: String): String =
    String.format(Locale.ROOT, "%.4f %s", value, suffix)

/** Bare number for the input box: no suffix, no trailing zeros to delete before retyping. */
private fun formatValue(value: Double): String =
    String.format(Locale.ROOT, "%.4f", value).trimEnd('0').trimEnd('.').ifEmpty { "0" }

@Composable
fun ResultRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(horizontal = 4.dp, vertical = 8.dp),
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
}
