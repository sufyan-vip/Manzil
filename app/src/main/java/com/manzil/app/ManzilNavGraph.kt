package com.manzil.app

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.manzil.app.feature.onboarding.OnboardingScreen
import com.manzil.app.feature.today.TodayScreen
import com.manzil.app.feature.calendar.CalendarScreen
import com.manzil.app.feature.goals.GoalsScreen
import com.manzil.app.feature.search.SearchScreen
import com.manzil.app.feature.ai.AiCoachScreen
import com.manzil.app.feature.journal.JournalScreen
import com.manzil.app.feature.time.TimeScreen
import com.manzil.app.feature.settings.SettingsScreen
import com.manzil.app.feature.clienthunt.ClientHuntScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Today : Screen("today", "Today", Icons.Filled.Home)
    object Calendar : Screen("calendar", "Calendar", Icons.Filled.CalendarMonth)
    object Goals : Screen("goals", "Goals", Icons.Filled.Flag)
    object Search : Screen("search", "Search", Icons.Filled.Search)
    object Onboarding : Screen("onboarding", "Onboarding", Icons.Filled.Person)
    object AiCoach : Screen("ai", "AI Coach", Icons.Filled.SmartToy)
    object Journal : Screen("journal", "Journal", Icons.Filled.Book)
    object Time : Screen("time", "Time", Icons.Filled.Timer)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object ClientHunt : Screen("clienthunt", "Clients", Icons.Filled.Groups)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManzilNavGraph() {
    val navController = rememberNavController()
    val bottomTabs = listOf(Screen.Today, Screen.Calendar, Screen.Goals, Screen.Search)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // For M0: check if onboarding needed - for now always show bottom nav after onboarding
    var showOnboarding by remember { mutableStateOf(false) }

    if (showOnboarding) {
        OnboardingScreen(
            onComplete = { showOnboarding = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manzil — BS SE Edition") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { navController.navigate(Screen.AiCoach.route) }) {
                        Icon(Icons.Filled.SmartToy, contentDescription = "AI Coach")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                bottomTabs.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Quick capture */ }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Today.route) { TodayScreen() }
            composable(Screen.Calendar.route) { CalendarScreen() }
            composable(Screen.Goals.route) { GoalsScreen() }
            composable(Screen.Search.route) { SearchScreen() }
            composable(Screen.AiCoach.route) { AiCoachScreen() }
            composable(Screen.Journal.route) { JournalScreen() }
            composable(Screen.Time.route) { TimeScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.ClientHunt.route) { ClientHuntScreen() }
            composable(Screen.Onboarding.route) { 
                OnboardingScreen(onComplete = { 
                    navController.navigate(Screen.Today.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }) 
            }
        }
    }
}
