package com.qbtester.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundexTest {

    @Test
    fun `matches classic reference encodings`() {
        assertEquals("R163", Soundex.encode("Robert"))
        assertEquals("R163", Soundex.encode("Rupert"))
        assertEquals("P236", Soundex.encode("Pfister"))
    }

    @Test
    fun `is case insensitive`() {
        assertEquals(Soundex.encode("robert"), Soundex.encode("ROBERT"))
    }

    @Test
    fun `jaxson and jackson encode identically`() {
        assertEquals(Soundex.encode("Jaxson"), Soundex.encode("Jackson"))
        assertTrue(Soundex.soundsLike("Jaxson", "Jackson"))
    }

    @Test
    fun `different leading letters never sound alike`() {
        assertFalse(Soundex.soundsLike("Prescott", "Trescott"))
    }

    @Test
    fun `empty input encodes to empty and never sounds like anything`() {
        assertEquals("", Soundex.encode(""))
        assertFalse(Soundex.soundsLike("", ""))
        assertFalse(Soundex.soundsLike("", "Dart"))
    }
}
