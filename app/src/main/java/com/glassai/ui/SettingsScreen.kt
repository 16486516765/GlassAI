package com.glassai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.glassai.data.Provider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    providers: List<Provider>,
    currentName: String,
    onSelect: (String) -> Unit,
    onSave: (List<Provider>) -> Unit,
    onBack: () -> Unit
) {
    var working by remember(providers) { mutableStateOf(providers) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置 · Providers") },
                navigationIcon = { TextButton(onClick = onBack) { Text("返回") } }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            working.forEachIndexed { i, p ->
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = p.name == currentName,
                                onClick = { onSelect(p.name) }
                            )
                            Text(p.name, style = MaterialTheme.typography.titleMedium)
                        }
                        OutlinedTextField(
                            value = p.baseUrl,
                            onValueChange = { v -> working = working.toMutableList().also { it[i] = p.copy(baseUrl = v) } },
                            label = { Text("Base URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = p.model,
                            onValueChange = { v -> working = working.toMutableList().also { it[i] = p.copy(model = v) } },
                            label = { Text("模型名") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = p.apiKey,
                            onValueChange = { v -> working = working.toMutableList().also { it[i] = p.copy(apiKey = v) } },
                            label = { Text("API Key") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Button(onClick = { onSave(working) }, modifier = Modifier.fillMaxWidth()) {
                Text("保存")
            }
        }
    }
}
