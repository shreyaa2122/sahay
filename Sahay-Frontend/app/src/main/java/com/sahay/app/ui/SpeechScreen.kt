package com.sahay.app.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sahay.app.network.ChatGPTService
import org.json.JSONObject

// 1. Data model for the Chat
data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun SpeechScreen(onSpeak: (String) -> Unit) {
    val context = LocalContext.current
    val chatGPT = remember { ChatGPTService(context) }

    // 2. Chat History State
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenText?.firstOrNull() ?: ""

            if (recognizedText.isNotEmpty()) {
                // Add user message to UI
                chatMessages.add(ChatMessage(recognizedText, true))

                chatGPT.askChatGPT(recognizedText) { response ->
                    Handler(Looper.getMainLooper()).post {
                        handleAction(context, response, onSpeak) { aiText ->
                            // Add AI response to UI
                            chatMessages.add(ChatMessage(aiText, false))
                        }
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            }
            speechLauncher.launch(intent)
        }
    }

    Scaffold(
        bottomBar = {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            ) {
                Text("Tap to Speak to Sahay")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Chat List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(chatMessages) { message ->
                    ChatBubble(message)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (message.isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(bubbleColor)
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(text = message.text, color = textColor, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

fun handleAction(context: Context, gptResponse: String, onSpeak: (String) -> Unit, resultCallback: (String) -> Unit) {
    try {
        val obj = JSONObject(gptResponse.trim())
        val action = obj.optString("action", "").lowercase()
        val text = obj.optString("text", "").ifEmpty { obj.optString("reply", "") }

        when {
            action.contains("open") -> {
                val packageId = obj.optString("package_id", "").replace("null", "")
                val opened = if (packageId.isNotEmpty()) openAppByPackage(context, packageId) else false
                val finalMsg = if (opened) text.ifEmpty { "Opening app..." } else "I found the app, but it isn't installed."
                resultCallback(finalMsg)
                onSpeak(finalMsg)
            }

            action == "call" -> {
                val contact = obj.optString("contact", "").replace("null", "")
                if (contact.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$contact")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    val msg = text.ifEmpty { "Calling $contact" }
                    resultCallback(msg)
                    onSpeak(msg)
                }
            }

            action == "set_alarm" || action.contains("reminder") -> {
                val hour = obj.optInt("hour", -1)
                val minute = obj.optInt("minute", 0)
                val label = obj.optString("message", "Medicine Reminder")

                if (hour in 0..23) { // Validate hour
                    try {
                        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                            putExtra(AlarmClock.EXTRA_HOUR, hour)
                            putExtra(AlarmClock.EXTRA_MINUTES, minute)
                            putExtra(AlarmClock.EXTRA_MESSAGE, label)
                            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                        val msg = "Okay, I've set a reminder for $label at ${String.format("%02d:%02d", hour, minute)}."
                        resultCallback(msg)
                        onSpeak(msg)
                    } catch (e: Exception) {
                        val errorMsg = "I couldn't access your alarm clock. Please check permissions."
                        resultCallback(errorMsg)
                        onSpeak(errorMsg)
                    }
                } else {
                    val errorMsg = "I heard the time, but it sounds invalid. Could you repeat that?"
                    resultCallback(errorMsg)
                    onSpeak(errorMsg)
                }
            }

            action == "add_item" -> {
                val item = obj.optString("item", "groceries")
                // Elderly users usually use Blinkit/Grofers
                openAppByPackage(context, "com.grofers.customerapp")
                val msg = "I've added $item to your list on Blinkit. I am opening the app for you."
                resultCallback(msg)
                onSpeak(msg)
            }

            else -> {
                val msg = text.ifEmpty { "I'm here to help! I can open apps, make calls, or set medicine reminders." }
                resultCallback(msg)
                onSpeak(msg)
            }
        }
    } catch (e: Exception) {
        resultCallback(gptResponse)
        onSpeak(gptResponse)
    }
}

fun openAppByPackage(context: Context, packageId: String): Boolean {
    val pm = context.packageManager
    val intent = pm.getLaunchIntentForPackage(packageId)
    return if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    } else false
}