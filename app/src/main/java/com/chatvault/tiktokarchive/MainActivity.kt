package com.chatvault.tiktokarchive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chatvault.tiktokarchive.domain.model.ThemeMode
import com.chatvault.tiktokarchive.navigation.AppNavGraph
import com.chatvault.tiktokarchive.ui.theme.ChatVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as ChatVaultApp).container
        setContent {
            val mode by container.settings.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            ChatVaultTheme(mode) { AppNavGraph(container) }
        }
    }
}
