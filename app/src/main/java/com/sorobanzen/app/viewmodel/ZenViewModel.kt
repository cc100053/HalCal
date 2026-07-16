package com.sorobanzen.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sorobanzen.app.data.HistoryDao
import com.sorobanzen.app.data.HistoryEntity
import com.sorobanzen.app.domain.MathEvaluator
import com.sorobanzen.app.domain.SorobanEngine
import com.sorobanzen.app.domain.TaxCalculator
import com.sorobanzen.app.domain.UnitConverter
import com.sorobanzen.app.domain.TatamiPlanner
import kotlin.math.pow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.random.Random

class ZenViewModel(private val historyDao: HistoryDao) : ViewModel() {

    // --- Calculator Portrait States ---
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _displayText = MutableStateFlow("0")
    val displayText: StateFlow<String> = _displayText.asStateFlow()

    private var isResultDisplayed = false

    // --- Soroban Landscape States ---
    private val _rodsCount = MutableStateFlow(13)
    val rodsCount: StateFlow<Int> = _rodsCount.asStateFlow()

    private val _rodValues = MutableStateFlow(IntArray(13))
    val rodValues: StateFlow<IntArray> = _rodValues.asStateFlow()

    private val _sorobanValue = MutableStateFlow(0L)
    val sorobanValue: StateFlow<Long> = _sorobanValue.asStateFlow()

    // --- Settings States ---
    private val _soundEffectsEnabled = MutableStateFlow(true)
    val soundEffectsEnabled: StateFlow<Boolean> = _soundEffectsEnabled.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(true)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(true)
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

    private val _language = MutableStateFlow("ja") // "ja" or "en"
    val language: StateFlow<String> = _language.asStateFlow()

    // --- History Flow ---
    val historyList = historyDao.getAllHistory()

    // --- TTS Event Flow ---
    private val _ttsEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val ttsEvent: SharedFlow<String> = _ttsEvent.asSharedFlow()

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

    private val _metricValue = MutableStateFlow("1.0")
    val metricValue: StateFlow<String> = _metricValue.asStateFlow()

    // --- Tatami Planner States ---
    private val _tatamiWidth = MutableStateFlow(3.6)
    val tatamiWidth: StateFlow<Double> = _tatamiWidth.asStateFlow()

    private val _tatamiLength = MutableStateFlow(3.6)
    val tatamiLength: StateFlow<Double> = _tatamiLength.asStateFlow()

    private val _tatamiRegion = MutableStateFlow(TatamiPlanner.Region.TOKYO)
    val tatamiRegion: StateFlow<TatamiPlanner.Region> = _tatamiRegion.asStateFlow()

    // --- Practice/Training Mode States ---
    private val _isPracticeActive = MutableStateFlow(false)
    val isPracticeActive: StateFlow<Boolean> = _isPracticeActive.asStateFlow()

    private val _practiceTimeLeft = MutableStateFlow(60)
    val practiceTimeLeft: StateFlow<Int> = _practiceTimeLeft.asStateFlow()

    private val _practiceScore = MutableStateFlow(0)
    val practiceScore: StateFlow<Int> = _practiceScore.asStateFlow()

    private val _practiceTotal = MutableStateFlow(0)
    val practiceTotal: StateFlow<Int> = _practiceTotal.asStateFlow()

    private val _currentProblemText = MutableStateFlow("")
    val currentProblemText: StateFlow<String> = _currentProblemText.asStateFlow()

    private var currentProblemAnswer = 0L

    private val _practiceInput = MutableStateFlow("")
    val practiceInput: StateFlow<String> = _practiceInput.asStateFlow()

