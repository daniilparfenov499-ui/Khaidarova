package com.haidarova.headachediary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haidarova.headachediary.data.HeadacheEntry

private val SYMPTOMS = listOf("Тошнота", "Светобоязнь", "Звукобоязнь", "Аура", "Головокружение")
private val TRIGGERS = listOf("Недосып", "Стресс", "Еда", "Кофе", "Погода", "Нагрузка", "Алкоголь", "Экран")
private val DURATIONS = listOf("30 мин" to 30, "1 ч" to 60, "2 ч" to 120, "4 ч" to 240, "8+ ч" to 480)
private val WHEN = listOf("Сейчас" to 0L, "1 час назад" to 60L, "3 часа назад" to 180L, "Вчера" to 24 * 60L)

private fun SnapshotStateList<String>.toggle(item: String) {
    if (!remove(item)) add(item)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewEntryScreen(onBack: () -> Unit, onSave: (HeadacheEntry) -> Unit) {
    var intensity by remember { mutableIntStateOf(5) }
    var duration by remember { mutableIntStateOf(60) }
    var whenOffset by remember { mutableStateOf(0L) }
    val symptoms = remember { mutableStateListOf<String>() }
    val triggers = remember { mutableStateListOf<String>() }
    var medication by remember { mutableStateOf("") }
    var helped by remember { mutableStateOf<Boolean?>(null) }
    var note by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Назад", color = Deep) }
        }
        ScreenTitle("Новый приступ")

        SectionLabel("Когда начался")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WHEN.forEach { (label, offset) -> Chip(label, whenOffset == offset) { whenOffset = offset } }
        }

        SectionLabel("Сила боли: $intensity из 10")
        PainScale(intensity) { intensity = it }

        SectionLabel("Длительность")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DURATIONS.forEach { (label, min) -> Chip(label, duration == min) { duration = min } }
        }

        SectionLabel("Симптомы")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SYMPTOMS.forEach { s -> Chip(s, s in symptoms) { symptoms.toggle(s) } }
        }

        SectionLabel("Возможные триггеры")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TRIGGERS.forEach { t -> Chip(t, t in triggers) { triggers.toggle(t) } }
        }

        SectionLabel("Лекарство")
        OutlinedTextField(
            value = medication,
            onValueChange = { medication = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Название и доза (необязательно)") },
            singleLine = true
        )
        if (medication.isNotBlank()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("Помогло", helped == true) { helped = if (helped == true) null else true }
                Chip("Не помогло", helped == false) { helped = if (helped == false) null else false }
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Заметка") }
        )

        Button(
            onClick = {
                onSave(
                    HeadacheEntry(
                        startTime = System.currentTimeMillis() - whenOffset * 60_000L,
                        durationMin = duration,
                        intensity = intensity,
                        symptoms = symptoms.joinToString(","),
                        triggers = triggers.joinToString(","),
                        medication = medication.trim(),
                        medHelped = if (medication.isBlank()) null else helped,
                        note = note.trim()
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Deep, contentColor = androidx.compose.ui.graphics.Color.White)
        ) {
            Text("Сохранить", fontWeight = FontWeight.Bold)
        }
    }
}
