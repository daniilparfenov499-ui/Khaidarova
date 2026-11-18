package com.haidarova.headachediary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haidarova.headachediary.data.DayLog
import com.haidarova.headachediary.data.HeadacheEntry
import com.haidarova.headachediary.data.fmtDateTime
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun HomeScreen(
    entries: List<HeadacheEntry>,
    logs: List<DayLog>,
    onNew: () -> Unit,
    onSaveDay: (DayLog) -> Unit,
    onReminders: () -> Unit
) {
    var showDay by remember { mutableStateOf(false) }
    val last = entries.firstOrNull()
    val weekAgo = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L
    val week = entries.count { it.startTime >= weekAgo }
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 6 -> "Доброй ночи"
        hour < 12 -> "Доброе утро"
        hour < 18 -> "Добрый день"
        else -> "Добрый вечер"
    }
    val today = LocalDate.now().toEpochDay()
    val todayDone = logs.any { it.epochDay == today }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle(greeting)
        Button(
            onClick = onNew,
            modifier = Modifier.fillMaxWidth().height(72.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Mid, contentColor = Deep)
        ) {
            Text("+ Записать приступ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        AppCard {
            SectionLabel("Последний приступ")
            if (last == null) {
                Text("Приступов пока нет — это хорошо!", fontWeight = FontWeight.Bold)
            } else {
                Text("${fmtDateTime(last.startTime)} · боль ${last.intensity}/10", fontWeight = FontWeight.Bold)
            }
        }
        AppCard {
            SectionLabel("За последние 7 дней")
            Text(
                when (week) {
                    0 -> "Приступов не было"
                    1 -> "1 приступ"
                    in 2..4 -> "$week приступа"
                    else -> "$week приступов"
                },
                fontWeight = FontWeight.Bold
            )
        }
        AppCard(container = Pale) {
            SectionLabel("Ежедневная отметка")
            Text(
                if (todayDone) "Сегодня уже отмечено. Можно обновить."
                else "Отметьте сон и стресс — так проще найти триггеры."
            )
            TextButton(onClick = { showDay = true }) { Text("Отметить день", color = Deep, fontWeight = FontWeight.Bold) }
        }
        TextButton(onClick = onReminders) { Text("Напоминания →", color = Deep) }
    }

    if (showDay) {
        DayDialog(
            initial = logs.firstOrNull { it.epochDay == today },
            onDismiss = { showDay = false },
            onSave = { onSaveDay(it); showDay = false }
        )
    }
}

@Composable
private fun DayDialog(initial: DayLog?, onDismiss: () -> Unit, onSave: (DayLog) -> Unit) {
    var sleep by remember { mutableIntStateOf(initial?.sleep ?: 3) }
    var stress by remember { mutableIntStateOf(initial?.stress ?: 3) }
    var coffee by remember { mutableStateOf(initial?.coffee ?: false) }
    var water by remember { mutableStateOf(initial?.water ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Как прошёл день?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Сон (1 — плохо, 5 — отлично)")
                RatingRow(sleep) { sleep = it }
                SectionLabel("Стресс (1 — спокойно, 5 — сильный)")
                RatingRow(stress) { stress = it }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Много кофе", Modifier.weight(1f))
                    Switch(checked = coffee, onCheckedChange = { coffee = it })
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Достаточно воды", Modifier.weight(1f))
                    Switch(checked = water, onCheckedChange = { water = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(DayLog(LocalDate.now().toEpochDay(), sleep, stress, coffee, water))
            }) { Text("Сохранить", color = Deep, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = Txt) } }
    )
}
