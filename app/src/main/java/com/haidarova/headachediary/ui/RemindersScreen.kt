package com.haidarova.headachediary.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.haidarova.headachediary.reminders.ReminderPrefs
import com.haidarova.headachediary.reminders.ReminderScheduler

@Composable
fun RemindersScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Назад", color = Deep) }
        ScreenTitle("Напоминания")
        ReminderItem("day", "Отметка дня", "Каждый день в выбранное время", 21)
        ReminderItem("med", "Лекарство", "Каждый день в выбранное время", 9)
        ReminderItem("water", "Пить воду", "Каждые 2 часа", null)
        Text(
            "Система может сдвигать время уведомлений на несколько минут для экономии заряда.",
            color = Txt,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ReminderItem(kind: String, title: String, subtitle: String, defHour: Int?) {
    val ctx = LocalContext.current
    var on by remember { mutableStateOf(ReminderPrefs.isOn(ctx, kind)) }
    var hour by remember { mutableIntStateOf(if (defHour != null) ReminderPrefs.hour(ctx, kind, defHour) else 0) }

    fun commit(enable: Boolean, h: Int) {
        ReminderPrefs.save(ctx, kind, enable, h)
        if (enable) ReminderScheduler.schedule(ctx, kind, h) else ReminderScheduler.cancel(ctx, kind)
        on = enable
        hour = h
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        commit(granted, hour)
    }

    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Txt)
            }
            Switch(
                checked = on,
                onCheckedChange = { want ->
                    val needPermission = want && Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
                    if (needPermission) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else commit(want, hour)
                }
            )
        }
        if (defHour != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { commit(on, (hour + 23) % 24) }) { Text("−", color = Deep, fontWeight = FontWeight.Bold) }
                Text("%02d:00".format(hour), fontWeight = FontWeight.Bold)
                TextButton(onClick = { commit(on, (hour + 1) % 24) }) { Text("+", color = Deep, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
