package com.wipeproof.app.ui.prove

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wipeproof.app.core.model.SignedCertificate
import com.wipeproof.app.data.repository.CertificateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProveUiState {
    object Loading : ProveUiState()
    data class Ready(
        val signedCertificate: SignedCertificate,
        val qrPayload: String
    ) : ProveUiState()
    data class Error(val message: String) : ProveUiState()
}

@HiltViewModel
class ProveViewModel @Inject constructor(
    private val certificateRepository: CertificateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProveUiState>(ProveUiState.Loading)
    val uiState: StateFlow<ProveUiState> = _uiState.asStateFlow()

    fun loadCertificate(caseId: String) {
        viewModelScope.launch {
            _uiState.value = ProveUiState.Loading
            val certPair = certificateRepository.getByCaseId(caseId)
            if (certPair == null) {
                _uiState.value = ProveUiState.Error("Certificate for case $caseId not found")
                return@launch
            }
            _uiState.value = ProveUiState.Ready(certPair.first, certPair.second)
        }
    }
}
