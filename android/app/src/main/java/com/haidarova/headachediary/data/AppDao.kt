package com.haidarova.headachediary.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM entries ORDER BY startTime DESC")
    fun entries(): Flow<List<HeadacheEntry>>

    @Insert
    suspend fun insertEntry(entry: HeadacheEntry)

    @Delete
    suspend fun deleteEntry(entry: HeadacheEntry)

    @Query("SELECT * FROM day_logs")
    fun dayLogs(): Flow<List<DayLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(log: DayLog)
}
