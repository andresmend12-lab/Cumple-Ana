package com.example.birthday.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.birthday.LocalRepository
import com.example.birthday.gate.TimeGate
import com.example.birthday.ui.screens.ActivityDetailScreen
import com.example.birthday.ui.screens.GenerateVideoScreen
import com.example.birthday.ui.screens.LockedScreen
import com.example.birthday.ui.screens.MemoriesScreen
import com.example.birthday.ui.screens.TimelineScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

object Routes {
    const val Locked = "locked"
    const val Timeline = "timeline"
    const val Activity = "activity"
    const val GenerateVideo = "generateVideo"
    const val Memories = "memories"
}

@Composable
fun CumpleNavHost(navController: NavHostController = rememberNavController()) {
    val startDestination = if (TimeGate.isUnlocked()) Routes.Timeline else Routes.Locked

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Locked) {
            val remainingFlow: StateFlow<Long> = rememberCountdownState()
            val remaining by remainingFlow.collectAsState()
            val navigateToTimeline: () -> Unit = {
                navController.navigate(Routes.Timeline) {
                    popUpTo(Routes.Locked) { inclusive = true }
                }
            }
            LockedScreen(
                remainingSeconds = remaining,
                onCheckAgain = {
                    if (TimeGate.isUnlocked()) {
                        navigateToTimeline()
                    }
                },
                onSkip = {
                    TimeGate.targetDate = LocalDateTime.now(TimeGate.zone).minusSeconds(1)
                    navigateToTimeline()
                }
            )
            LaunchedEffect(remaining) {
                if (remaining <= 0 && TimeGate.isUnlocked()) {
                    navigateToTimeline()
                }
            }
        }
        composable(Routes.Timeline) {
            val repository = LocalRepository.current
            TimelineScreen(
                repository = repository,
                onOpenActivity = { id ->
                    navController.navigate("${Routes.Activity}/$id")
                },
                onShowMemories = {
                    navController.navigate(Routes.Memories)
                }
            )
        }
        composable("${Routes.Activity}/{activityId}") { backStackEntry ->
            val repository = LocalRepository.current
            val id = backStackEntry.arguments?.getString("activityId")?.toIntOrNull() ?: return@composable
            ActivityDetailScreen(
                activityId = id,
                repository = repository,
                onBack = { navController.popBackStack() },
                onCompleted = { isFinal ->
                    navController.popBackStack()
                    if (isFinal) {
                        navController.navigate(Routes.GenerateVideo)
                    }
                }
            )
        }
        composable(Routes.GenerateVideo) {
            val repository = LocalRepository.current
            GenerateVideoScreen(
                repository = repository,
                onFinished = { navController.navigate(Routes.Memories) { popUpTo(Routes.Timeline) { inclusive = false } } },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Memories) {
            val repository = LocalRepository.current
            MemoriesScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun rememberCountdownState(): StateFlow<Long> {
    val state = remember { MutableStateFlow(0L) }
    LaunchedEffect(Unit) {
        TimeGate.countdownFlow().collect { duration ->
            state.value = duration.seconds
        }
    }
    return state
}
