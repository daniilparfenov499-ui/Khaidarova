package com.haidarova.headachediary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.haidarova.headachediary.data.AppDatabase
import com.haidarova.headachediary.data.DayLog
import com.haidarova.headachediary.data.HeadacheEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).dao()

    val entries: StateFlow<List<HeadacheEntry>> =
        dao.entries().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dayLogs: StateFlow<List<DayLog>> =
        dao.dayLogs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(entry: HeadacheEntry) {
        viewModelScope.launch { dao.insertEntry(entry) }
    }

    fun deleteEntry(entry: HeadacheEntry) {
        viewModelScope.launch { dao.deleteEntry(entry) }
    }

    fun saveDay(log: DayLog) {
        viewModelScope.launch { dao.upsertDay(log) }
    }
}
