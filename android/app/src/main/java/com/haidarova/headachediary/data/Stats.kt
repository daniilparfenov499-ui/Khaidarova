package com.haidarova.headachediary.data

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private val RU: Locale = Locale.forLanguageTag("ru")
private val DATE_TIME = DateTimeFormatter.ofPattern("d MMM, HH:mm", RU)
private val DAY_MONTH = DateTimeFormatter.ofPattern("d.MM", RU)
private const val DAY_MS = 24 * 60 * 60 * 1000L

fun fmtDateTime(ms: Long): String =
    Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(DATE_TIME)

fun fmtDuration(min: Int): String = when {
    min < 60 -> "$min мин"
    min % 60 == 0 -> "${min / 60} ч"
    else -> "${min / 60} ч ${min % 60} мин"
}

fun entryDay(ms: Long): LocalDate =
    Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()

fun splitList(s: String): List<String> = s.split(",").map { it.trim() }.filter { it.isNotEmpty() }

fun fmt1(v: Double): String = String.format(RU, "%.1f", v)

object Stats {
    /** Количество приступов по неделям: подписи (понедельник недели) и значения, от старых к новым. */
    fun weeklyCounts(entries: List<HeadacheEntry>, weeks: Int = 6): Pair<List<String>, List<Int>> {
        val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val labels = mutableListOf<String>()
        val values = mutableListOf<Int>()
        for (k in weeks - 1 downTo 0) {
            val start = monday.minusWeeks(k.toLong())
            val end = start.plusDays(6)
            labels.add(start.format(DAY_MONTH))
            values.add(entries.count {
                val d = entryDay(it.startTime)
                !d.isBefore(start) && !d.isAfter(end)
            })
        }
        return labels to values
    }

    fun topTriggers(entries: List<HeadacheEntry>, limit: Int = 5): List<Pair<String, Int>> =
        entries.flatMap { splitList(it.triggers) }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .take(limit).map { it.key to it.value }

    fun avgIntensity(entries: List<HeadacheEntry>): Double? =
        if (entries.isEmpty()) null else entries.map { it.intensity }.average()

    fun avgDuration(entries: List<HeadacheEntry>): Int? =
        if (entries.isEmpty()) null else entries.map { it.durationMin }.average().toInt()

    /** Доля случаев (в %), когда лекарство помогло, среди записей с отметкой. */
    fun medEffect(entries: List<HeadacheEntry>): Int? {
        val rated = entries.filter { it.medHelped != null }
        if (rated.isEmpty()) return null
        return rated.count { it.medHelped == true } * 100 / rated.size
    }

    /** Среднее качество сна в дни с приступами и в остальные дни. */
    fun sleepCompare(entries: List<HeadacheEntry>, logs: List<DayLog>): Pair<Double?, Double?> {
        val attackDays = entries.map { entryDay(it.startTime).toEpochDay() }.toSet()
        val a = logs.filter { it.epochDay in attackDays }.map { it.sleep }
        val b = logs.filter { it.epochDay !in attackDays }.map { it.sleep }
        return (if (a.isEmpty()) null else a.average()) to (if (b.isEmpty()) null else b.average())
    }

    fun inLastDays(entries: List<HeadacheEntry>, days: Int): List<HeadacheEntry> {
        val cutoff = System.currentTimeMillis() - days * DAY_MS
        return entries.filter { it.startTime >= cutoff }
    }

    fun buildReport(entries: List<HeadacheEntry>, days: Int): String {
        val list = inLastDays(entries, days).sortedBy { it.startTime }
        val sb = StringBuilder()
        sb.appendLine("Дневник головной боли — отчёт за $days дн.")
        sb.appendLine()
        sb.appendLine("Приступов: ${list.size}")
        sb.appendLine("Дней с приступами: ${list.map { entryDay(it.startTime) }.distinct().size}")
        avgIntensity(list)?.let { sb.appendLine("Средняя сила боли: ${fmt1(it)} из 10") }
        avgDuration(list)?.let { sb.appendLine("Средняя длительность: ${fmtDuration(it)}") }
        val tr = topTriggers(list)
        if (tr.isNotEmpty()) sb.appendLine("Частые триггеры: " + tr.joinToString { "${it.first} (${it.second})" })
        medEffect(list)?.let { sb.appendLine("Лекарство помогло в $it% случаев") }
        if (list.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Записи:")
            list.forEach {
                val extra = buildList {
                    if (it.symptoms.isNotBlank()) add(splitList(it.symptoms).joinToString(", "))
                    if (it.medication.isNotBlank()) add("лекарство: ${it.medication}")
                }.joinToString("; ")
                val tail = if (extra.isNotEmpty()) " · $extra" else ""
                sb.appendLine("— ${fmtDateTime(it.startTime)} · боль ${it.intensity}/10 · ${fmtDuration(it.durationMin)}$tail")
            }
        }
        return sb.toString().trimEnd()
    }
}
