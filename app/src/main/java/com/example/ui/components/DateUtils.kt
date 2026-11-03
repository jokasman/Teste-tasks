package com.example.ui.components

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utilitário para formatação de datas em português europeu.
 */
object DateUtils {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("pt", "PT"))
    private val dayMonthFormat = SimpleDateFormat("d 'de' MMM, HH:mm", Locale("pt", "PT"))

    fun formatRelativeDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val itemTime = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isToday = now.get(Calendar.YEAR) == itemTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == itemTime.get(Calendar.DAY_OF_YEAR)

        val isYesterday = now.get(Calendar.YEAR) == itemTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - itemTime.get(Calendar.DAY_OF_YEAR) == 1

        val timeStr = timeFormat.format(Date(timestamp))

        return when {
            isToday -> "Hoje às $timeStr"
            isYesterday -> "Ontem às $timeStr"
            else -> dayMonthFormat.format(Date(timestamp))
        }
    }
}
