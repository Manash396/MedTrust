package com.mk.medtrust.util


import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UtilObjectTest {

    // 1️⃣ Normal AM
    @Test
    fun timeToMinutes_02_30_AM() {
        val result = UtilObject.timeToMinutes("02:30 AM")
        assertThat(result).isEqualTo(2 * 60 + 30)
    }

    // 2️⃣ Normal PM
    @Test
    fun timeToMinutes_02_30_PM() {
        val result = UtilObject.timeToMinutes("02:30 PM")
        assertThat(result).isEqualTo(14 * 60 + 30)
    }

    // 3️⃣ 12 AM (Midnight)
    @Test
    fun timeToMinutes_12_00_AM() {
        val result = UtilObject.timeToMinutes("12:00 AM")
        assertThat(result).isEqualTo(0)
    }

    // 4️⃣ 12 PM (Noon)
    @Test
    fun timeToMinutes_12_00_PM() {
        val result = UtilObject.timeToMinutes("12:00 PM")
        assertThat(result).isEqualTo(12 * 60)
    }

    // 5️⃣ Edge minute case
    @Test
    fun timeToMinutes_11_59_PM() {
        val result = UtilObject.timeToMinutes("11:59 PM")
        assertThat(result).isEqualTo(23 * 60 + 59)
    }

    // 6️⃣ Invalid format
    @Test
    fun timeToMinutes_invalidFormat_returnsZero() {
        val result = UtilObject.timeToMinutes("25:99 PM")
        assertThat(result).isEqualTo(0)
    }

    // 7️⃣ Empty string
    @Test
    fun timeToMinutes_emptyString_returnsZero() {
        val result = UtilObject.timeToMinutes("")
        assertThat(result).isEqualTo(0)
    }

    // 8️⃣ Completely wrong input
    @Test
    fun timeToMinutes_randomText_returnsZero() {
        val result = UtilObject.timeToMinutes("hello")
        assertThat(result).isEqualTo(1)
    }

    // 9️⃣ Lowercase am/pm
    @Test
    fun timeToMinutes_lowercasePm() {
        val result = UtilObject.timeToMinutes("02:30 pm")
        assertThat(result).isEqualTo(14 * 60 + 30)
    }

    // 🔟 Leading/trailing spaces
    @Test
    fun timeToMinutes_withSpaces() {
        val result = UtilObject.timeToMinutes(" 02:30 PM ")
        assertThat(result).isEqualTo(14 * 60 + 30)
    }
}