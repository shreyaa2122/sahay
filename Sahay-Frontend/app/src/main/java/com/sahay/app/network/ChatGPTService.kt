package com.sahay.app.network

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * Service to communicate with the Sahay Backend (Node.js/Groq).
 * Handles voice command processing, history retrieval, and analytics.
 */
class ChatGPTService(private val context: Context) {
    private val client = OkHttpClient()

    // Base URL for the Sahay API.
    private val baseUrl = "http://192.168.29.227:5000/api/v1/commands"

    // --- STATIC APP LIST (Synced with Backend) ---
    private val staticApps = listOf(
        AppInfo("whatsapp", "com.whatsapp", listOf("chat", "message")),
        AppInfo("blinkit", "com.grofers.customerapp", listOf("grocery", "blinkit", "grofers")),
        AppInfo("youtube", "com.google.android.youtube", listOf("video", "watch", "yt")),
        AppInfo("instagram", "com.instagram.android", listOf("insta", "reels", "photos")),
        AppInfo("chrome", "com.android.chrome", listOf("browser", "google", "search")),
        AppInfo("zomato", "com.application.zomato", listOf("food", "order", "delivery")),
        AppInfo("camera", "com.android.camera", listOf("photo", "selfie")),
        AppInfo("gallery", "com.google.android.apps.photos", listOf("album", "screenshot", "videos"))
    )

    data class AppInfo(val name: String, val packageId: String, val keywords: List<String>)

    /**
     * Sends the user's voice transcript to the backend to get an AI-parsed command.
     * Includes local fetch logic for faster responses on common app commands.
     */
    fun askChatGPT(question: String, callback: (String) -> Unit) {
        val lowerQuestion = question.lowercase()

        // 1. LOCAL FETCH LOGIC: Check if it's a simple "open app" command we know
        val localResponse = checkLocalCommands(lowerQuestion)
        if (localResponse != null) {
            callback(localResponse)
            return
        }

        // 2. BACKEND FETCH: If not handled locally, ask the AI brain
        try {
            val json = JSONObject()
            json.put("question", question)
            val requestBody = json.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$baseUrl/process")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback("{\"action\":\"reply\", \"text\":\"Connection failed. Using offline fallback.\"}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        callback(body)
                    } else {
                        callback("{\"action\":\"reply\", \"text\":\"Brain error: ${response.code}\"}")
                    }
                }
            })
        } catch (e: Exception) {
            callback("{\"action\":\"reply\", \"text\":\"Error: ${e.message}\"}")
        }
    }

    /**
     * Checks if the question contains "open" or "launch" followed by an app name or keyword.
     */
    private fun checkLocalCommands(question: String): String? {
        if (!question.contains("open") && !question.contains("launch")) return null

        for (app in staticApps) {
            val matchesName = question.contains(app.name)
            val matchesKeyword = app.keywords.any { question.contains(it) }

            if (matchesName || matchesKeyword) {
                return JSONObject().apply {
                    put("action", "open_app")
                    put("package_id", app.packageId)
                    put("text", "Opening ${app.name.replaceFirstChar { it.uppercase() }}...")
                }.toString()
            }
        }
        return null
    }

    /**
     * Fetches the history of commands from MongoDB.
     */
    fun getHistory(search: String? = null, callback: (String) -> Unit) {
        val url = if (search != null) "$baseUrl/history?search=$search" else "$baseUrl/history"
        
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("[]")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                callback(body ?: "[]")
            }
        })
    }

    /**
     * Fetches usage statistics (top apps, total commands).
     */
    fun getStats(callback: (String) -> Unit) {
        val request = Request.Builder()
            .url("$baseUrl/stats")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("{}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                callback(body ?: "{}")
            }
        })
    }
}
