package com.phantom.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.phantom.player.ui.screens.LibraryScreen
import com.phantom.player.ui.screens.PlayerScreen
import com.phantom.player.ui.screens.EqScreen
import com.phantom.player.ui.theme.PhantomBlack
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhantomPlayerApp()
        }
    }
}

@Composable
fun PhantomPlayerApp() {
    val navController = rememberNavController()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PhantomBlack
    ) {
        NavHost(
            navController = navController,
            startDestination = "library"
        ) {
            composable("library") {
                LibraryScreen()
            }
            
            composable("player") {
                PlayerScreen()
            }
            
            composable("eq") {
                EqScreen()
            }
        }
    }
}
