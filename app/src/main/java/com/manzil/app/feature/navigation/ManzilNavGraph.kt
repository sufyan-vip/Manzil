@file:OptIn(ExperimentalMaterial3Api::class)

package com.manzil.app.feature.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.manzil.app.feature.ai.AiCoachScreen
import com.manzil.app.feature.calendar.CalendarScreen
import com.manzil.app.feature.goals.GoalPulseScreen
import com.manzil.app.feature.goals.GoalsScreen
import com.manzil.app.feature.goals.KpiScreen
import com.manzil.app.feature.importer.RoadmapImportScreen
import com.manzil.app.feature.journal.JournalScreen
import com.manzil.app.feature.more.MoreScreen
import com.manzil.app.feature.search.SearchScreen
import com.manzil.app.feature.settings.SettingsScreen
import com.manzil.app.feature.tasks.TasksScreen
import com.manzil.app.feature.time.TimeScreen
import com.manzil.app.feature.today.TodayScreen
import com.manzil.app.feature.tools.ClientHuntScreen
import com.manzil.app.feature.tools.FreeToolsScreen

object Routes {
    const val TODAY = "today"
    const val TASKS = "tasks"
    const val CALENDAR = "calendar"
    const val GOALS = "goals"
    const val MORE = "more"
    const val SEARCH = "search"
    const val AI = "ai"
    const val JOURNAL = "journal"
    const val TIME = "time"
    const val PULSE = "pulse"
    const val KPI = "kpi"
    const val CLIENTS = "clients"
    const val TOOLS = "tools"
    const val IMPORT = "import"
    const val SETTINGS = "settings"
    const val TASKS_NEW = "tasks/new"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.TODAY, "Today", Icons.Filled.Today),
    Tab(Routes.TASKS, "Tasks", Icons.Filled.Checklist),
    Tab(Routes.CALENDAR, "Calendar", Icons.Filled.DateRange),
    Tab(Routes.GOALS, "Goals", Icons.Filled.Flag),
    Tab(Routes.MORE, "More", Icons.Filled.MoreHoriz)
)

@Composable
fun ManzilNavGraph(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun open(route: String) {
        navController.navigate(route) { launchSingleTop = true }
    }

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = Routes.TODAY,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Routes.TODAY) {
                    TodayScreen(
                        onSearch = { open(Routes.SEARCH) },
                        onOpenTasks = { open(Routes.TASKS) },
                        onOpenCalendar = { open(Routes.CALENDAR) },
                        onOpenGoals = { open(Routes.GOALS) },
                        onOpenJournal = { open(Routes.JOURNAL) }
                    )
                }
                composable(Routes.TASKS) {
                    TasksScreen(
                        onSearch = { open(Routes.SEARCH) },
                        onOpenCalendar = { open(Routes.CALENDAR) }
                    )
                }
                composable(Routes.CALENDAR) {
                    CalendarScreen(onSearch = { open(Routes.SEARCH) })
                }
                composable(Routes.GOALS) {
                    GoalsScreen(
                        onSearch = { open(Routes.SEARCH) },
                        onOpenPulse = { open(Routes.PULSE) },
                        onOpenKpi = { open(Routes.KPI) }
                    )
                }
                composable(Routes.MORE) {
                    MoreScreen(onOpen = { target -> open(target) }, onSearch = { open(Routes.SEARCH) })
                }

                composable(Routes.SEARCH) {
                    SearchScreen(
                        onBack = { navController.popBackStack() },
                        onCreateTask = { title ->
                            navController.navigate(
                                "tasks_new?prefill=" + java.net.URLEncoder.encode(title, "UTF-8")
                            )
                        }
                    )
                }
                composable(
                    route = "tasks_new?prefill={prefill}",
                    arguments = listOf(
                        navArgument("prefill") {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) {
                    TasksScreen(
                        onSearch = { open(Routes.SEARCH) },
                        onOpenCalendar = { open(Routes.CALENDAR) },
                        prefillTitle = it.arguments?.getString("prefill").orEmpty()
                    )
                }
                composable(Routes.PULSE) {
                    GoalPulseScreen(
                        onBack = { navController.popBackStack() },
                        onSearch = { open(Routes.SEARCH) }
                    )
                }
                composable(Routes.KPI) {
                    KpiScreen(
                        onBack = { navController.popBackStack() },
                        onSearch = { open(Routes.SEARCH) }
                    )
                }
                composable(Routes.TIME) {
                    TimeScreen(
                        onBack = { navController.popBackStack() },
                        onSearch = { open(Routes.SEARCH) }
                    )
                }
                composable(Routes.JOURNAL) {
                    JournalScreen(
                        onBack = { navController.popBackStack() },
                        onSearch = { open(Routes.SEARCH) }
                    )
                }
                composable(Routes.AI) {
                    AiCoachScreen(
                        onBack = { navController.popBackStack() },
                        onOpenSettings = { open(Routes.SETTINGS) }
                    )
                }
                composable(Routes.CLIENTS) {
                    ClientHuntScreen(
                        onBack = { navController.popBackStack() },
                        onSearch = { open(Routes.SEARCH) }
                    )
                }
                composable(Routes.TOOLS) {
                    FreeToolsScreen(
                        onBack = { navController.popBackStack() },
                        onSearch = { open(Routes.SEARCH) }
                    )
                }
                composable(Routes.IMPORT) {
                    RoadmapImportScreen(
                        onBack = { navController.popBackStack() },
                        onSearch = { open(Routes.SEARCH) }
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
