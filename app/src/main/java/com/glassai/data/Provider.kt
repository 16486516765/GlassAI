package com.glassai.data

import kotlinx.serialization.Serializable

@Serializable
data class Provider(
    val name: String,
    val baseUrl: String,
    val apiKey: String = "",
    val model: String,
    val temperature: Float = 0.7f
)

object Presets {
    val builtin = listOf(
        Provider("OpenAI",   "https://api.openai.com/v1",                         model = "gpt-4o-mini"),
        Provider("DeepSeek", "https://api.deepseek.com/v1",                       model = "deepseek-chat"),
        Provider("Moonshot", "https://api.moonshot.cn/v1",                        model = "moonshot-v1-8k"),
        Provider("Qwen",     "https://dashscope.aliyuncs.com/compatible-mode/v1", model = "qwen-plus"),
        Provider("Ollama",   "http://10.0.2.2:11434/v1",                          model = "llama3.1")
    )
}
