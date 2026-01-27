package com.sahay.app

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sahay.app.ui.SpeechScreen
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         logInstalledApps(this)

        // 1. Initialize the Text To Speech engine
        tts = TextToSpeech(this, this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 2. Load your new SpeechScreen here
                    SpeechScreen(
                        onSpeak = { textToSpeak ->
                            speakOut(textToSpeak)
                        }
                    )
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    private fun speakOut(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onDestroy() {
        // Shutdown TTS when app closes to save resources
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
        super.onDestroy()
    }
}
fun logInstalledApps(context: android.content.Context) {
    val pm = context.packageManager
    val packages = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
    for (packageInfo in packages) {
        // This will print every app name and its package ID
        android.util.Log.d("INSTALLED_APPS", "App: ${pm.getApplicationLabel(packageInfo)} -> Package: ${packageInfo.packageName}")
    }
}
