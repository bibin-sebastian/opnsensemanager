package com.bibin.opnsense.ui.navigation

import androidx.lifecycle.ViewModel
import com.bibin.opnsense.util.CredentialManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    val credentialManager: CredentialManager,
) : ViewModel()
