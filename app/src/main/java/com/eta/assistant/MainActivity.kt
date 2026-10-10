package com.eta.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.eta.assistant.core.theme.EtaTheme
import com.eta.assistant.ui.navigation.EtaNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as EtaApplication
        val preferencesRepository = app.preferencesRepository

        setContent {
            val prefs by preferencesRepository.userPreferencesFlow.collectAsState(initial = null)
            val isDark = when (prefs?.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            EtaTheme(darkTheme = isDark) {
                EtaNavHost()
            }
        }
    }
}

