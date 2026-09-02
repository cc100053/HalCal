package com.sorobanzen.app.domain

/**
 * Turns the raw strings [CalculatorEngine] keeps into the ones the reader sees: parser symbols
 * become calculator glyphs, binary operators get breathing room, and numbers are grouped in
 * thousands. Nothing here is parsed back — the engine always works from its own raw text.
 */
object DisplayFormat {

    private val binaryOperator = Regex("(?<=[0-9.)])([+\\-*/])")
    private val numberToken = Regex("""\d+(?:\.\d*)?""")

    /**
     * Groups the integer part of every number in thousands: "1234567.5" -> "1,234,567.5".
     * The fractional part is left alone, and a trailing "." survives so grouping does not fight
     * the user mid-entry.
     */
    fun groupDigits(text: String): String = numberToken.replace(text) { match ->
        val dot = match.value.indexOf('.')
        val whole = if (dot < 0) match.value else match.value.substring(0, dot)
        val fraction = if (dot < 0) "" else match.value.substring(dot)
        whole.reversed().chunked(3).joinToString(",").reversed() + fraction
    }

    /** Spaces out binary operators and swaps in the calculator glyphs: "5*3" -> "5 × 3". */
    fun expression(raw: String): String = binaryOperator
        .replace(raw) { " ${it.value} " }
        .replace("*", "×")
        .replace("/", "÷")
        .trim()
        .let(::groupDigits)
}
