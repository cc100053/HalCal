package com.sorobanzen.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sorobanzen.app.data.AppPreferences
import com.sorobanzen.app.data.HistoryDao
import com.sorobanzen.app.data.HistoryEntity
import com.sorobanzen.app.domain.CalculatorEngine
import com.sorobanzen.app.domain.PracticeSession
import com.sorobanzen.app.domain.PracticeSubmission
import com.sorobanzen.app.domain.SorobanEngine
import com.sorobanzen.app.domain.TaxCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.random.Random

enum class PracticePhase {
    READY,
    ACTIVE,
    FINISHED
}

class ZenViewModel(
    private val historyDao: HistoryDao,
    private val preferences: AppPreferences
) : ViewModel() {

    private val calculator = CalculatorEngine()

    // --- Calculator Portrait States ---
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _displayText = MutableStateFlow("0")
    val displayText: StateFlow<String> = _displayText.asStateFlow()

    // --- Soroban Landscape States ---
    private val _rodsCount = MutableStateFlow(preferences.rodsCount)
    val rodsCount: StateFlow<Int> = _rodsCount.asStateFlow()

    private val _rodValues = MutableStateFlow(IntArray(preferences.rodsCount))
    val rodValues: StateFlow<IntArray> = _rodValues.asStateFlow()

    private val _sorobanValue = MutableStateFlow(0L)
    val sorobanValue: StateFlow<Long> = _sorobanValue.asStateFlow()

    // --- Settings States ---
    private val _soundEffectsEnabled = MutableStateFlow(preferences.soundEffectsEnabled)
    val soundEffectsEnabled: StateFlow<Boolean> = _soundEffectsEnabled.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(preferences.hapticsEnabled)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(preferences.ttsEnabled)
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

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

    // --- Practice/Training Mode States ---
    private val _practicePhase = MutableStateFlow(PracticePhase.READY)
    val practicePhase: StateFlow<PracticePhase> = _practicePhase.asStateFlow()

    private val _practiceTimeLeft = MutableStateFlow(60)
    val practiceTimeLeft: StateFlow<Int> = _practiceTimeLeft.asStateFlow()

    private val _practiceScore = MutableStateFlow(0)
    val practiceScore: StateFlow<Int> = _practiceScore.asStateFlow()

    private val _practiceTotal = MutableStateFlow(0)
    val practiceTotal: StateFlow<Int> = _practiceTotal.asStateFlow()

    private val _currentProblemText = MutableStateFlow("")
    val currentProblemText: StateFlow<String> = _currentProblemText.asStateFlow()

    private val _practiceInput = MutableStateFlow("")
    val practiceInput: StateFlow<String> = _practiceInput.asStateFlow()

    private val _practiceFeedback = MutableStateFlow<PracticeSubmission?>(null)
    val practiceFeedback: StateFlow<PracticeSubmission?> = _practiceFeedback.asStateFlow()

    private val _isPracticeAnswerLocked = MutableStateFlow(false)
    val isPracticeAnswerLocked: StateFlow<Boolean> = _isPracticeAnswerLocked.asStateFlow()

    private var timerJob: Job? = null
    private var nextProblemJob: Job? = null
    private val practiceSession = PracticeSession()

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
        val state = calculator.press(key)
        _expression.value = state.expression
        _displayText.value = state.display

        state.completedExpression?.let { completedExpression ->
            state.display.toLongOrNull()?.let { result ->
                if (result in 0 until 10_000_000_000_000_000L) {
                    speakJapaneseNumber(result)
                }
            }
            saveToHistory(completedExpression, state.display, "通常計算")
        }
    }

    fun loadCalculatorResult(result: String) {
        val state = calculator.loadResult(result)
        _expression.value = state.expression
        _displayText.value = state.display
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
        _rodValues.value = IntArray(_rodsCount.value)
        _sorobanValue.value = 0L
    }

    fun restoreSoroban(values: IntArray) {
        if (values.size != _rodsCount.value) return
        _rodValues.value = values.copyOf().also { rods ->
            rods.indices.forEach { index -> rods[index] = rods[index].coerceIn(0, 9) }
        }
        updateSorobanValueFromRods()
    }

    fun loadNumberToSoroban(num: Long) {
        val count = _rodsCount.value
        val rods = IntArray(count)
        var temp = num.coerceAtLeast(0L)
        for (i in count - 1 downTo 0) {
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
    fun setRodsCount(count: Int) {
        val validatedCount = count.coerceIn(7, 17)
        _rodsCount.value = validatedCount
        preferences.rodsCount = validatedCount
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        _soundEffectsEnabled.value = enabled
        preferences.soundEffectsEnabled = enabled
    }

    fun setHapticEnabled(enabled: Boolean) {
        _hapticEnabled.value = enabled
        preferences.hapticsEnabled = enabled
    }

    fun setTtsEnabled(enabled: Boolean) {
        _ttsEnabled.value = enabled
        preferences.ttsEnabled = enabled
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
    }

    fun setMetricValue(value: String) {
        _metricValue.value = value
    }

    // --- Practice/Training Mode Logic ---
    fun startPractice() {
        nextProblemJob?.cancel()
        _practicePhase.value = PracticePhase.ACTIVE
        _practiceScore.value = 0
        _practiceTotal.value = 0
        _practiceTimeLeft.value = 60
        _practiceInput.value = ""
        _practiceFeedback.value = null
        _isPracticeAnswerLocked.value = false
        val firstProblem = createProblem()
        practiceSession.start(firstProblem.first, firstProblem.second)
        _currentProblemText.value = firstProblem.first
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_practiceTimeLeft.value > 0) {
                delay(1000)
                if (_practicePhase.value != PracticePhase.ACTIVE) return@launch
                _practiceTimeLeft.value = (_practiceTimeLeft.value - 1).coerceAtLeast(0)
            }
            finishPractice()
        }
    }

    fun stopPractice() {
        if (_practicePhase.value != PracticePhase.ACTIVE) return
        timerJob?.cancel()
        finishPractice()
    }

    fun submitPracticeAnswer() {
        val submission = practiceSession.submit(_practiceInput.value) ?: return
        _practiceScore.value = practiceSession.progress.score
        _practiceTotal.value = practiceSession.progress.total
        _isPracticeAnswerLocked.value = true

        _practiceFeedback.value = submission

        nextProblemJob?.cancel()
        nextProblemJob = viewModelScope.launch {
            delay(1200)
            _practiceInput.value = ""
            _practiceFeedback.value = null
            if (_practicePhase.value == PracticePhase.ACTIVE) {
                val nextProblem = createProblem()
                practiceSession.advance(nextProblem.first, nextProblem.second)
                _currentProblemText.value = nextProblem.first
                _isPracticeAnswerLocked.value = false
            }
        }
    }

    fun updatePracticeInput(input: String) {
        if (
            !_isPracticeAnswerLocked.value &&
            input.length <= MAX_PRACTICE_INPUT_LENGTH &&
            input.all(Char::isDigit)
        ) {
            _practiceInput.value = input
        }
    }

    private fun finishPractice() {
        _practicePhase.value = PracticePhase.FINISHED
        _isPracticeAnswerLocked.value = false
        _practiceInput.value = ""
        _practiceFeedback.value = null
        nextProblemJob?.cancel()
        practiceSession.stop()
    }

    private fun createProblem(): Pair<String, Long> {
        // Simple level generator based on standard 1-2 digit additions/subtractions
        val isAddition = Random.nextBoolean()
        val num1 = Random.nextLong(1, 100)
        val num2 = Random.nextLong(1, 100)

        return if (isAddition) {
            "$num1 + $num2" to (num1 + num2)
        } else {
            // Ensure positive result for abacus learning
            val large = maxOf(num1, num2)
            val small = minOf(num1, num2)
            "$large - $small" to (large - small)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        nextProblemJob?.cancel()
    }

    private companion object {
        const val MAX_PRACTICE_INPUT_LENGTH = 3
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
