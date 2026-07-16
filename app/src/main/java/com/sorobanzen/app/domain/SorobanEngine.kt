package com.sorobanzen.app.domain

import kotlin.math.pow

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

    /**
     * Converts a Long number to its Romaji reading.
     */
    fun convertToRomaji(number: Long): String {
        if (number == 0L) return "zero"
        
        val isNegative = number < 0
        val absNum = if (isNegative) -number else number
        
        val units = listOf("", "man", "oku", "chō", "kei")
        var temp = absNum
        var unitIndex = 0
        val parts = mutableListOf<String>()
        
        while (temp > 0) {
            val part = (temp % 10000).toInt()
            if (part > 0) {
                val partRomaji = convertPartToRomaji(part, unitIndex > 0)
                val unitLabel = units[unitIndex]
                if (unitLabel.isNotEmpty()) {
                    parts.add(0, "$partRomaji $unitLabel")
                } else {
                    parts.add(0, partRomaji)
                }
            }
            temp /= 10000
            unitIndex++
        }
        
        val romajiResult = parts.joinToString(" ")
        return if (isNegative) "mainasu $romajiResult" else romajiResult
    }

    private fun convertPartToRomaji(num: Int, forceIchi: Boolean): String {
        val thousands = num / 1000
        val hundreds = (num % 1000) / 100
        val tens = (num % 100) / 10
        val units = num % 10
        
        val parts = mutableListOf<String>()
        
        if (thousands > 0) {
            val thWord = when (thousands) {
                1 -> "sen"
                3 -> "sanzen"
                8 -> "hassen"
                else -> convertDigitRomaji(thousands) + "sen"
            }
            parts.add(thWord)
        }
        
        if (hundreds > 0) {
            val hWord = when (hundreds) {
                1 -> "hyaku"
                3 -> "sanbyaku"
                6 -> "roppyaku"
                8 -> "happyaku"
                else -> convertDigitRomaji(hundreds) + "hyaku"
            }
            parts.add(hWord)
        }
        
        if (tens > 0) {
            val tWord = when (tens) {
                1 -> "jū"
                else -> convertDigitRomaji(tens) + "jū"
            }
            parts.add(tWord)
        }
        
        if (units > 0) {
            if (!(units == 1 && num == 1 && !forceIchi)) {
                parts.add(convertDigitRomaji(units))
            } else {
                parts.add("ichi")
            }
        } else if (num == 1 && forceIchi) {
            parts.add("ichi")
        }
        
        return parts.joinToString(" ")
    }

    private fun convertDigitRomaji(digit: Int): String {
        return when (digit) {
            1 -> "ichi"
            2 -> "ni"
            3 -> "san"
            4 -> "yon"
            5 -> "go"
            6 -> "roku"
            7 -> "nana"
            8 -> "hachi"
            9 -> "kyū"
            else -> ""
        }
    }
}
