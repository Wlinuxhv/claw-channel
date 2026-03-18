package com.clawchannel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.clawchannel.app.ui.chat.ChatScreen
import com.clawchannel.app.ui.login.LoginScreen
import com.clawchannel.app.ui.theme.LobsterRed
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClawChannelTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }
    
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "chat" else "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    isLoggedIn = true
                    navController.navigate("chat") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("chat") {
            ChatScreen(
                onLogout = {
                    isLoggedIn = false
                    navController.navigate("login") {
                        popUpTo("chat") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun ClawChannelTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = LobsterRed,
            secondary = LobsterRed.copy(alpha = 0.8f),
            tertiary = LobsterRed.copy(alpha = 0.6f)
        )
    } else {
        lightColorScheme(
            primary = LobsterRed,
            secondary = LobsterRed.copy(alpha = 0.8f),
            tertiary = LobsterRed.copy(alpha = 0.6f)
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
