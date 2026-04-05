package com.bibin.opnsense.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val state by viewModel.uiState.collectAsState()
    val creds = viewModel.credentialManager
    var url by remember { mutableStateOf(creds.firewallUrl) }
    var apiKey by remember { mutableStateOf(creds.apiKey) }
    var apiSecret by remember { mutableStateOf(creds.apiSecret) }

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
            Text("Firewall Connection", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Firewall URL") },
                placeholder = { Text("https://192.168.1.1") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = apiSecret,
                onValueChange = { apiSecret = it },
                label = { Text("API Secret") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )

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

            Text("About", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "OPNsense Manager v1.0.0",
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
