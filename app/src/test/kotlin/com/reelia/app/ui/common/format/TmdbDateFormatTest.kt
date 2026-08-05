package com.reelia.app.ui.common.format

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TmdbDateFormatTest {

    private val today = LocalDate.of(2026, 8, 4)

    @Test
    fun `daysUntilOrNull counts forward from today`() {
        assertEquals(0L, "2026-08-04".daysUntilOrNull(today))
        assertEquals(8L, "2026-08-12".daysUntilOrNull(today))
        assertEquals(30L, "2026-09-03".daysUntilOrNull(today))
    }

    @Test
    fun `daysUntilOrNull is null for a past date`() {
        assertNull("2026-08-03".daysUntilOrNull(today))
    }

    @Test
    fun `daysUntilOrNull is null for a null or unparseable date`() {
        assertNull(null.daysUntilOrNull(today))
        assertNull("not-a-date".daysUntilOrNull(today))
        assertNull("".daysUntilOrNull(today))
    }

    @Test
    fun `isAfterToday is strictly future only`() {
        assertFalse("2026-08-04".isAfterToday(today))
        assertFalse("2026-08-03".isAfterToday(today))
        assertTrue("2026-08-05".isAfterToday(today))
    }

    @Test
    fun `isAfterToday is false for null or unparseable`() {
        assertFalse(null.isAfterToday(today))
        assertFalse("not-a-date".isAfterToday(today))
    }

    @Test
    fun `toYearOrNull extracts the leading 4-digit year`() {
        assertEquals("2026", "2026-08-04".toYearOrNull())
        assertEquals("1955", "1955-01-18".toYearOrNull())
    }

    @Test
    fun `toYearOrNull is null for short or non-numeric input`() {
        assertNull(null.toYearOrNull())
        assertNull("".toYearOrNull())
        assertNull("abc".toYearOrNull())
        assertNull("202".toYearOrNull())
    }
}
