package com.bibin.opnsense.ui.devices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bibin.opnsense.domain.model.Device
import com.bibin.opnsense.domain.model.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListScreen(
    viewModel: DeviceListViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val state by viewModel.uiState.collectAsState()
    var selectedDevice by remember { mutableStateOf<Device?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Devices") },
                actions = {
                    IconButton(onClick = { viewModel.loadDevices() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .padding(bottom = contentPadding.calculateBottomPadding())
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.errorMessage != null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Cannot reach firewall",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadDevices() }) { Text("Retry") }
                }
                state.devices.isEmpty() -> Text(
                    "No devices found",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.outline,
                )
                else -> LazyColumn {
                    items(state.devices, key = { it.mac }) { device ->
                        DeviceRow(
                            device = device,
                            onToggleBlock = { viewModel.toggleBlock(device) },
                            onClick = { selectedDevice = device },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    selectedDevice?.let { device ->
        DeviceDetailSheet(
            device = device,
            onDismiss = {
                selectedDevice = null
                viewModel.loadDevices()
            },
        )
    }
}

@Composable
private fun DeviceRow(
    device: Device,
    onToggleBlock: () -> Unit,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(device.displayName)
                if (device.isOnline) {
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primary,
                    ) {}
                }
            }
        },
        supportingContent = { Text("${device.ip} · ${device.mac}") },
        leadingContent = {
            Icon(
                imageVector = if (device.isBlocked) Icons.Default.Lock else Icons.Default.Check,
                contentDescription = null,
                tint = if (device.isBlocked) MaterialTheme.colorScheme.error
                       else MaterialTheme.colorScheme.primary,
            )
        },
        trailingContent = {
            Switch(
                checked = !device.isBlocked,
                onCheckedChange = { onToggleBlock() },
            )
        },
    )
}
