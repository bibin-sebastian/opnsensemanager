package com.bibin.opnsense.ui.auth

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.bibin.opnsense.util.BiometricHelper

/**
 * Shows [content] if the user has authenticated (or biometric is disabled).
 * Otherwise shows a lock screen and immediately presents the biometric prompt.
 */
@Composable
fun BiometricGate(
    biometricHelper: BiometricHelper,
    content: @Composable () -> Unit,
) {
    var authenticated by remember {
        mutableStateOf(!biometricHelper.isBiometricEnabled)
    }
    var authError by remember { mutableStateOf<String?>(null) }

    if (authenticated) {
        content()
        return
    }

    val activity = LocalContext.current as FragmentActivity

    fun authenticate() {
        authError = null
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    authenticated = true
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        authError = errString.toString()
                    }
                }
                override fun onAuthenticationFailed() {
                    authError = "Authentication failed — try again"
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("OPNsense Manager")
            .setSubtitle("Authenticate to access your firewall")
            .setAllowedAuthenticators(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        prompt.authenticate(promptInfo)
    }

    // Trigger prompt immediately when this composable enters composition
    LaunchedEffect(Unit) { authenticate() }

    // Lock screen shown behind / while the system dialog is displayed
    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp),
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "OPNsense Manager",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    "Authentication required",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
                authError?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Button(onClick = { authenticate() }) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Unlock")
                }
            }
        }
    }
}
