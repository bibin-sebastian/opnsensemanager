package com.bibin.opnsense.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibin.opnsense.data.repository.OPNsenseRepository
import com.bibin.opnsense.util.CredentialManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val isTesting: Boolean = false,
    val connectionSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val credentialManager: CredentialManager,
    private val repository: OPNsenseRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(OnboardingUiState())

    fun testConnection(url: String, key: String, secret: String) {
        viewModelScope.launch {
            uiState.update { it.copy(isTesting = true, errorMessage = null, connectionSuccess = false) }
            credentialManager.saveAll(url, key, secret)
            repository.refreshApi()
            val success = repository.testConnection()
            if (success) {
                uiState.update { it.copy(isTesting = false, connectionSuccess = true) }
            } else {
                credentialManager.clear()
                uiState.update {
                    it.copy(
                        isTesting = false,
                        errorMessage = "Cannot reach firewall. Check URL and credentials.",
                    )
                }
            }
        }
    }
}
