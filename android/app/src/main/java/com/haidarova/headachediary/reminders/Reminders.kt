package com.haidarova.headachediary.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.haidarova.headachediary.MainActivity
import com.haidarova.headachediary.R
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object ReminderPrefs {
    private fun prefs(c: Context) = c.getSharedPreferences("reminders", Context.MODE_PRIVATE)
    fun isOn(c: Context, kind: String) = prefs(c).getBoolean("${kind}_on", false)
    fun hour(c: Context, kind: String, def: Int) = prefs(c).getInt("${kind}_hour", def)
    fun save(c: Context, kind: String, on: Boolean, hour: Int) {
        prefs(c).edit().putBoolean("${kind}_on", on).putInt("${kind}_hour", hour).apply()
    }
}

object ReminderScheduler {
    private fun delayUntil(hour: Int): Long {
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(hour, 0)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return java.time.Duration.between(now, target).toMillis()
    }

    fun schedule(ctx: Context, kind: String, hour: Int) {
        val builder = if (kind == "water") {
            PeriodicWorkRequestBuilder<ReminderWorker>(2, TimeUnit.HOURS)
        } else {
            PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delayUntil(hour), TimeUnit.MILLISECONDS)
        }
        val request = builder.setInputData(workDataOf("kind" to kind)).build()
        WorkManager.getInstance(ctx)
            .enqueueUniquePeriodicWork("reminder_$kind", ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel(ctx: Context, kind: String) {
        WorkManager.getInstance(ctx).cancelUniqueWork("reminder_$kind")
    }
}

class ReminderWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        val kind = inputData.getString("kind") ?: "day"
        val (title, text) = when (kind) {
            "med" -> "Время лекарства" to "Если врач назначил приём — не забудьте принять."
            "water" -> "Выпейте воды" to "Обезвоживание — частый триггер головной боли."
            else -> "Отметьте день" to "Как сегодня сон и самочувствие? Это займёт 10 секунд."
        }
        Notifier.show(applicationContext, kind.hashCode(), title, text)
        return Result.success()
    }
}

object Notifier {
    private const val CHANNEL = "reminders"

    fun show(ctx: Context, id: Int, title: String, text: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Напоминания", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val pi = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        nm.notify(id, notification)
    }
}
