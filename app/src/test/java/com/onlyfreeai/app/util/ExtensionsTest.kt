package com.onlyfreeai.app.util

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date

class ExtensionsTest {

    @Test
    fun testStringSanitize_stripsScriptTags() {
        val unsafeString = "<script>alert('hack')</script>Hello World"
        val expected = "Hello World"
        assertEquals(expected, unsafeString.sanitize())
    }

    @Test
    fun testStringSanitize_stripsHtmlTags() {
        val htmlString = "<b>Bold text</b> and <i>italic</i>"
        val expected = "Bold text and italic"
        assertEquals(expected, htmlString.sanitize())
    }

    @Test
    fun testStringSanitize_trimsWhitespace() {
        val whitespaceString = "   Hello World   "
        val expected = "Hello World"
        assertEquals(expected, whitespaceString.sanitize())
    }

    @Test
    fun testStringSanitize_limitsLength() {
        val longString = "a".repeat(350)
        val expected = "a".repeat(Constants.MAX_DESCRIPTION_LENGTH)
        assertEquals(expected, longString.sanitize())
        assertEquals(Constants.MAX_DESCRIPTION_LENGTH, longString.sanitize().length)
    }

    @Test
    fun testStringIsValidUrl_validHttps() {
        val url = "https://onlyfree.ai"
        assertTrue(url.isValidUrl())
    }

    @Test
    fun testStringIsValidUrl_validHttp() {
        val url = "http://onlyfree.ai"
        assertTrue(url.isValidUrl())
    }

    @Test
    fun testStringIsValidUrl_invalidProtocol() {
        val url = "ftp://onlyfree.ai"
        assertFalse(url.isValidUrl())
    }

    @Test
    fun testStringIsValidUrl_noProtocol() {
        val url = "onlyfree.ai"
        assertFalse(url.isValidUrl())
    }

    @Test
    fun testTimestampToRelativeTime_justNow() {
        val now = Timestamp(Date())
        assertEquals("Just now", now.toRelativeTime())
    }

    @Test
    fun testTimestampToRelativeTime_minutesAgo() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, -5)
        val fiveMinutesAgo = Timestamp(cal.time)
        assertEquals("5m ago", fiveMinutesAgo.toRelativeTime())
    }

    @Test
    fun testTimestampToRelativeTime_hoursAgo() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR, -3)
        val threeHoursAgo = Timestamp(cal.time)
        assertEquals("3h ago", threeHoursAgo.toRelativeTime())
    }

    @Test
    fun testTimestampToRelativeTime_daysAgo() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -4)
        val fourDaysAgo = Timestamp(cal.time)
        assertEquals("4d ago", fourDaysAgo.toRelativeTime())
    }
}
