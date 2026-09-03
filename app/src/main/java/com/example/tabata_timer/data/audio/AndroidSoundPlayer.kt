package com.example.tabata_timer.data.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.tabata_timer.domain.audio.SoundPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidSoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) : SoundPlayer, TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isInitialized = false
    private val pendingSounds = mutableListOf<String>()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isInitialized = true
            synchronized(pendingSounds) {
                pendingSounds.forEach { playSound(it) }
                pendingSounds.clear()
            }
        }
    }

    override fun playSound(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            synchronized(pendingSounds) {
                pendingSounds.add(text)
            }
        }
    }

    override fun stop() {
        tts?.stop()
    }

    override fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
