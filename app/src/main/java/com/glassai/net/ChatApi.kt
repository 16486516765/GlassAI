package com.glassai.net

import com.glassai.data.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class ChatMessage(val role: String, val content: String)

class ChatApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    fun stream(p: Provider, history: List<ChatMessage>): Flow<String> = flow {
        val payload = buildJsonObject {
            put("model", p.model)
            put("temperature", p.temperature)
            put("stream", true)
            putJsonArray("messages") {
                history.forEach {
                    addJsonObject {
                        put("role", it.role)
                        put("content", it.content)
                    }
                }
            }
        }.toString()

        val req = Request.Builder()
            .url(p.baseUrl.trimEnd('/') + "/chat/completions")
            .addHeader("Authorization", "Bearer " + p.apiKey)
            .addHeader("Content-Type", "application/json")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                emit("[错误] HTTP " + resp.code + ": " + resp.body?.string())
                return@flow
            }
            val source = resp.body!!.source()
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (line.startsWith("data:")) {
                    val data = line.removePrefix("data:").trim()
                    if (data == "[DONE]") break
                    runCatching {
                        val delta = json.parseToJsonElement(data)
                            .jsonObject["choices"]!!.jsonArray[0]
                            .jsonObject["delta"]!!.jsonObject["content"]
                            ?.jsonPrimitive?.content
                        if (!delta.isNullOrEmpty()) emit(delta)
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
