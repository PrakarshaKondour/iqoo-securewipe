package com.wipeproof.app.ui.verify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wipeproof.app.core.model.EvidenceHashPayload
import com.wipeproof.app.core.model.SanitizationCase
import com.wipeproof.app.core.model.VerificationOutcome
import com.wipeproof.app.data.repository.CaseRepository
import com.wipeproof.app.domain.usecase.BuildCertificateUseCase
import com.wipeproof.app.domain.usecase.CollectEvidenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VerifyUiState {
    object Loading : VerifyUiState()
    data class Display(
        val case_: SanitizationCase,
        val evidenceList: List<EvidenceHashPayload>,
        val outcome: VerificationOutcome,
        val isCertified: Boolean = false
    ) : VerifyUiState()
    data class Error(val message: String) : VerifyUiState()
}

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val caseRepository: CaseRepository,
    private val collectEvidenceUseCase: CollectEvidenceUseCase,
    private val buildCertificateUseCase: BuildCertificateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<VerifyUiState>(VerifyUiState.Loading)
    val uiState: StateFlow<VerifyUiState> = _uiState.asStateFlow()

    fun loadVerification(caseId: String) {
        viewModelScope.launch {
            _uiState.value = VerifyUiState.Loading
            val case_ = caseRepository.getByCaseId(caseId)
            if (case_ == null) {
                _uiState.value = VerifyUiState.Error("Case $caseId not found")
                return@launch
            }

            val evidence = collectEvidenceUseCase.execute(case_)
            val outcome = when {
                case_.isDemoMode -> VerificationOutcome.DEMO
                evidence.isNotEmpty() -> VerificationOutcome.SANITIZED_VERIFIED
                else -> VerificationOutcome.SANITIZED_UNVERIFIED
            }

            _uiState.value = VerifyUiState.Display(
                case_ = case_,
                evidenceList = evidence,
                outcome = outcome
            )
        }
    }

    fun generateCertificate(onCertificateReady: (String) -> Unit) {
        val currentState = _uiState.value
        if (currentState is VerifyUiState.Display) {
            viewModelScope.launch {
                val (_, _) = buildCertificateUseCase.execute(currentState.case_)
                _uiState.value = currentState.copy(isCertified = true)
                onCertificateReady(currentState.case_.caseId)
            }
        }
    }
}
