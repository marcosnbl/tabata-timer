package com.example.tabata_timer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tabata_timer.data.service.TimerService
import com.example.tabata_timer.domain.timer.TimerManager
import com.example.tabata_timer.ui.config.CreateWorkoutScreen
import com.example.tabata_timer.ui.execution.WorkoutExecutionScreen
import com.example.tabata_timer.ui.theme.TabatatimerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var timerManager: TimerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TabatatimerTheme {
                val navController = rememberNavController()
                
                // Request runtime permissions for Android 13+ and FGS types
                val permissionsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { _ -> }

                LaunchedEffect(Unit) {
                    val permissions = mutableListOf<String>()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    
                    val toRequest = permissions.filter {
                        ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
                    }
                    
                    if (toRequest.isNotEmpty()) {
                        permissionsLauncher.launch(toRequest.toTypedArray())
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "config",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("config") {
                            CreateWorkoutScreen(
                                onStartWorkout = { config ->
                                    timerManager.startWorkout(config)
                                    val intent = Intent(this@MainActivity, TimerService::class.java)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        startForegroundService(intent)
                                    } else {
                                        startService(intent)
                                    }
                                    navController.navigate("execution")
                                }
                            )
                        }
                        composable("execution") {
                            WorkoutExecutionScreen(
                                onWorkoutCancelled = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
