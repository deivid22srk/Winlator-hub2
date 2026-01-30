package com.winlator.downloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.winlator.downloader.ui.screens.MainScreen
import com.winlator.downloader.ui.screens.SetupScreen
import com.winlator.downloader.ui.screens.isSetupComplete
import com.winlator.downloader.ui.theme.WinlatorDownloaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WinlatorDownloaderTheme {
                var showMainScreen by remember { mutableStateOf(isSetupComplete(this@MainActivity)) }

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
                }
            }
        }
    }
}