    private val _practiceFeedback = MutableStateFlow("") // Correct/Incorrect feedback
    val practiceFeedback: StateFlow<String> = _practiceFeedback.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Observe rods count to update the rodValues array size
        viewModelScope.launch {
            _rodsCount.collect { count ->
                _rodValues.value = IntArray(count)
                updateSorobanValueFromRods()
            }
        }
    }

    // --- Calculator Operations ---
    fun onCalculatorKeyPress(key: String) {
        when (key) {
            "C" -> {
                if (_displayText.value.length > 1) {
                    _displayText.value = _displayText.value.dropLast(1)
                } else {
                    _displayText.value = "0"
                }
            }
            "AC" -> {
                _expression.value = ""
                _displayText.value = "0"
                isResultDisplayed = false
            }
            "+", "-", "×", "÷" -> {
                val operator = when (key) {
                    "×" -> "*"
                    "÷" -> "/"
                    else -> key
                }
                
                // If a result is displayed, chain the expression using the result
                if (isResultDisplayed) {
                    _expression.value = _displayText.value + operator
                    isResultDisplayed = false
                } else {
                    _expression.value += _displayText.value + operator
                }
                _displayText.value = "0"
            }
            "=" -> {
                val fullExpr = _expression.value + _displayText.value
                if (fullExpr.isNotEmpty()) {
                    val result = MathEvaluator.evaluate(fullExpr)
                    if (result.isNaN()) {
                        _displayText.value = "Error"
                    } else {
                        // Format result cleanly
                        val formattedResult = if (result % 1.0 == 0.0) {
                            result.toLong().toString()
                        } else {
                            String.format("%.4f", result).trimEnd('0').trimEnd('.')
                        }
                        
                        _displayText.value = formattedResult
                        _expression.value = fullExpr
                        isResultDisplayed = true
                        
                        // Speak out the result if TTS is enabled
                        if (result % 1.0 == 0.0 && result >= 0 && result < 10000000000000000L) {
                            speakJapaneseNumber(result.toLong())
                        }
                        
                        // Save to Database
                        saveToHistory(fullExpr, formattedResult, "Normal")
                    }
                }
            }
            "." -> {
                if (isResultDisplayed) {
                    _displayText.value = "0."
                    isResultDisplayed = false
                } else if (!_displayText.value.contains(".")) {
                    _displayText.value += "."
                }
            }
            else -> { // Number keys 0-9
                if (_displayText.value == "0" || isResultDisplayed) {
                    _displayText.value = key
                    isResultDisplayed = false
                } else {
                    _displayText.value += key
                }
            }
        }
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
        val rods = _rodValues.value
        // Rod index 0 is left (high weight), rod index N-1 is right (low weight: 10^0)
        val size = rods.size
        for (i in 0 until size) {
            val weight = 10.0.pow(size - 1 - i).toLong()
            total += rods[i] * weight
        }
        _sorobanValue.value = total
    }

    fun clearSoroban() {
        _rodValues.value = IntArray(_rodsCount.value)
        _sorobanValue.value = 0L
    }

    fun loadNumberToSoroban(num: Long) {
        val count = _rodsCount.value
        val rods = IntArray(count)
        var temp = num
        for (i in count - 1 downTo 0) {
            rods[i] = (temp % 10).toInt()
            temp /= 10
        }
        _rodValues.value = rods
        _sorobanValue.value = num
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
    fun setRodsCount(count: Int) {
        _rodsCount.value = count.coerceIn(7, 17)
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        _soundEffectsEnabled.value = enabled
    }

    fun setHapticEnabled(enabled: Boolean) {
        _hapticEnabled.value = enabled
    }

    fun setTtsEnabled(enabled: Boolean) {
        _ttsEnabled.value = enabled
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    // --- Japanese TTS Trigger ---
    fun speakJapaneseNumber(number: Long) {
        if (_ttsEnabled.value) {
            val reading = SorobanEngine.convertToKanji(number)
            viewModelScope.launch {
                _ttsEvent.emit(reading)
            }
        }
    }

    // --- Tax Calculator Operations ---
    fun updateTaxInput(input: String) {
        _taxAmountExcl.value = input
        calculateTaxBreakdown()
    }

    fun setTaxRate(rate: TaxCalculator.TaxRate) {
        _taxRate.value = rate
        calculateTaxBreakdown()
    }

    private fun calculateTaxBreakdown() {
        val amount = _taxAmountExcl.value.toDoubleOrNull() ?: 0.0
        _taxBreakdown.value = TaxCalculator.addTax(amount, _taxRate.value)
    }

    fun addTaxToCalculator() {
        val breakdown = _taxBreakdown.value
        if (breakdown != null) {
            _displayText.value = breakdown.totalAmount.toLong().toString()
            saveToHistory(
                expr = "${breakdown.originalAmount} + 消費税 (${(breakdown.taxRate * BigDecimal("100")).toInt()}%)",
                resultStr = breakdown.totalAmount.toLong().toString(),
                mode = "Tax"
            )
        }
    }

    fun removeTaxFromCalculator() {
        val inputVal = _taxAmountExcl.value.toDoubleOrNull() ?: 0.0
        val breakdown = TaxCalculator.removeTax(inputVal, _taxRate.value)
        _displayText.value = breakdown.originalAmount.toLong().toString()
        saveToHistory(
            expr = "${breakdown.totalAmount} - 消費税抜 (${(breakdown.taxRate * BigDecimal("100")).toInt()}%)",
            resultStr = breakdown.originalAmount.toLong().toString(),
            mode = "Tax"
        )
    }

    // --- Traditional Unit Converter Operations ---
    fun setUnitCategory(category: String) {
        _unitCategory.value = category
    }

    fun setMetricValue(value: String) {
        _metricValue.value = value
    }

    // --- Tatami Planner Operations ---
    fun updateTatamiDimensions(width: Double, length: Double) {
        _tatamiWidth.value = width
        _tatamiLength.value = length
    }

    fun setTatamiRegion(region: TatamiPlanner.Region) {
        _tatamiRegion.value = region
    }

    // --- Practice/Training Mode Logic ---
    fun startPractice() {
        _isPracticeActive.value = true
        _practiceScore.value = 0
        _practiceTotal.value = 0
        _practiceTimeLeft.value = 60
        _practiceInput.value = ""
        _practiceFeedback.value = ""
        generateNewProblem()
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_practiceTimeLeft.value > 0 && _isPracticeActive.value) {
                delay(1000)
                _practiceTimeLeft.value -= 1
            }
            _isPracticeActive.value = false
        }
    }

    fun stopPractice() {
        _isPracticeActive.value = false
        timerJob?.cancel()
    }

    fun submitPracticeAnswer() {
        if (!_isPracticeActive.value) return
        
        val userAnswer = _practiceInput.value.toLongOrNull()
        _practiceTotal.value += 1
        
        if (userAnswer == currentProblemAnswer) {
            _practiceScore.value += 1
            _practiceFeedback.value = "Correct"
        } else {
            _practiceFeedback.value = "Incorrect: $currentProblemAnswer"
        }
        
        // Wait a brief moment, then load next problem
        viewModelScope.launch {
            delay(1200)
            _practiceInput.value = ""
            _practiceFeedback.value = ""
            if (_isPracticeActive.value) {
                generateNewProblem()
            }
        }
    }

    fun updatePracticeInput(input: String) {
        _practiceInput.value = input
    }

    private fun generateNewProblem() {
        // Simple level generator based on standard 1-2 digit additions/subtractions
        val isAddition = Random.nextBoolean()
        val num1 = Random.nextLong(1, 100)
        val num2 = Random.nextLong(1, 100)
        
        if (isAddition) {
            _currentProblemText.value = "$num1 + $num2"
            currentProblemAnswer = num1 + num2
        } else {
            // Ensure positive result for abacus learning
            val large = maxOf(num1, num2)
            val small = minOf(num1, num2)
            _currentProblemText.value = "$large - $small"
            currentProblemAnswer = large - small
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

class ZenViewModelFactory(private val historyDao: HistoryDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ZenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ZenViewModel(historyDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
