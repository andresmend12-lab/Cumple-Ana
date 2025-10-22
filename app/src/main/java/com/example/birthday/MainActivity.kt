package com.example.birthday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.compose.rememberNavController
import com.example.birthday.data.repo.CumpleRepository
import com.example.birthday.navigation.CumpleNavHost
import com.example.birthday.ui.theme.CumpleAnaTheme

val LocalRepository = staticCompositionLocalOf<CumpleRepository> {
    error("CumpleRepository not provided")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as CumpleAnaApp).repository
        setContent {
            CumpleAnaTheme {
                CompositionLocalProvider(LocalRepository provides repository) {
                    CumpleApp()
                }
            }
        }
    }
}

@Composable
private fun CumpleApp() {
    val navController = rememberNavController()
    CumpleNavHost(navController = navController)
}
