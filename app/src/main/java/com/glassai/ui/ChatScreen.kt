package com.glassai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

private val BgColor = Color(0xFF212121)
private val AssistantBg = Color(0xFF2A2A2A)
private val AccentColor = Color(0xFF10A37F)
private val InputBg = Color(0xFF2F2F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<UiMessage>,
    busy: Boolean,
    currentName: String,
    onSend: (String) -> Unit,
    onClear: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor),
                title = { Text(currentName, color = Color.White) },
                actions = {
                    IconButton(onClick = onClear) {
                        Text("清空", color = Color.White.copy(alpha = 0.8f))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "设置", tint = Color.White)
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .imePadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages, key = { it.hashCode() }) { msg -> MessageRow(msg) }
            }

            InputBar(busy = busy, onSend = onSend)
        }
    }
}

@Composable
private fun MessageRow(msg: UiMessage) {
    val isUser = msg.role == "user"
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (isUser) Color.Transparent else AssistantBg)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = msg.content.ifEmpty { "…" },
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun InputBar(
    busy: Boolean,
    onSend: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }

    Row(
        Modifier
            .fillMaxWidth()
            .background(BgColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp)),
            placeholder = { Text("发送消息…", color = Color.White.copy(alpha = 0.5f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = InputBg,
                unfocusedContainerColor = InputBg,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            maxLines = 4
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = {
                if (input.isNotBlank() && !busy) {
                    onSend(input)
                    input = ""
                }
            },
            enabled = !busy,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = AccentColor)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "发送")
        }
    }
}
