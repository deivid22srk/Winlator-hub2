package com.winlator.downloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.winlator.downloader.data.AppConfig
import com.winlator.downloader.data.SupabaseClient
import com.winlator.downloader.data.SupabaseService
import com.winlator.downloader.ui.screens.MainScreen
import com.winlator.downloader.ui.screens.SetupScreen
import com.winlator.downloader.ui.screens.isSetupComplete
import com.winlator.downloader.ui.screens.downloadFile
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
                        if (config != null && config.showDialog == true) {
                            val currentVersionCode = packageManager.getPackageInfo(packageName, 0).versionCode
                            val latestVersion = config.latestVersion ?: 0
                            if (config.isUpdate == true) {
                                if (currentVersionCode < latestVersion) {
                                    startupConfig = config
                                    showStartupDialog = true
                                }
                            } else {
                                startupConfig = config
                                showStartupDialog = true
                            }
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
                        val config = startupConfig!!
                        val context = LocalContext.current
                        AlertDialog(
                            onDismissRequest = { showStartupDialog = false },
                            title = { Text(config.dialogTitle ?: "") },
                            text = { Text(config.dialogMessage ?: "") },
                            confirmButton = {
                                if (config.isUpdate == true) {
                                    Button(
                                        onClick = {
                                            val url = config.updateUrl
                                            if (!url.isNullOrBlank()) {
                                                downloadFile(context, url, "WinlatorHub_Update.apk")
                                            }
                                            showStartupDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Baixar Atualização")
                                    }
                                } else {
                                    Button(onClick = { showStartupDialog = false }) {
                                        Text("OK")
                                    }
                                }
                            },
                            dismissButton = {
                                if (config.isUpdate == true) {
                                    TextButton(onClick = { showStartupDialog = false }) {
                                        Text("Depois")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
