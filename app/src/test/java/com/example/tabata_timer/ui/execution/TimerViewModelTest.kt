package com.example.tabata_timer.ui.execution

import com.example.tabata_timer.domain.model.TimerState
import com.example.tabata_timer.domain.model.WorkoutConfig
import com.example.tabata_timer.domain.timer.TimerManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TimerViewModelTest {

    private val timerManager = mockk<TimerManager>(relaxed = true)
    private lateinit var viewModel: TimerViewModel
    private val fakeState = MutableStateFlow<TimerState>(TimerState.Idle())

    @Before
    fun setup() {
        every { timerManager.timerState } returns fakeState
        viewModel = TimerViewModel(timerManager)
    }

    private fun createTestConfig() = WorkoutConfig(
        name = "Test",
        color = 0,
        warmupSeconds = 1,
        workSeconds = 1,
        restSeconds = 1,
        rounds = 1,
        sets = 1,
        restBetweenSetsSeconds = 1,
        cooldownSeconds = 1
    )

    /**
     * RISK: ViewModel might not reflect the actual state of the engine.
     * BUG PREVENTED: Ensures the UI always shows what the Domain layer dictates.
     */
    @Test
    fun `viewModel timerState reflects manager state`() {
        // Given
        val expectedState = TimerState.Working(createTestConfig(), 10, 1, 1)
        
        // When
        fakeState.value = expectedState
        
        // Then
        assertEquals(expectedState, viewModel.timerState.value)
    }

    /**
     * RISK: UI interactions not reaching the engine.
     * BUG PREVENTED: Ensures buttons like Pause actually trigger the logic.
     */
    @Test
    fun `commands are delegated to manager`() {
        // When
        viewModel.pause()
        viewModel.resume()
        viewModel.skip()
        viewModel.stop()
        
        // Then
        verify { timerManager.pause() }
        verify { timerManager.resume() }
        verify { timerManager.skip() }
        verify { timerManager.stop() }
    }
}
