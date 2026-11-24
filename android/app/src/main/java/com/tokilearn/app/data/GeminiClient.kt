package com.tokilearn.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Gemini 直连客户端：OkHttp 直接请求 Google Gemini API，不经过任何服务器。
 * Key 由调用方从本地 DataStore 传入，绝不写进代码或日志。
 */
object GeminiClient {
    private val http = OkHttpClient()
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    suspend fun ask(apiKey: String, model: String, subjectContext: String, question: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(apiKey.isNotBlank()) { "请先在“我的”里填写 Gemini Key" }
                val modelName = model.ifBlank { "gemini-2.0-flash" }
                val body = JSONObject().apply {
                    put(
                        "contents",
                        org.json.JSONArray().put(
                            JSONObject().put(
                                "parts",
                                org.json.JSONArray().put(
                                    JSONObject().put("text", "$subjectContext\n\n问题：$question"),
                                ),
                            ),
                        ),
                    )
                }.toString()
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey")
                    .post(body.toRequestBody(jsonMedia))
                    .build()
                http.newCall(request).execute().use { response ->
                    val text = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val message = runCatching { JSONObject(text).optString("error") }.getOrNull().orEmpty()
                        error("Gemini 请求失败（${response.code}）$message")
                    }
                    JSONObject(text)
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                }
            }
        }
}
