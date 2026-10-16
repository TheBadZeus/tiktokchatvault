package com.chatvault.tiktokarchive.ui.components

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object TimeFormat {
    private fun zone(): ZoneId = ZoneId.systemDefault()
    private fun zoned(ts: Long): ZonedDateTime = Instant.ofEpochMilli(ts).atZone(zone())
    private fun date(ts: Long): LocalDate = zoned(ts).toLocalDate()

    fun sameDay(a: Long, b: Long): Boolean = date(a) == date(b)

    fun clock(ts: Long): String = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(zoned(ts))

    fun inboxTime(ts: Long): String {
        val d = date(ts)
        val today = LocalDate.now(zone())
        return when {
            d == today -> clock(ts)
            d == today.minusDays(1) -> "Yesterday"
            d.year == today.year -> DateTimeFormatter.ofPattern("MMM d").format(d)
            else -> DateTimeFormatter.ofPattern("MMM d, yyyy").format(d)
        }
    }

    fun dayLabel(ts: Long): String {
        val d = date(ts)
        val today = LocalDate.now(zone())
        return when {
            d == today -> "Today"
            d == today.minusDays(1) -> "Yesterday"
            d.year == today.year -> DateTimeFormatter.ofPattern("EEE, MMM d").format(d)
            else -> DateTimeFormatter.ofPattern("MMM d, yyyy").format(d)
        }
    }

    /** Local time with zone name, plus the exact UTC instant, for the evidence screen. */
    fun full(ts: Long): String =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(zoned(ts)) +
            " (${zone().id})\n" + Instant.ofEpochMilli(ts).toString()

    fun compact(ts: Long): String = DateTimeFormatter.ofPattern("MMM d, HH:mm").format(zoned(ts))
}

fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
    else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
}
