package com.haidarova.headachediary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haidarova.headachediary.data.DayLog
import com.haidarova.headachediary.data.HeadacheEntry
import com.haidarova.headachediary.data.Stats
import com.haidarova.headachediary.data.fmt1
import com.haidarova.headachediary.data.fmtDuration

@Composable
fun StatsScreen(entries: List<HeadacheEntry>, logs: List<DayLog>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle("Статистика")
        if (entries.isEmpty()) {
            Text("Данных пока нет. Статистика появится после первых записей.", color = Txt)
            return@Column
        }

        val (labels, values) = Stats.weeklyCounts(entries)
        AppCard {
            SectionLabel("Приступов по неделям")
            BarChart(values, labels, Modifier.padding(top = 8.dp))
        }

        AppCard {
            SectionLabel("В среднем")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(Stats.avgIntensity(entries)?.let { fmt1(it) } ?: "—", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Deep)
                    Text("сила боли", color = Txt)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(Stats.avgDuration(entries)?.let { fmtDuration(it) } ?: "—", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Deep)
                    Text("длительность", color = Txt)
                }
            }
        }

        val top = Stats.topTriggers(entries)
        AppCard {
            SectionLabel("Частые триггеры")
            if (top.isEmpty()) {
                Text("Отмечайте возможные триггеры в записях приступов.", color = Txt)
            } else {
                val max = top.first().second.toFloat()
                top.forEach { (name, count) ->
                    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(name, modifier = Modifier.width(110.dp))
                        LinearProgressIndicator(
                            progress = { count / max },
                            modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp)),
                            color = Mid,
                            trackColor = Pale
                        )
                        Text("  $count", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        val (sleepAttack, sleepOther) = Stats.sleepCompare(entries, logs)
        AppCard(container = Pale) {
            SectionLabel("Сон и приступы")
            if (sleepAttack != null && sleepOther != null) {
                Text("Качество сна (1–5): в дни приступов ${fmt1(sleepAttack)}, в остальные дни ${fmt1(sleepOther)}.")
            } else {
                Text("Отмечайте день на главном экране — появится сравнение сна в дни с приступами и без.", color = Txt)
            }
        }

        val eff = Stats.medEffect(entries)
        AppCard {
            SectionLabel("Эффективность лекарств")
            Text(if (eff != null) "Помогло в $eff% случаев" else "Пока нет отметок «помогло / не помогло».", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BarChart(values: List<Int>, labels: List<String>, modifier: Modifier = Modifier) {
    val max = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    Row(
        modifier = modifier.fillMaxWidth().height(170.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { i, v ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(v.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Box(
                    Modifier
                        .width(26.dp)
                        .height((110f * v / max).dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(if (v == max && v > 0) Accent else Mid)
                )
                Text(labels[i], fontSize = 10.sp, color = Txt)
            }
        }
    }
}
