package com.example.tabata_timer.ui.execution

import androidx.lifecycle.ViewModel
import com.example.tabata_timer.domain.timer.TimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val timerManager: TimerManager
) : ViewModel() {

    val timerState = timerManager.timerState

    fun pause() {
        timerManager.pause()
    }

    fun resume() {
        timerManager.resume()
    }

    fun skip() {
        timerManager.skip()
    }

    fun stop() {
        timerManager.stop()
    }
}
