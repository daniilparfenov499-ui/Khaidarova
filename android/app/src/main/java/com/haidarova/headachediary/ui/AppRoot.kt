package com.haidarova.headachediary.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.haidarova.headachediary.MainViewModel

private data class Tab(val route: String, val label: String, val icon: String)

private val tabs = listOf(
    Tab("home", "Главная", "🏠"),
    Tab("history", "История", "📋"),
    Tab("stats", "Статистика", "📊"),
    Tab("report", "Отчёт", "📄")
)

@Composable
fun AppRoot(vm: MainViewModel = viewModel()) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val entries by vm.entries.collectAsStateWithLifecycle()
    val logs by vm.dayLogs.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            if (tabs.any { it.route == route }) {
                NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(tab.icon) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Mint)
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") {
                HomeScreen(
                    entries = entries,
                    logs = logs,
                    onNew = { nav.navigate("new") },
                    onSaveDay = { vm.saveDay(it) },
                    onReminders = { nav.navigate("reminders") }
                )
            }
            composable("new") {
                NewEntryScreen(
                    onBack = { nav.popBackStack() },
                    onSave = { vm.addEntry(it); nav.popBackStack() }
                )
            }
            composable("history") { HistoryScreen(entries, onDelete = { vm.deleteEntry(it) }) }
            composable("stats") { StatsScreen(entries, logs) }
            composable("report") { ReportScreen(entries) }
            composable("reminders") { RemindersScreen(onBack = { nav.popBackStack() }) }
        }
    }
}
