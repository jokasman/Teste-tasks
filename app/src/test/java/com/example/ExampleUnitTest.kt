package com.example

import com.example.ui.components.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
    @Test
    fun testDateFormattingToday() {
        val now = System.currentTimeMillis()
        val formatted = DateUtils.formatRelativeDate(now)
        assertTrue(formatted.startsWith("Hoje às"))
    }

    @Test
    fun testDateFormattingYesterday() {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val formatted = DateUtils.formatRelativeDate(cal.timeInMillis)
        assertTrue(formatted.startsWith("Ontem às"))
    }
}
