package com.sorobanzen.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.sorobanzen.app.domain.TaxCalculator
import com.sorobanzen.app.ui.components.ZenBackground
import com.sorobanzen.app.ui.components.ZenCard
import com.sorobanzen.app.ui.components.ZenChoicePill
import com.sorobanzen.app.ui.components.ZenScreenHeader
import com.sorobanzen.app.viewmodel.ZenViewModel

@Composable
fun TaxScreen(
    viewModel: ZenViewModel,
    modifier: Modifier = Modifier
) {
    val inputAmount by viewModel.taxAmountExcl.collectAsState()
    val rate by viewModel.taxRate.collectAsState()
    val breakdown by viewModel.taxBreakdown.collectAsState()
    val isValidAmount = inputAmount.toDoubleOrNull()?.let { it.isFinite() && it >= 0.0 } == true
    val showError = inputAmount.isNotBlank() && !isValidAmount

    ZenBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            ZenScreenHeader(
                title = stringResource(id = R.string.tax_calculator),
                eyebrow = stringResource(id = R.string.daily_tools_eyebrow),
                subtitle = stringResource(id = R.string.tax_subtitle)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = inputAmount,
                onValueChange = viewModel::updateTaxInput,
                label = { Text(stringResource(id = R.string.amount)) },
                prefix = { Text("¥") },
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = stringResource(id = R.string.tax_rate),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ZenChoicePill(
                    label = stringResource(id = R.string.standard_label),
                    selected = rate == TaxCalculator.TaxRate.STANDARD,
                    onClick = { viewModel.setTaxRate(TaxCalculator.TaxRate.STANDARD) },
                    modifier = Modifier.weight(1f)
                )
                ZenChoicePill(
                    label = stringResource(id = R.string.reduced_label),
                    selected = rate == TaxCalculator.TaxRate.REDUCED,
                    onClick = { viewModel.setTaxRate(TaxCalculator.TaxRate.REDUCED) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = viewModel::addTaxToCalculator,
                    enabled = isValidAmount,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(stringResource(id = R.string.add_tax))
                }
                Button(
                    onClick = viewModel::removeTaxFromCalculator,
                    enabled = isValidAmount,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(stringResource(id = R.string.remove_tax))
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            AnimatedVisibility(
                visible = breakdown != null,
                enter = fadeIn() + slideInVertically { it / 5 },
                exit = fadeOut()
            ) {
                breakdown?.let { result ->
                    ZenCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.breakdown),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        BreakdownRow(
                            label = stringResource(id = R.string.amount_excl_tax),
                            value = "¥${result.originalAmount}"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        BreakdownRow(
                            label = stringResource(id = R.string.tax_amount),
                            value = "¥${result.taxAmount}",
                            accent = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        BreakdownRow(
                            label = stringResource(id = R.string.amount_incl_tax),
                            value = "¥${result.totalAmount}",
                            total = true
                        )
                    }
                }
            }

            if (breakdown == null) {
                Text(
                    text = stringResource(id = R.string.tax_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    accent: Boolean = false,
    total: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (total) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (total) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (total) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = when {
                total -> MaterialTheme.colorScheme.primary
                accent -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
