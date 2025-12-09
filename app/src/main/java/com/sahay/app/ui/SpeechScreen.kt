package com.sahay.app.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sahay.app.network.ChatGPTService
import org.json.JSONObject

@Composable
fun SpeechScreen(onSpeak: (String) -> Unit) {
    val context = LocalContext.current
    val chatGPT = remember { ChatGPTService(context) }
    var reply by remember { mutableStateOf("") }
    var textResult by remember { mutableStateOf("Tap the button and speak..") }

    // 1. Define the Speech Recognizer Launcher first
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenText?.firstOrNull() ?: "didn't catch that .."
            textResult = recognizedText

            // Call ChatGPT
            chatGPT.askChatGPT(recognizedText) { response ->
                // Use the handleAction function to parse JSON and decide what to do
                handleAction(context, response, onSpeak) { messageForScreen ->
                    reply = messageForScreen
                }
            }

        }
    }

    // 2. Define the Permission Launcher
    // This handles the request, and IF granted, launches the speechLauncher defined above
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, NOW we launch the speech recognizer intent
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            }
            speechLauncher.launch(intent)
        } else {
            // Permission denied
            textResult = "Permission to record audio is required."
        }
    }

    // 3. The UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "You: $textResult",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "AI: $reply",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            // We ONLY launch the permission check here.
            // The actual speech recognition starts inside the permissionLauncher callback above.
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }) {
            Text("Start Speaking")
        }
    }
}

fun handleAction(
    context: Context,
    gptResponse: String,
    onSpeak: (String) -> Unit,
    resultCallback: (String) -> Unit
) {
    try {
        //1. Try to parse the response as JSON
        // If gptResponse is just "Hello", this line will throw an error and go to 'catch'
        val obj = JSONObject(gptResponse)
        val action = obj.optString("action", "")

        when (action) {
            "open_app" -> {
                val appName = obj.getString("app_name")
                val opened = openApp(context, appName)
                val msg = if (opened) "Opened $appName" else "Could not open $appName"
                resultCallback(msg)
                onSpeak(msg)
            }

            "reply" -> {
                val text = obj.getString("text")
                resultCallback(text)
                onSpeak(text)
            }

            "add_item" -> {
                val items = obj.getJSONArray("items")
                if (items.length() > 0) {
                    val first = items.getJSONObject(0)
                    val name = first.getString("name")
                    val qty = first.optString("quantity", "")
                    openApp(context, "blinkit")
                    val msg = "Added $qty $name to cart (initiated in Blinkit)"
                    resultCallback(msg)
                    onSpeak(msg)
                } else {
                    resultCallback("No items found")
                    onSpeak("No items found")
                }
            }

            "call" -> {
                val contact = obj.getString("contact")
                val i = Intent(Intent.ACTION_DIAL)
                i.data = Uri.parse("tel:$contact")
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(i)
                val msg = "Calling $contact"
                resultCallback(msg)
                onSpeak(msg)
            }

            else -> {
                // It is valid JSON, but we don't know the action.
                // Just read the whole thing or look for a 'text' field.
                val text = obj.optString("text", gptResponse)
                resultCallback(text)
                onSpeak(text)
            }
        }

    } catch (e: Exception) {
        // 2. FALLBACK: This happens if ChatGPT sent plain text (not JSON).
        // Instead of showing an error, we just show/speak the text!
        resultCallback(gptResponse)
        onSpeak(gptResponse)
    }
}
fun openApp(context: Context, appName: String): Boolean {
    val pm = context.packageManager

    // Map common names to package names
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
