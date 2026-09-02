package com.example.tabata_timer.domain.audio

interface SoundPlayer {
    fun playSound(text: String)
    fun stop()
    fun release()
}
