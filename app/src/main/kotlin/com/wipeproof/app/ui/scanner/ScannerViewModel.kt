package com.wipeproof.app.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wipeproof.app.core.model.VerificationResult
import com.wipeproof.app.domain.usecase.VerifyCertificateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Verifying : ScannerUiState()
    data class Result(val verificationResult: VerificationResult) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val verifyCertificateUseCase: VerifyCertificateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun verifyPayload(qrPayload: String) {
        if (_uiState.value is ScannerUiState.Verifying) return

        viewModelScope.launch {
            _uiState.value = ScannerUiState.Verifying
            try {
                val result = verifyCertificateUseCase.execute(qrPayload)
                _uiState.value = ScannerUiState.Result(result)
            } catch (e: Exception) {
                _uiState.value = ScannerUiState.Error(e.localizedMessage ?: "Verification failed")
            }
        }
    }

    fun resetScanner() {
        _uiState.value = ScannerUiState.Idle
    }
}
