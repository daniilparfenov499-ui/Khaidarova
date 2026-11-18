package com.haidarova.headachediary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haidarova.headachediary.data.HeadacheEntry
import com.haidarova.headachediary.data.fmtDateTime
import com.haidarova.headachediary.data.fmtDuration
import com.haidarova.headachediary.data.splitList

@Composable
fun HistoryScreen(entries: List<HeadacheEntry>, onDelete: (HeadacheEntry) -> Unit) {
    var toDelete by remember { mutableStateOf<HeadacheEntry?>(null) }

    if (entries.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            ScreenTitle("История")
            Text("Записей пока нет. Добавьте первый приступ на главном экране.", color = Txt, modifier = Modifier.padding(top = 12.dp))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { ScreenTitle("История") }
            items(entries, key = { it.id }) { e ->
                AppCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            Modifier
                                .padding(top = 5.dp, end = 12.dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(painColor(e.intensity))
                        )
                        Column(Modifier.weight(1f)) {
                            Text(fmtDateTime(e.startTime), fontWeight = FontWeight.Bold)
                            Text("Боль ${e.intensity}/10 · ${fmtDuration(e.durationMin)}", color = Txt)
                            if (e.symptoms.isNotBlank()) Text("Симптомы: ${splitList(e.symptoms).joinToString(", ")}", color = Txt)
                            if (e.triggers.isNotBlank()) Text("Триггеры: ${splitList(e.triggers).joinToString(", ")}", color = Txt)
                            if (e.medication.isNotBlank()) {
                                val res = when (e.medHelped) { true -> " — помогло"; false -> " — не помогло"; null -> "" }
                                Text("Лекарство: ${e.medication}$res", color = Txt)
                            }
                            if (e.note.isNotBlank()) Text(e.note, color = Txt)
                        }
                        TextButton(onClick = { toDelete = e }) { Text("Удалить", color = Accent) }
                    }
                }
            }
        }
    }

    toDelete?.let { e ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Удалить запись?") },
            text = { Text("Приступ от ${fmtDateTime(e.startTime)} будет удалён без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = { onDelete(e); toDelete = null }) { Text("Удалить", color = Accent) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Отмена", color = Txt) } }
        )
    }
}
