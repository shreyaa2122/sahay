package com.sahay.app.network

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ChatGPTService(private val context: Context) {
    private val client = OkHttpClient()

    // --- IMPORTANT ---
    // This flag lets you switch between the REAL API and the FAKE one.
    // Set to 'false' to use the fake service for free testing.
    // Set to 'true' when you have API credits and want to use the real OpenAI.
    private val useRealApi = false

    fun askChatGPT(question: String, callback: (String) -> Unit) {
        if (useRealApi) {
            // --- REAL API LOGIC (Costs Money) ---
            // This will only run if you set useRealApi = true
            val apiKey = "YOUR_API_KEY_HERE" // Make sure your key is here
            val prompt = createPrompt(question)

            val requestBody = """
                {
                    "model": "gpt-3.5-turbo",
                    "messages": [{"role": "user", "content": "$prompt"}],
                    "temperature": 0.7
                }
            """.trimIndent().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback("API call failed: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        // Extract message content from the real response
                        val jsonObject = org.json.JSONObject(body)
                        val choices = jsonObject.getJSONArray("choices")
                        if (choices.length() > 0) {
                            val message = choices.getJSONObject(0).getJSONObject("message")
                            val content = message.getString("content")
                            callback(content)
                        } else {
                            callback("No response from AI.")
                        }
                    } else {
                        callback("API Error: ${response.code}\n$body")
                    }
                }
            })

        } else {
            // --- FAKE API LOGIC (Free) ---
            // This runs instantly and costs nothing.
            // It simulates the real API's responses.
            getFakeResponse(question, callback)
        }
    }

    private fun getFakeResponse(question: String, callback: (String) -> Unit) {
        val lowerCaseQuestion = question.lowercase()
        // Simulate a small delay to feel more realistic
        Thread.sleep(500)

        // Check for keywords to return a specific command
        val fakeJson = when {
            "open whatsapp" in lowerCaseQuestion -> """{"action":"open_app", "app_name":"whatsapp"}"""
            "open youtube" in lowerCaseQuestion -> """{"action":"open_app", "app_name":"youtube"}"""
            "call mammi" in lowerCaseQuestion -> """{"action":"call", "contact":"7004070977"}""" // Use a real number to test
            "my name is" in lowerCaseQuestion -> """{"action":"reply", "text":"Nice to meet you!"}"""
            "open chrome" in lowerCaseQuestion -> """{"action":"open_app", "app_name":"chrome"}"""
            "open instagram" in lowerCaseQuestion -> """{"action":"open_app", "app_name":"instagram"}"""
            // Make sure this line looks EXACTLY like this:
            "open grofers" in lowerCaseQuestion || "open blinkit" in lowerCaseQuestion -> """{"action":"open_app", "app_name":"blinkit"}"""


            else -> """{"action":"reply", "text":"I'm a test version. I can only open WhatsApp,chrome, blinkit,instagram ,YouTube, and call Mom."}"""
        }
        callback(fakeJson)
    }

    private fun createPrompt(question: String): String {
        // Your existing prompt logic
        return """
            From now on, you are Sahay, a voice assistant. Your goal is to understand user commands and respond ONLY in JSON format. Do not add any extra text.

            Here are the possible actions:
            - "reply": For simple conversation. JSON: {"action":"reply", "text":"your response"}
            - "open_app": To open an app. JSON: {"action":"open_app", "app_name":"app name"}
            - "call": To make a call. JSON: {"action":"call", "contact":"contact name or number"}
            - "add_item": To add to a list (like groceries). JSON: {"action":"add_item", "items":[{"name":"item name", "quantity":"quantity"}]}

            User's command: "$question"
            JSON response:
        """.trimIndent()
    }
}
