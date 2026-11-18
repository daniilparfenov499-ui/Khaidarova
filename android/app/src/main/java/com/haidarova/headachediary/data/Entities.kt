package com.haidarova.headachediary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Один приступ головной боли. symptoms и triggers — списки через запятую. */
@Entity(tableName = "entries")
data class HeadacheEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val durationMin: Int,
    val intensity: Int,
    val symptoms: String,
    val triggers: String,
    val medication: String,
    val medHelped: Boolean?,
    val note: String
)

/** Ежедневная отметка: сон и стресс по шкале 1–5, кофе, вода. */
@Entity(tableName = "day_logs")
data class DayLog(
    @PrimaryKey val epochDay: Long,
    val sleep: Int,
    val stress: Int,
    val coffee: Boolean,
    val water: Boolean
)
