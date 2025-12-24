package com.sahay.app.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.sahay.app.network.ChatGPTService
import com.sahay.app.ui.theme.NeumorphicCard
import com.sahay.app.ui.theme.NeumorphicCircleButton
import com.sahay.app.ui.theme.NeumorphicGradientText
import org.json.JSONObject
import kotlin.random.Random

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun SpeechScreen(onSpeak: (String) -> Unit) {
    val context = LocalContext.current
    val chatGPT = remember { ChatGPTService(context) }

    var userQuery by remember { mutableStateOf("👋 Tap the mic and speak") }
    var aiResponse by remember { mutableStateOf("I'm your AI assistant ready to help!") }
    var isListening by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    val chatHistory = remember { mutableStateListOf<ChatMessage>() }

    val micRotation by animateFloatAsState(
        targetValue = if (isListening) 360f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "micRotation"
    )

    val micScale by animateFloatAsState(
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "micScale"
    )

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val query = spokenText?.firstOrNull() ?: "I didn't catch that. Try again?"
            userQuery = query

            chatHistory.add(ChatMessage(
                id = Random.nextLong().toString(),
                text = query,
                isUser = true
            ))

            isProcessing = true

            chatGPT.askChatGPT(query) { response ->
                isProcessing = false
                handleAction(context, response, onSpeak) { messageForScreen ->
                    aiResponse = messageForScreen
                    chatHistory.add(ChatMessage(
                        id = Random.nextLong().toString(),
                        text = messageForScreen,
                        isUser = false
                    ))
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            isListening = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            }
            speechLauncher.launch(intent)
        } else {
            userQuery = "🎤 Microphone permission required"
            aiResponse = "Please enable microphone in settings to use voice features."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (MaterialTheme.colorScheme.background == com.sahay.app.ui.theme.NeumorphicDarkBackground) {
                        listOf(
                            Color(0xFF1E2128),
                            Color(0xFF252A34)
                        )
                    } else {
                        listOf(
                            com.sahay.app.ui.theme.NeumorphicLightBackground,
                            Color(0xFFE8EDF5)
                        )
                    }
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            NeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                elevation = 6.dp,
                cornerRadius = 25.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NeumorphicGradientText(
                        text = "SAHAY AI",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Your Intelligent Voice Assistant",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(25.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SpeechBubble(
                        text = if (isListening) "🎤 Listening..." else userQuery,
                        isUser = true,
                        isLoading = isListening
                    )

                    SpeechBubble(
                        text = if (isProcessing) "🤔 Processing..." else aiResponse,
                        isUser = false,
                        isLoading = isProcessing
                    )
                }

                if (chatHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Recent Conversations",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(chatHistory.reversed()) { message ->
                            ChatHistoryItem(message = message)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            QuickActionsRow(
                onMicClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                onSettingsClick = { /* Open settings */ },
                onHistoryClick = { /* Show history */ },
                onInfoClick = { /* Show info */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(bottom = 32.dp)
            ) {
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    )
                }

                NeumorphicCircleButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    size = 140.dp,
                    elevation = if (isListening) 4.dp else 12.dp,
                    backgroundColor = if (isListening) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    isPressed = isListening,
                    modifier = Modifier
                        .rotate(micRotation)
                        .size((140.dp * micScale))
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(60.dp)
                        )
                    } else {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = "Start Speaking",
                            modifier = Modifier.size(70.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpeechBubble(text: String, isUser: Boolean, isLoading: Boolean) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        elevation = 4.dp,
        backgroundColor = if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
            } else {
                Icon(
                    imageVector = if (isUser) Icons.Filled.Mic else Icons.Filled.VolumeUp,
                    contentDescription = if (isUser) "User query" else "AI response",
                    tint = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ChatHistoryItem(message: ChatMessage) {
    NeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        elevation = 2.dp,
        cornerRadius = 16.dp,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (message.isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (message.isUser) "U" else "AI",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (message.isUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message.text.take(40) + if (message.text.length > 40) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@Composable
fun QuickActionsRow(
    onMicClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val actions = listOf(
        ActionItem(Icons.Outlined.History, "History", onHistoryClick),
        ActionItem(Icons.Filled.Mic, "Quick Speak", onMicClick),
        ActionItem(Icons.Outlined.Settings, "Settings", onSettingsClick),
        ActionItem(Icons.Outlined.Info, "Help", onInfoClick)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        actions.forEach { action ->
            QuickActionButton(action)
        }
    }
}

data class ActionItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun QuickActionButton(action: ActionItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { action.onClick() }
    ) {
        NeumorphicCard(
            modifier = Modifier.size(56.dp),
            elevation = 4.dp,
            cornerRadius = 16.dp,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    action.icon,
                    contentDescription = action.label,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

fun handleAction(
    context: Context,
    gptResponse: String,
    onSpeak: (String) -> Unit,
    resultCallback: (String) -> Unit
) {
    try {
        val obj = JSONObject(gptResponse)
        val action = obj.optString("action", "")

        when (action) {
            "open_app" -> {
                val appName = obj.getString("app_name")
                val opened = openApp(context, appName)
                val msg = if (opened) "✅ Opened $appName" else "❌ Could not open $appName"
                resultCallback(msg)
                onSpeak(msg)
            }

            "reply" -> {
                val text = obj.getString("text")
                resultCallback(text)
                onSpeak(text)
            }

            "add_item" -> {
                val items = obj.getJSONArray(
                    "items"
                )
                if (items.length() > 0) {
                    val first = items.getJSONObject(0)
                    val name = first.getString("name")
                    val qty = first.optString("quantity", "")
                    openApp(context, "blinkit")
                    val msg = "🛒 Added $qty $name to cart (initiated in Blinkit)"
                    resultCallback(msg)
                    onSpeak(msg)
                } else {
                    resultCallback("📝 No items found")
                    onSpeak("No items found")
                }
            }

            "call" -> {
                val contact = obj.getString("contact")
                val i = Intent(Intent.ACTION_DIAL)
                i.data = "tel:$contact".toUri()
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(i)
                val msg = "📞 Calling $contact"
                resultCallback(msg)
                onSpeak(msg)
            }

            else -> {
                val text = obj.optString("text", gptResponse)
                resultCallback(text)
                onSpeak(text)
            }
        }

    } catch (_: Exception) {
        resultCallback(gptResponse)
        onSpeak(gptResponse)
    }
}

fun openApp(context: Context, appName: String): Boolean {
    val pm = context.packageManager

    val map = mapOf(
        "whatsapp" to "com.whatsapp",
        "blinkit" to "com.grofers.customerapp",
        "youtube" to "com.google.android.youtube",
        "instagram" to "com.instagram.android",
        "chrome" to "com.android.chrome"
    )

    val pkg = map[appName.lowercase()]

    if (pkg != null) {
        val intent = pm.getLaunchIntentForPackage(pkg)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return true
        }
    }
    return false
}
