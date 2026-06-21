package com.glassai.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(working, key = { _, p -> p.name }) { index, p ->
                ProviderCard(
                    provider = p,
                    selected = p.name == currentName,
                    onSelect = { onSelect(p.name) },
                    onChange = { updated ->
                        working = working.toMutableList().also { it[index] = updated }
                    }
                )
            }
            item {
                Button(onClick = { onSave(working) }, modifier = Modifier.fillMaxWidth()) {
                    Text("保存")
                }
            }
        }
    }
}

@Composable
private fun ProviderCard(
    provider: Provider,
    selected: Boolean,
    onSelect: () -> Unit,
    onChange: (Provider) -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "border"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 2.dp else 1.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "width"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selected, onClick = onSelect)
                Text(provider.name, style = MaterialTheme.typography.titleMedium)
            }
            OutlinedTextField(
                value = provider.baseUrl,
                onValueChange = { onChange(provider.copy(baseUrl = it)) },
                label = { Text("Base URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = provider.model,
                onValueChange = { onChange(provider.copy(model = it)) },
                label = { Text("模型名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = provider.apiKey,
                onValueChange = { onChange(provider.copy(apiKey = it)) },
                label = { Text("API Key") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
