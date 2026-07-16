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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.sorobanzen.app.domain.TaxCalculator
import com.sorobanzen.app.viewmodel.ZenViewModel

@Composable
fun TaxScreen(
    viewModel: ZenViewModel,
    modifier: Modifier = Modifier
) {
    val inputAmount by viewModel.taxAmountExcl.collectAsState()
    val rate by viewModel.taxRate.collectAsState()
    val breakdown by viewModel.taxBreakdown.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.tax_calculator),
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input
        OutlinedTextField(
            value = inputAmount,
            onValueChange = { viewModel.updateTaxInput(it) },
            label = { Text(stringResource(id = R.string.amount_excl_tax)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tax Rate Selection (Standard vs Reduced)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.setTaxRate(TaxCalculator.TaxRate.STANDARD) }
            ) {
                RadioButton(
                    selected = rate == TaxCalculator.TaxRate.STANDARD,
                    onClick = { viewModel.setTaxRate(TaxCalculator.TaxRate.STANDARD) },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = stringResource(id = R.string.standard_label),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.setTaxRate(TaxCalculator.TaxRate.REDUCED) }
            ) {
                RadioButton(
                    selected = rate == TaxCalculator.TaxRate.REDUCED,
                    onClick = { viewModel.setTaxRate(TaxCalculator.TaxRate.REDUCED) },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = stringResource(id = R.string.reduced_label),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.addTaxToCalculator() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(id = R.string.add_tax), color = MaterialTheme.colorScheme.onPrimary)
            }

            Button(
                onClick = { viewModel.removeTaxFromCalculator() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(id = R.string.remove_tax), color = MaterialTheme.colorScheme.onSecondary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Breakdown Card
        breakdown?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "計算明細 (Breakdown)",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(id = R.string.amount_excl_tax), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(text = "¥${it.originalAmount}", fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(id = R.string.tax_amount), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(text = "¥${it.taxAmount}", color = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.height(8.dp).fillMaxWidth().background(MaterialTheme.colorScheme.outline))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(id = R.string.amount_incl_tax), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = "¥${it.totalAmount}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
