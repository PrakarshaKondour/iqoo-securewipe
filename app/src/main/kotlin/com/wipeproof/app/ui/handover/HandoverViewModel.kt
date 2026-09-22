package com.wipeproof.app.ui.handover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wipeproof.app.domain.usecase.GetPassportUseCase
import com.wipeproof.app.domain.usecase.PassportData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HandoverUiState {
    object Loading : HandoverUiState()
    data class Display(val passport: PassportData) : HandoverUiState()
    data class Error(val message: String) : HandoverUiState()
}

@HiltViewModel
class HandoverViewModel @Inject constructor(
    private val getPassportUseCase: GetPassportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HandoverUiState>(HandoverUiState.Loading)
    val uiState: StateFlow<HandoverUiState> = _uiState.asStateFlow()

    fun loadPassport(caseId: String) {
        viewModelScope.launch {
            _uiState.value = HandoverUiState.Loading
            val passport = getPassportUseCase.execute(caseId)
            if (passport == null) {
                _uiState.value = HandoverUiState.Error("Case $caseId not found")
                return@launch
            }
            _uiState.value = HandoverUiState.Display(passport)
        }
    }

    fun completeHandover(caseId: String) {
        viewModelScope.launch {
            getPassportUseCase.markHandoverComplete(caseId)
            loadPassport(caseId)
        }
    }
}
