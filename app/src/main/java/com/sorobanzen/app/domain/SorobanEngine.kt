package com.sorobanzen.app.domain

object SorobanEngine {

    /**
     * Converts a Long number to its traditional Japanese Kanji representation.
     * Supports up to 99 京 (Quadrillion), which is 17 digits.
     */
    fun convertToKanji(number: Long): String {
        if (number == 0L) return "零"
        
        val isNegative = number < 0
        val absNum = if (isNegative) -number else number
        
        val units = listOf("", "万", "億", "兆", "京")
        var temp = absNum
        var unitIndex = 0
        var kanjiResult = ""
        
        while (temp > 0) {
            val part = (temp % 10000).toInt()
            if (part > 0) {
                val partKanji = convertPartToKanji(part, unitIndex > 0)
                kanjiResult = partKanji + units[unitIndex] + kanjiResult
            }
            temp /= 10000
            unitIndex++
        }
        
        return if (isNegative) "マイナス $kanjiResult" else kanjiResult
    }

    private fun convertPartToKanji(num: Int, forceIchi: Boolean): String {
        val thousands = num / 1000
        val hundreds = (num % 1000) / 100
        val tens = (num % 100) / 10
        val units = num % 10
        
        var result = ""
        
        if (thousands > 0) {
            result += when (thousands) {
                1 -> "千"
                3 -> "三千" // san-zen
                8 -> "八千" // has-sen
                else -> convertDigit(thousands) + "千"
            }
        }
        
        if (hundreds > 0) {
            result += when (hundreds) {
                1 -> "百"
                3 -> "三百" // san-byaku
                6 -> "六百" // rop-pyaku
                8 -> "八百" // hap-pyaku
                else -> convertDigit(hundreds) + "百"
            }
        }
        
        if (tens > 0) {
            result += when (tens) {
                1 -> "十"
                else -> convertDigit(tens) + "十"
            }
        }
        
        if (units > 0) {
            // For numbers like 10,000 (一万), if forceIchi is true and num is 1, we write "一"
            if (units == 1 && num == 1 && !forceIchi) {
                result += "一"
            } else {
                result += convertDigit(units)
            }
        } else if (num == 1 && forceIchi) {
            result += "一"
        }
        
        return result
    }

    private fun convertDigit(digit: Int): String {
        return when (digit) {
            1 -> "一"
            2 -> "二"
            3 -> "三"
            4 -> "四"
            5 -> "五"
            6 -> "六"
            7 -> "七"
            8 -> "八"
            9 -> "九"
            else -> ""
        }
    }

}
