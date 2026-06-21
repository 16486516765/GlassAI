package com.glassai.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.glassai.data.HistoryMessage
import com.glassai.data.Provider
import com.glassai.data.SettingsRepository
import com.glassai.net.ChatApi
import com.glassai.net.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class UiMessage(val role: String, val content: String, val streaming: Boolean = false)

private const val RESPONSE_TIMEOUT_MS = 30_000L

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository(app)
    private val api = ChatApi()

    private val _messages = MutableStateFlow<List<UiMessage>>(emptyList())
    val messages: StateFlow<List<UiMessage>> = _messages

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    val providers = repo.providers
    val currentName = repo.currentName

    init {
        viewModelScope.launch {
            val saved = repo.history.first()
            if (saved.isNotEmpty()) {
                _messages.value = saved.map { UiMessage(it.role, it.content) }
            }
        }
    }

    fun saveProviders(list: List<Provider>) = viewModelScope.launch { repo.saveProviders(list) }
    fun selectProvider(name: String) = viewModelScope.launch { repo.selectProvider(name) }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _busy.value) return

        _messages.value = _messages.value + UiMessage("user", trimmed)
        _messages.value = _messages.value + UiMessage("assistant", "", streaming = true)
        _busy.value = true
        persistHistory()

        viewModelScope.launch {
            val list = repo.providers.first()
            val current = repo.currentName.first()
            val provider: Provider = list.firstOrNull { it.name == current } ?: list.first()

            if (provider.apiKey.isBlank() && !provider.baseUrl.contains("10.0.2.2")) {
                updateLast("[提示] 请先到设置页填写 " + provider.name + " 的 API Key。", false)
                _busy.value = false
                persistHistory()
                return@launch
            }

            val history = _messages.value
                .filter { !(it.role == "assistant" && it.streaming && it.content.isEmpty()) }
                .map { ChatMessage(it.role, it.content) }

            val sb = StringBuilder()
            val completed = withTimeoutOrNull(RESPONSE_TIMEOUT_MS) {
                runCatching {
                    api.stream(provider, history).collect { delta ->
                        sb.append(delta)
                        updateLast(sb.toString(), true)
                    }
                }.onFailure { e ->
                    sb.append("\n[网络错误] " + e.message)
                }
                true
            }

            if (completed == null && sb.isEmpty()) {
                sb.append("[无响应] 请求超时，请检查网络或 API 配置后重试。")
            } else if (completed == null) {
                sb.append("\n[超时] 响应可能未完整接收。")
            }

            updateLast(sb.toString(), false)
            _busy.value = false
            persistHistory()
        }
    }

    private fun updateLast(content: String, streaming: Boolean) {
        val l = _messages.value.toMutableList()
        val idx = l.indexOfLast { it.role == "assistant" }
        if (idx >= 0) {
            l[idx] = l[idx].copy(content = content, streaming = streaming)
            _messages.value = l
        }
    }

    private fun persistHistory() {
        viewModelScope.launch {
            repo.saveHistory(_messages.value.map { HistoryMessage(it.role, it.content) })
        }
    }

    fun clear() {
        _messages.value = emptyList()
        persistHistory()
    }
}
