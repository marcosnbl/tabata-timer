package com.example.tabata_timer.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabata_timer.domain.model.WorkoutConfig
import com.example.tabata_timer.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutConfigViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutConfigUiState())
    val uiState = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateWarmup(seconds: Int) {
        _uiState.update { it.copy(warmupSeconds = (it.warmupSeconds + seconds).coerceAtLeast(0)) }
    }

    fun updateWork(seconds: Int) {
        _uiState.update { it.copy(workSeconds = (it.workSeconds + seconds).coerceAtLeast(5)) }
    }

    fun updateRest(seconds: Int) {
        _uiState.update { it.copy(restSeconds = (it.restSeconds + seconds).coerceAtLeast(0)) }
    }

    fun updateRounds(count: Int) {
        _uiState.update { it.copy(rounds = (it.rounds + count).coerceAtLeast(1)) }
    }

    fun updateSets(count: Int) {
        _uiState.update { it.copy(sets = (it.sets + count).coerceAtLeast(1)) }
    }

    fun updateRestBetweenSets(seconds: Int) {
        _uiState.update { it.copy(restBetweenSetsSeconds = (it.restBetweenSetsSeconds + seconds).coerceAtLeast(0)) }
    }

    fun updateCooldown(seconds: Int) {
        _uiState.update { it.copy(cooldownSeconds = (it.cooldownSeconds + seconds).coerceAtLeast(0)) }
    }

    fun saveWorkout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val config = with(_uiState.value) {
                WorkoutConfig(
                    name = name,
                    color = color,
                    warmupSeconds = warmupSeconds,
                    workSeconds = workSeconds,
                    restSeconds = restSeconds,
                    rounds = rounds,
                    sets = sets,
                    restBetweenSetsSeconds = restBetweenSetsSeconds,
                    cooldownSeconds = cooldownSeconds
                )
            }
            repository.insertWorkout(config)
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}
