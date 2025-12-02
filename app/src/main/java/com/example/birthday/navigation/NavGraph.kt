package com.example.birthday.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.birthday.LocalRepository
import com.example.birthday.gate.TimeGate
import com.example.birthday.ui.screens.*
import com.example.birthday.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object Routes {
    const val Locked = "locked"
    const val Timeline = "timeline"
    const val Activity = "activity"
    const val LockedActivity = "lockedActivity"
    const val Memories = "memories"
    const val FinalCelebration = "finalCelebration"
    const val YearlyGallery = "yearlyGallery"
}

@Composable
fun CumpleNavHost(navController: NavHostController = rememberNavController()) {
    val repository = LocalRepository.current
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val isBirthday = TimeGate.isUnlocked()
        val allCompleted = repository.areAllActivitiesCompleted()

        startDestination = if (allCompleted) {
            Routes.FinalCelebration
        } else if (isBirthday) {
            Routes.Timeline
        } else {
            Routes.Locked
        }
    }

    if (startDestination == null) return

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable(Routes.Locked) {
            val remainingFlow: StateFlow<Long> = rememberCountdownState()
            val remaining by remainingFlow.collectAsState()

            LockedScreen(
                remainingSeconds = remaining,
                onCheckAgain = {
                    if (TimeGate.isUnlocked()) {
                        navController.navigate(Routes.Timeline) {
                            popUpTo(Routes.Locked) { inclusive = true }
                        }
                    }
                },
                onSkip = {
                    TimeGate.forceUnlock()
                    navController.navigate(Routes.Timeline) {
                        popUpTo(Routes.Locked) { inclusive = true }
                    }
                }
            )
        }

        // Timeline: Acepta argumento opcional "review"
        composable(
            route = "${Routes.Timeline}?review={review}",
            arguments = listOf(navArgument("review") { defaultValue = false; type = NavType.BoolType })
        ) { backStackEntry ->
            val isReview = backStackEntry.arguments?.getBoolean("review") ?: false

            TimelineScreen(
                repository = repository,
                onOpenActivity = { id ->
                    navController.navigate("${Routes.Activity}/$id")
                },
                onOpenAlbum = {
                    navController.navigate(Routes.Memories)
                },
                isReviewMode = isReview,
                onNavigateToFinal = {
                    navController.navigate(Routes.FinalCelebration) {
                        popUpTo(Routes.Timeline) { inclusive = true }
                    }
                }
            )
        }

        composable("${Routes.Activity}/{activityId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("activityId")?.toIntOrNull() ?: return@composable
            ActivityDetailScreen(
                activityId = id,
                repository = repository,
                onBack = { navController.popBackStack() },
                onCompleted = {
                    navController.popBackStack()
                }
            )
        }

        composable("${Routes.LockedActivity}/{activityId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("activityId")?.toIntOrNull() ?: return@composable
            LockedActivityScreen(
                activityId = id,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Memories) {
            MemoriesScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FinalCelebration) {
            FinalCelebrationScreen(
                repository = repository,
                onOpenYearGallery = { year ->
                    navController.navigate("${Routes.YearlyGallery}/$year")
                },
                onSeeActivities = {
                    // Navegar al timeline en modo revisión
                    navController.navigate("${Routes.Timeline}?review=true")
                }
            )
        }

        composable(
            route = "${Routes.YearlyGallery}/{year}",
            arguments = listOf(navArgument("year") { type = NavType.IntType })
        ) { backStackEntry ->
            val year = backStackEntry.arguments?.getInt("year") ?: 2025
            YearlyGalleryScreen(
                year = year,
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