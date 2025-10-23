package com.example.birthday

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.birthday.data.repo.CumpleRepository
import com.example.birthday.navigation.CumpleNavHost
import com.example.birthday.navigation.Routes
import com.example.birthday.notification.NotificationHelper
import com.example.birthday.ui.theme.CumpleAnaTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

val LocalRepository = staticCompositionLocalOf<CumpleRepository> {
    error("CumpleRepository not provided")
}

class MainActivity : ComponentActivity() {
    private val navigationEvents = MutableSharedFlow<NavigationEvent>(replay = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as CumpleAnaApp).repository
        setContent {
            CumpleAnaTheme {
                CompositionLocalProvider(LocalRepository provides repository) {
                    CumpleApp(navigationEvents)
                }
            }
        }
        handleNavigationIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?) {
        if (intent == null) return
        val activityId = intent.getIntExtra(NotificationHelper.EXTRA_ACTIVITY_ID, -1).takeIf { it > 0 }
        val openTimeline = intent.getBooleanExtra(NotificationHelper.EXTRA_OPEN_TIMELINE, false)
        if (activityId != null || openTimeline) {
            lifecycleScope.launch {
                navigationEvents.emit(NavigationEvent(activityId = activityId, openTimeline = openTimeline))
            }
            intent.removeExtra(NotificationHelper.EXTRA_ACTIVITY_ID)
            intent.removeExtra(NotificationHelper.EXTRA_OPEN_TIMELINE)
        }
    }
}

@Composable
private fun CumpleApp(navigationEvents: SharedFlow<NavigationEvent>) {
    val navController = rememberNavController()
    val currentNavController by rememberUpdatedState(navController)
    LaunchedEffect(navigationEvents) {
        navigationEvents.collect { event ->
            if (event.activityId != null) {
                currentNavController.navigate("${Routes.Activity}/${event.activityId}")
            } else if (event.openTimeline) {
                currentNavController.navigate(Routes.Timeline) {
                    popUpTo(Routes.Timeline) { inclusive = false }
                }
            }
        }
    }
    CumpleNavHost(navController = navController)
}

data class NavigationEvent(val activityId: Int?, val openTimeline: Boolean)
