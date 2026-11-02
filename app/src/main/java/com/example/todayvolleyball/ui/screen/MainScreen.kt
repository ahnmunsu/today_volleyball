package com.example.todayvolleyball.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    var isSplashVisible by remember { mutableStateOf(true) }

    if (isSplashVisible) {
        SplashScreen(onTimeout = { isSplashVisible = false })
    } else {
        val navController = rememberNavController()
        val unreadChanges by viewModel.unreadChanges.collectAsState()

        if (unreadChanges.isNotEmpty()) {
            ChangeLogDialog(
                changes = unreadChanges,
                onDismiss = { viewModel.markChangesAsRead(unreadChanges) }
            )
        }

        Scaffold { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "calendar",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("calendar") {
                    CalendarScreen(
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToDetail = { matchId -> navController.navigate("detail/$matchId") }
                    )
                }
                composable("settings") {
                    SettingsScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable("detail/{matchId}") { backStackEntry ->
                    val matchId = backStackEntry.arguments?.getString("matchId")?.toIntOrNull() ?: 0
                    MatchDetailScreen(
                        matchId = matchId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
