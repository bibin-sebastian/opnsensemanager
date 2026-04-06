package com.bibin.opnsense.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibin.opnsense.data.repository.OPNsenseRepository
import com.bibin.opnsense.util.BiometricHelper
import com.bibin.opnsense.util.CredentialManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isTesting: Boolean = false,
    val testResult: String? = null,
    val isEditingCredentials: Boolean = false,
    val biometricEnabled: Boolean = false,
    val biometricAvailable: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val credentialManager: CredentialManager,
    val biometricHelper: BiometricHelper,
    private val repository: OPNsenseRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(
        SettingsUiState(
            isEditingCredentials = !credentialManager.isConfigured,
            biometricEnabled = biometricHelper.isBiometricEnabled,
            biometricAvailable = biometricHelper.canAuthenticate(),
        )
    )

    fun startEditingCredentials() {
        uiState.update { it.copy(isEditingCredentials = true, testResult = null) }
    }

    fun saveAndTest(url: String, key: String, secret: String) {
        viewModelScope.launch {
            uiState.update { it.copy(isTesting = true, testResult = null) }
            credentialManager.saveAll(url, key, secret)
            repository.refreshApi()
            val ok = repository.testConnection()
            uiState.update {
                it.copy(
                    isTesting = false,
                    isEditingCredentials = !ok,
                    testResult = if (ok) "Connected successfully"
                                 else "Connection failed — check URL and credentials",
                )
            }
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        biometricHelper.isBiometricEnabled = enabled
        uiState.update { it.copy(biometricEnabled = enabled) }
    }
}
