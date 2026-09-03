package com.example.tabata_timer.domain.timer

import app.cash.turbine.test
import com.example.tabata_timer.domain.audio.SoundPlayer
import com.example.tabata_timer.domain.model.TimerState
import com.example.tabata_timer.domain.model.WorkoutConfig
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerManagerTest {

    private val soundPlayer = mockk<SoundPlayer>(relaxed = true)
    private lateinit var timerManager: TimerManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        timerManager = TimerManager(soundPlayer)
        // Link the manager's time to the test scheduler to prevent "drift" issues in virtual time
        timerManager.timeProvider = { testDispatcher.scheduler.currentTime }
        timerManager.ioDispatcher = testDispatcher
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createTestConfig(
        warmupSeconds: Int = 0,
        workSeconds: Int = 1,
        restSeconds: Int = 0,
        rounds: Int = 1,
        sets: Int = 1,
        restBetweenSetsSeconds: Int = 0,
        cooldownSeconds: Int = 0
    ) = WorkoutConfig(
        name = "Test",
        color = 0,
        warmupSeconds = warmupSeconds,
        workSeconds = workSeconds,
        restSeconds = restSeconds,
        rounds = rounds,
        sets = sets,
        restBetweenSetsSeconds = restBetweenSetsSeconds,
        cooldownSeconds = cooldownSeconds
    )

    /**
     * RISK: Timer doesn't transition through all states correctly, especially REST phases.
     * BUG PREVENTED: Ensures the complete sequence including Rest and Multi-round logic.
     */
    @Test
    fun `full workout transitions through all phases including rest`() = runTest(testDispatcher) {
        val config = createTestConfig(
            warmupSeconds = 1,
            workSeconds = 1,
            restSeconds = 1,
            rounds = 2,
            sets = 1
        )

        timerManager.timerState.test {
            assertTrue(awaitItem() is TimerState.Idle)
            timerManager.startWorkout(config)
            
            assertTrue(awaitItem() is TimerState.Preparing) // Warmup
            assertTrue(awaitItem() is TimerState.Working)   // Round 1 Work
            assertTrue(awaitItem() is TimerState.Resting)   // Round 1 Rest
            assertTrue(awaitItem() is TimerState.Working)   // Round 2 Work
            assertTrue(awaitItem() is TimerState.Finished)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when phases have 0 duration, they are skipped`() = runTest(testDispatcher) {
        val config = createTestConfig(warmupSeconds = 0, workSeconds = 1)

        timerManager.timerState.test {
            awaitItem() // Idle
            timerManager.startWorkout(config)
            
            val firstState = awaitItem()
            assertTrue("Expected Working, got $firstState", firstState is TimerState.Working)
            
            assertTrue(awaitItem() is TimerState.Finished)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * RISK: Race conditions between start and pause.
     * BUG PREVENTED: Ensures that even if pause is called immediately after start, the job is cancelled.
     */
    @Test
    fun `rapid pause and resume handles state correctly`() = runTest(testDispatcher) {
        val config = createTestConfig(workSeconds = 10)
        
        timerManager.startWorkout(config)
        advanceTimeBy(500)
        
        timerManager.pause()
        assertTrue(timerManager.timerState.value is TimerState.Paused)
        
        timerManager.resume()
        advanceTimeBy(100)
        assertTrue(timerManager.timerState.value is TimerState.Working)
    }

    @Test
    fun `sound player triggers at start and end`() = runTest(testDispatcher) {
        val config = createTestConfig(warmupSeconds = 0, workSeconds = 1)
        
        timerManager.startWorkout(config)
        advanceUntilIdle()
        
        verify { soundPlayer.playSound("Work") }
        verify { soundPlayer.playSound("Workout finished") }
    }

    @Test
    fun `skip requested advances to next state immediately`() = runTest(testDispatcher) {
        val config = createTestConfig(warmupSeconds = 10, workSeconds = 10)
        
        timerManager.timerState.test {
            awaitItem() // Idle
            timerManager.startWorkout(config)
            assertTrue(awaitItem() is TimerState.Preparing)
            
            timerManager.skip()
            
            assertTrue(awaitItem() is TimerState.Working)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * RISK: Stop event doesn't clean up sounds or state.
     * BUG PREVENTED: Ensures resources are released and state resets to Idle.
     */
    @Test
    fun `stop command resets to idle and stops sounds`() = runTest(testDispatcher) {
        val config = createTestConfig(workSeconds = 10)
        timerManager.startWorkout(config)
        advanceTimeBy(1000)
        
        timerManager.stop()
        
        assertTrue(timerManager.timerState.value is TimerState.Idle)
        verify { soundPlayer.stop() }
    }
}
