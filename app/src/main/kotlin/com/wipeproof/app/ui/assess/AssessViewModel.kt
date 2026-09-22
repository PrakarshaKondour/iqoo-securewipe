package com.wipeproof.app.ui.assess

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wipeproof.app.core.model.SanitizationCase
import com.wipeproof.app.core.model.SanitizationMethod
import com.wipeproof.app.core.sanitization.MethodAssessment
import com.wipeproof.app.data.repository.CaseRepository
import com.wipeproof.app.domain.usecase.AssessSanitizationUseCase
import com.wipeproof.app.officekit.OfficekitBridgeClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AssessUiState {
    object Loading : AssessUiState()
    data class Success(
        val case_: SanitizationCase,
        val assessments: List<MethodAssessment>,
        val isOfficeKitConnected: Boolean,
        val selectedMethod: SanitizationMethod?
    ) : AssessUiState()
    data class Error(val message: String) : AssessUiState()
}

@HiltViewModel
class AssessViewModel @Inject constructor(
    private val caseRepository: CaseRepository,
    private val assessUseCase: AssessSanitizationUseCase,
    private val officekitClient: OfficekitBridgeClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<AssessUiState>(AssessUiState.Loading)
    val uiState: StateFlow<AssessUiState> = _uiState.asStateFlow()

    fun loadAssessment(caseId: String) {
        viewModelScope.launch {
            _uiState.value = AssessUiState.Loading
            val case_ = caseRepository.getByCaseId(caseId)
            if (case_ == null) {
                _uiState.value = AssessUiState.Error("Case $caseId not found")
                return@launch
            }

            val officeKitConnected = officekitClient.isConnected()
            val assessments = assessUseCase.execute(case_, isOfficeKitConnected = officeKitConnected)
            _uiState.value = AssessUiState.Success(
                case_ = case_,
                assessments = assessments,
                isOfficeKitConnected = officeKitConnected,
                selectedMethod = case_.selectedMethod ?: assessments.firstOrNull()?.recommendedMethod
            )
        }
    }

    fun selectMethod(method: SanitizationMethod) {
        val currentState = _uiState.value
        if (currentState is AssessUiState.Success) {
            viewModelScope.launch {
                val updated = currentState.case_.copy(selectedMethod = method)
                caseRepository.update(updated)
                _uiState.value = currentState.copy(case_ = updated, selectedMethod = method)
            }
        }
    }
}
