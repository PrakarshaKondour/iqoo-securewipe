package com.wipeproof.app.ui.identify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wipeproof.app.core.model.DeviceInfo
import com.wipeproof.app.core.model.SanitizationCase
import com.wipeproof.app.domain.usecase.CreateCaseUseCase
import com.wipeproof.app.domain.usecase.IdentifyDeviceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class IdentifyUiState {
    object Idle : IdentifyUiState()
    data class Identified(val deviceInfo: DeviceInfo, val createdCase: SanitizationCase?) : IdentifyUiState()
    data class Error(val message: String) : IdentifyUiState()
}

@HiltViewModel
class IdentifyViewModel @Inject constructor(
    private val identifyDeviceUseCase: IdentifyDeviceUseCase,
    private val createCaseUseCase: CreateCaseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<IdentifyUiState>(IdentifyUiState.Idle)
    val uiState: StateFlow<IdentifyUiState> = _uiState.asStateFlow()

    init {
        loadCurrentDevice()
    }

    fun loadCurrentDevice(scannedAssetId: String? = null) {
        viewModelScope.launch {
            try {
                val info = identifyDeviceUseCase.execute(scannedAssetId)
                _uiState.value = IdentifyUiState.Identified(info, null)
            } catch (e: Exception) {
                _uiState.value = IdentifyUiState.Error(e.localizedMessage ?: "Device identification failed")
            }
        }
    }

    fun onBarcodeScanned(barcode: String) {
        loadCurrentDevice(scannedAssetId = barcode.trim())
    }

    fun createCase(isDemo: Boolean = false, onCaseCreated: (String) -> Unit) {
        val currentState = _uiState.value
        if (currentState is IdentifyUiState.Identified) {
            viewModelScope.launch {
                val case_ = createCaseUseCase.execute(currentState.deviceInfo, isDemoMode = isDemo)
                _uiState.value = currentState.copy(createdCase = case_)
                onCaseCreated(case_.caseId)
            }
        }
    }
}
