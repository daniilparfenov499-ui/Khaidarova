package com.haidarova.headachediary.ui

import android.content.Intent
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haidarova.headachediary.data.HeadacheEntry
import com.haidarova.headachediary.data.Stats

@Composable
fun ReportScreen(entries: List<HeadacheEntry>) {
    val ctx = LocalContext.current
    var days by remember { mutableIntStateOf(30) }
    val text = Stats.buildReport(entries, days)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle("Отчёт для врача")
        SectionLabel("Период")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(7 to "7 дней", 30 to "30 дней", 90 to "90 дней").forEach { (d, label) ->
                Chip(label, days == d) { days = d }
            }
        }
        AppCard {
            Text(text)
        }
        Button(
            onClick = {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Дневник головной боли")
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                ctx.startActivity(Intent.createChooser(send, "Поделиться отчётом"))
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Mid, contentColor = Deep)
        ) {
            Text("Поделиться", fontWeight = FontWeight.Bold)
        }
    }
}
