package com.bibin.opnsense.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/** Disables the copy/cut/paste context menu on wrapped text fields. */
private val NoOpTextToolbar = object : TextToolbar {
    override val status: TextToolbarStatus get() = TextToolbarStatus.Hidden
    override fun hide() {}
    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
    ) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val state by viewModel.uiState.collectAsState()
    val creds = viewModel.credentialManager
    var url     by remember { mutableStateOf(creds.firewallUrl) }
    var apiKey  by remember { mutableStateOf("") }
    var apiSecret by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(bottom = contentPadding.calculateBottomPadding())
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Firewall connection ──────────────────────────────────────
            Text("Firewall Connection", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Firewall URL") },
                placeholder = { Text("https://192.168.1.1") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = state.isEditingCredentials || creds.firewallUrl.isBlank(),
            )
            Spacer(Modifier.height(12.dp))

            if (!state.isEditingCredentials && creds.isConfigured) {
                // ── Masked credential display ────────────────────────────
                MaskedCredentialRow(label = "API Key")
                Spacer(Modifier.height(8.dp))
                MaskedCredentialRow(label = "API Secret")
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { viewModel.startEditingCredentials() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Replace Credentials")
                }
            } else {
                // ── Edit mode: masked input, no clipboard ────────────────
                CompositionLocalProvider(LocalTextToolbar provides NoOpTextToolbar) {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = apiSecret,
                        onValueChange = { apiSecret = it },
                        label = { Text("API Secret") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.saveAndTest(url, apiKey, apiSecret) },
                    enabled = !state.isTesting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isTesting) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save & Test Connection")
                    }
                }
            }

            state.testResult?.let { result ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = result,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (result.startsWith("Connected")) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ── Security ─────────────────────────────────────────────────
            if (state.biometricAvailable) {
                Text("Security", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Biometric / Fingerprint Lock", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Require authentication on app open",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    Switch(
                        checked = state.biometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric(it) },
                    )
                }
                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
            }

            // ── About ─────────────────────────────────────────────────────
            Text("About", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "OPNsense Manager v2.0.0",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Open source — github.com/bibin-sebastian/opnsenseAPK",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun MaskedCredentialRow(label: String) {
    OutlinedTextField(
        value = "••••••••••••",
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        readOnly = true,
        enabled = false,
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
    )
}
