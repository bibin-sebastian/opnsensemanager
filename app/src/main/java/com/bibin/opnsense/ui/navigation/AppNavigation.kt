package com.bibin.opnsense.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bibin.opnsense.ui.devices.DeviceListScreen
import com.bibin.opnsense.ui.onboarding.OnboardingScreen
import com.bibin.opnsense.ui.settings.SettingsScreen

sealed class Screen(val route: String, val label: String) {
    data object Onboarding : Screen("onboarding", "Setup")
    data object Devices : Screen("devices", "Devices")
    data object Settings : Screen("settings", "Settings")
}

@Composable
fun AppNavigation(
    navViewModel: NavViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val startDestination = if (navViewModel.credentialManager.isConfigured) {
        Screen.Devices.route
    } else {
        Screen.Onboarding.route
    }

    val bottomItems = listOf(Screen.Devices, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route != Screen.Onboarding.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (screen == Screen.Devices) Icons.Default.DevicesOther
                                                  else Icons.Default.Settings,
                                    contentDescription = screen.label,
                                )
                            },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onSuccess = {
                        navController.navigate(Screen.Devices.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Devices.route) {
                DeviceListScreen(contentPadding = padding)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(contentPadding = padding)
            }
        }
    }
}
