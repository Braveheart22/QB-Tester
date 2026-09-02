package com.qbtester.app.domain

/**
 * Classic American Soundex phonetic encoding (a letter + 3 digits), e.g. "Jaxson" and "Jackson"
 * both encode to "J250" because X and CK/S sit in the same phonetic group. Used by
 * [AnswerMatcher] to forgive "sounds right but spelled wrong" guesses without resorting to
 * generic edit-distance fuzzy matching, which could accidentally accept an unrelated name.
 */
object Soundex {

    fun encode(input: String): String {
        val letters = input.uppercase().filter { it in 'A'..'Z' }
        if (letters.isEmpty()) return ""

        fun codeOf(c: Char): Char = when (c) {
            'B', 'F', 'P', 'V' -> '1'
            'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2'
            'D', 'T' -> '3'
            'L' -> '4'
            'M', 'N' -> '5'
            'R' -> '6'
            else -> '0' // vowels, plus H/W/Y
        }

        val result = StringBuilder().append(letters[0])
        var previousCode = codeOf(letters[0])

        for (i in 1 until letters.length) {
            val c = letters[i]
            val code = codeOf(c)
            if (code != '0' && code != previousCode) {
                result.append(code)
            }
            // H and W are transparent to the "same code as previous letter" rule; vowels reset it.
            if (c != 'H' && c != 'W') {
                previousCode = code
            }
        }

        return result.toString().padEnd(4, '0').take(4)
    }

    fun soundsLike(a: String, b: String): Boolean {
        val codeA = encode(a)
        val codeB = encode(b)
        return codeA.isNotEmpty() && codeA == codeB
    }
}
