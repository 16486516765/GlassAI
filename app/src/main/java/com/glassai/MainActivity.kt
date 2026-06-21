package com.glassai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glassai.ui.ChatScreen
import com.glassai.ui.ChatViewModel
import com.glassai.ui.SettingsScreen
import com.glassai.ui.theme.GlassAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            GlassAITheme {
                val vm: ChatViewModel = viewModel()
                val messages by vm.messages.collectAsStateWithLifecycle()
                val busy by vm.busy.collectAsStateWithLifecycle()
                val providers by vm.providers.collectAsStateWithLifecycle(initialValue = emptyList())
                val current by vm.currentName.collectAsStateWithLifecycle(initialValue = "")

                var showSettings by remember { mutableStateOf(false) }

                if (showSettings) {
                    SettingsScreen(
                        providers = providers,
                        currentName = current,
                        onSelect = { vm.selectProvider(it) },
                        onSave = { vm.saveProviders(it) },
                        onBack = { showSettings = false }
                    )
                } else {
                    ChatScreen(
                        messages = messages,
                        busy = busy,
                        currentName = current,
                        onSend = { vm.send(it) },
                        onClear = { vm.clear() },
                        onOpenSettings = { showSettings = true }
                    )
                }
            }
        }
    }
}
