package com.sorobanzen.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sorobanzen.app.data.AppPreferences
import com.sorobanzen.app.data.HistoryDao
import com.sorobanzen.app.data.HistoryEntity
import com.sorobanzen.app.domain.CalculatorEngine
import com.sorobanzen.app.domain.CalculatorState
import com.sorobanzen.app.domain.SorobanEngine
import com.sorobanzen.app.domain.TaxCalculator
import com.sorobanzen.app.domain.UnitConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class ZenViewModel(
    private val historyDao: HistoryDao,
    private val preferences: AppPreferences
) : ViewModel() {

    private val calculator = CalculatorEngine()

    // --- Calculator Portrait States ---
    /** Completed expression, shown only once "=" has produced a result. */
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    /** The single input line: the expression while typing, the result after "=". */
    private val _displayText = MutableStateFlow("0")
    val displayText: StateFlow<String> = _displayText.asStateFlow()

    // --- Soroban Landscape States ---
    val rodsCount: Int = SorobanEngine.ROD_COUNT

    private val _rodValues = MutableStateFlow(IntArray(rodsCount))
    val rodValues: StateFlow<IntArray> = _rodValues.asStateFlow()

    private val _sorobanValue = MutableStateFlow(0L)
    val sorobanValue: StateFlow<Long> = _sorobanValue.asStateFlow()

    // --- Settings States ---
    private val _soundEffectsEnabled = MutableStateFlow(preferences.soundEffectsEnabled)
    val soundEffectsEnabled: StateFlow<Boolean> = _soundEffectsEnabled.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(preferences.hapticsEnabled)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    // --- History Flow ---
    val historyList = historyDao.getAllHistory()

    // --- Tax States ---
    private val _taxAmountExcl = MutableStateFlow("0")
    val taxAmountExcl: StateFlow<String> = _taxAmountExcl.asStateFlow()

    private val _taxRate = MutableStateFlow(TaxCalculator.TaxRate.STANDARD)
    val taxRate: StateFlow<TaxCalculator.TaxRate> = _taxRate.asStateFlow()

    private val _taxBreakdown = MutableStateFlow<TaxCalculator.TaxBreakdown?>(null)
    val taxBreakdown: StateFlow<TaxCalculator.TaxBreakdown?> = _taxBreakdown.asStateFlow()

    // --- Unit Converter States ---
    private val _unitCategory = MutableStateFlow("length") // "length", "area", "volume", "weight"
    val unitCategory: StateFlow<String> = _unitCategory.asStateFlow()

    private val _unitValue = MutableStateFlow("1.0")
    val unitValue: StateFlow<String> = _unitValue.asStateFlow()

    /** Which unit [unitValue] is expressed in. Defaults to the category's metric unit. */
    private val _unitInput = MutableStateFlow(UnitConverter.baseUnit("length")!!.key)
    val unitInput: StateFlow<String> = _unitInput.asStateFlow()

    // --- Calculator Operations ---
    fun onCalculatorKeyPress(key: String) {
        val state = calculator.press(key)
        publishCalculatorState(state)

        state.completedExpression?.let { completedExpression ->
            saveToHistory(completedExpression, state.display, "通常計算")
        }
    }

    fun loadCalculatorResult(result: String) {
        val state = calculator.loadResult(result)
        publishCalculatorState(state)
    }

    private fun publishCalculatorState(state: CalculatorState) {
        _expression.value = if (state.isResultDisplayed) state.expression else ""
        _displayText.value = state.entry.ifEmpty { "0" }
    }

    // --- Soroban Operations ---
    fun updateRodValue(rodIndex: Int, value: Int) {
        val currentRods = _rodValues.value.copyOf()
        if (rodIndex in currentRods.indices) {
            currentRods[rodIndex] = value.coerceIn(0, 9)
            _rodValues.value = currentRods
            updateSorobanValueFromRods()
        }
    }

    fun updateSorobanValueFromRods() {
        var total = 0L
        for (digit in _rodValues.value) {
            total = total * 10 + digit
        }
        _sorobanValue.value = total
    }

    fun clearSoroban() {
        _rodValues.value = IntArray(rodsCount)
        _sorobanValue.value = 0L
    }

    fun restoreSoroban(values: IntArray) {
        if (values.size != rodsCount) return
        _rodValues.value = values.copyOf().also { rods ->
            rods.indices.forEach { index -> rods[index] = rods[index].coerceIn(0, 9) }
        }
        updateSorobanValueFromRods()
    }

    fun loadNumberToSoroban(num: Long) {
        val rods = IntArray(rodsCount)
        var temp = num.coerceAtLeast(0L)
        for (i in rodsCount - 1 downTo 0) {
            rods[i] = (temp % 10).toInt()
            temp /= 10
        }
        _rodValues.value = rods
        updateSorobanValueFromRods()
    }

    // --- Database Operations ---
    private fun saveToHistory(expr: String, resultStr: String, mode: String) {
        viewModelScope.launch {
            historyDao.insertHistory(
                HistoryEntity(
                    expression = expr,
                    result = resultStr,
                    mode = mode
                )
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyDao.clearAllHistory()
        }
    }

    // --- Settings Setters ---
    fun setSoundEffectsEnabled(enabled: Boolean) {
        _soundEffectsEnabled.value = enabled
        preferences.soundEffectsEnabled = enabled
    }

    fun setHapticEnabled(enabled: Boolean) {
        _hapticEnabled.value = enabled
        preferences.hapticsEnabled = enabled
    }

    // --- Tax Calculator Operations ---
    fun updateTaxInput(input: String) {
        _taxAmountExcl.value = input
        _taxBreakdown.value = parseTaxAmount(input)?.let {
            TaxCalculator.addTax(it, _taxRate.value)
        }
    }

    fun setTaxRate(rate: TaxCalculator.TaxRate) {
        _taxRate.value = rate
        calculateTaxBreakdown()
    }

    private fun calculateTaxBreakdown() {
        _taxBreakdown.value = parseTaxAmount(_taxAmountExcl.value)?.let {
            TaxCalculator.addTax(it, _taxRate.value)
        }
    }

    fun addTaxToCalculator() {
        parseTaxAmount(_taxAmountExcl.value)?.let { amount ->
            val breakdown = TaxCalculator.addTax(amount, _taxRate.value)
            _taxBreakdown.value = breakdown
            val formattedResult = breakdown.totalAmount.stripTrailingZeros().toPlainString()
            loadCalculatorResult(formattedResult)
            saveToHistory(
                expr = "${breakdown.originalAmount} + 消費税 (${(breakdown.taxRate * BigDecimal("100")).toInt()}%)",
                resultStr = formattedResult,
                mode = "消費税計算"
            )
        }
    }

    fun removeTaxFromCalculator() {
        parseTaxAmount(_taxAmountExcl.value)?.let { amount ->
            val breakdown = TaxCalculator.removeTax(amount, _taxRate.value)
            _taxBreakdown.value = breakdown
            val formattedResult = breakdown.originalAmount.stripTrailingZeros().toPlainString()
            loadCalculatorResult(formattedResult)
            saveToHistory(
                expr = "${breakdown.totalAmount} - 消費税抜 (${(breakdown.taxRate * BigDecimal("100")).toInt()}%)",
                resultStr = formattedResult,
                mode = "消費税計算"
            )
        }
    }

    private fun parseTaxAmount(input: String): Double? =
        input.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 }

    // --- Traditional Unit Converter Operations ---
    fun setUnitCategory(category: String) {
        _unitCategory.value = category
        // Units do not carry across categories, so input falls back to the new category's metric unit.
        UnitConverter.baseUnit(category)?.let { _unitInput.value = it.key }
    }

    fun setUnitValue(value: String) {
        _unitValue.value = value
    }

    /** Types the same quantity in a different unit: 1.5 m becomes 4.95 尺, not 1.5 尺. */
    fun setUnitInput(key: String, value: String) {
        _unitInput.value = key
        _unitValue.value = value
    }

    /**
     * The calculator display when it is something a tool sheet can accept. Zero, an error, and a
     * negative all return null, so opening a sheet on an idle calculator leaves your typing alone.
     */
    private fun usableDisplayValue(): String? = _displayText.value.takeIf { text ->
        text.toDoubleOrNull()?.let { it.isFinite() && it > 0.0 } == true
    }

    fun seedTaxFromCalculator() {
        usableDisplayValue()?.let(::updateTaxInput)
    }

    fun seedUnitFromCalculator() {
        usableDisplayValue()?.let(::setUnitValue)
    }

}

class ZenViewModelFactory(
    private val historyDao: HistoryDao,
    private val preferences: AppPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ZenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ZenViewModel(historyDao, preferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
