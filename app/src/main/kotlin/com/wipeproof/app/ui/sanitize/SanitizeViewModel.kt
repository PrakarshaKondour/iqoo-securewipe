package com.wipeproof.app.ui.sanitize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wipeproof.app.core.model.SanitizationCase
import com.wipeproof.app.core.model.SanitizationEvent
import com.wipeproof.app.core.model.SanitizationMethod
import com.wipeproof.app.data.repository.CaseRepository
import com.wipeproof.app.data.repository.EventRepository
import com.wipeproof.app.domain.usecase.ExecuteSanitizationUseCase
import com.wipeproof.app.domain.usecase.SanitizationProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SanitizeUiState {
    object Loading : SanitizeUiState()
    data class Ready(val case_: SanitizationCase, val method: SanitizationMethod) : SanitizeUiState()
    data class Running(
        val case_: SanitizationCase,
        val method: SanitizationMethod,
        val progress: SanitizationProgress,
        val recentEvents: List<SanitizationEvent>
    ) : SanitizeUiState()
    data class Completed(val case_: SanitizationCase) : SanitizeUiState()
    data class Error(val message: String) : SanitizeUiState()
}

@HiltViewModel
class SanitizeViewModel @Inject constructor(
    private val caseRepository: CaseRepository,
    private val eventRepository: EventRepository,
    private val executeSanitizationUseCase: ExecuteSanitizationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SanitizeUiState>(SanitizeUiState.Loading)
    val uiState: StateFlow<SanitizeUiState> = _uiState.asStateFlow()

    fun loadCase(caseId: String) {
        viewModelScope.launch {
            _uiState.value = SanitizeUiState.Loading
            val case_ = caseRepository.getByCaseId(caseId)
            if (case_ == null) {
                _uiState.value = SanitizeUiState.Error("Case $caseId not found")
                return@launch
            }
            val method = case_.selectedMethod ?: SanitizationMethod.FACTORY_RESET
            _uiState.value = SanitizeUiState.Ready(case_, method)
        }
    }

    fun startSanitization() {
        val currentState = _uiState.value
        if (currentState is SanitizeUiState.Ready) {
            val case_ = currentState.case_
            val method = currentState.method

            viewModelScope.launch {
                executeSanitizationUseCase.execute(case_, method).collect { progress ->
                    val events = eventRepository.getEventsForCase(case_.caseId)
                    if (progress.isComplete && progress.error == null) {
                        _uiState.value = SanitizeUiState.Completed(case_)
                    } else if (progress.error != null) {
                        _uiState.value = SanitizeUiState.Error(progress.error)
                    } else {
                        _uiState.value = SanitizeUiState.Running(
                            case_ = case_,
                            method = method,
                            progress = progress,
                            recentEvents = events.takeLast(5)
                        )
                    }
                }
            }
        }
    }
}
