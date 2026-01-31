package com.winlator.downloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.winlator.downloader.data.AppConfig
import com.winlator.downloader.data.SupabaseClient
import com.winlator.downloader.data.SupabaseService
import com.winlator.downloader.ui.screens.MainScreen
import com.winlator.downloader.ui.screens.SetupScreen
import com.winlator.downloader.ui.screens.isSetupComplete
import com.winlator.downloader.ui.theme.WinlatorDownloaderTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WinlatorDownloaderTheme {
                var showMainScreen by remember { mutableStateOf(isSetupComplete(this@MainActivity)) }
                var startupConfig by remember { mutableStateOf<AppConfig?>(null) }
                var showStartupDialog by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    try {
                        val service = Retrofit.Builder()
                            .baseUrl(SupabaseClient.URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                            .create(SupabaseService::class.java)

                        val config = service.getAppConfig(SupabaseClient.API_KEY, SupabaseClient.AUTH).firstOrNull()
                        if (config?.showDialog == true) {
                            startupConfig = config
                            showStartupDialog = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showMainScreen) {
                        MainScreen()
                    } else {
                        SetupScreen(onComplete = {
                            showMainScreen = true
                        })
                    }

                    if (showStartupDialog && startupConfig != null) {
                        AlertDialog(
                            onDismissRequest = { showStartupDialog = false },
                            title = { Text(startupConfig!!.dialogTitle) },
                            text = { Text(startupConfig!!.dialogMessage) },
                            confirmButton = {
                                Button(onClick = { showStartupDialog = false }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
