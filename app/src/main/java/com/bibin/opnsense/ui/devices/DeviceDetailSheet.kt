package com.bibin.opnsense.ui.devices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bibin.opnsense.data.remote.dto.ConnectionEntry
import com.bibin.opnsense.domain.model.Device
import com.bibin.opnsense.domain.model.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailSheet(
    device: Device,
    onDismiss: () -> Unit,
    viewModel: DeviceDetailViewModel = hiltViewModel<DeviceDetailViewModel, DeviceDetailViewModel.Factory>(
        key = device.ip,
        creationCallback = { factory -> factory.create(device) }
    ),
) {
    val uiState by viewModel.uiState.collectAsState()
    var nameInput by remember { mutableStateOf(device.friendlyName ?: device.hostname) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
        ) {
            // Header
            Text(device.displayName, style = MaterialTheme.typography.titleLarge)
            Text(
                "${device.ip}  ·  ${if (device.mac.isBlank()) "no MAC" else "MAC: ${device.mac}"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )

            Spacer(Modifier.height(24.dp))

            // Rename
            Text("Rename", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Friendly name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.saveFriendlyName(nameInput); onDismiss() },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save Name") }

            uiState.errorMessage?.let { err ->
                Spacer(Modifier.height(8.dp))
                Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { viewModel.clearError() }) { Text("Dismiss") }
            }

            Spacer(Modifier.height(28.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Connections section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Active Connections", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                if (uiState.isLoadingConnections && uiState.connections.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        "${uiState.connections.size} session${if (uiState.connections.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            when {
                uiState.connectionsError != null && uiState.connections.isEmpty() ->
                    Text(
                        "Connections unavailable: ${uiState.connectionsError}",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodySmall,
                    )

                uiState.connections.isEmpty() && !uiState.isLoadingConnections ->
                    Text(
                        "No active connections",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodySmall,
                    )

                else -> {
                    // Column headers
                    ConnectionRowHeader()
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    // Use a fixed-height scrollable list so the sheet doesn't grow unbounded
                    val listHeight = (uiState.connections.size * 56).coerceAtMost(320)
                    LazyColumn(modifier = Modifier.height(listHeight.dp)) {
                        items(uiState.connections) { conn ->
                            ConnectionRow(conn)
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionRowHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Text(
            "Proto",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.width(48.dp),
        )
        Text(
            "Source",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.weight(1f),
        )
        Text(
            "Destination",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.weight(1f),
        )
        Text(
            "Bytes",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.width(60.dp),
        )
    }
}

@Composable
private fun ConnectionRow(conn: ConnectionEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Protocol badge
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier.width(48.dp),
        ) {
            Text(
                conn.proto.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }

        // Source IP:port
        IpColumn(
            name = conn.srcName,
            addr = conn.srcAddr,
            port = conn.srcPort,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
        )

        // Destination IP:port
        IpColumn(
            name = conn.dstName,
            addr = conn.dstAddr,
            port = conn.dstPort,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
        )

        // Bytes + age
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                formatBytes(conn.totalBytes),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                formatAge(conn.firstSeenMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
private fun IpColumn(name: String?, addr: String, port: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        if (name != null) {
            Text(
                name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                addr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontFamily = FontFamily.Monospace,
            )
        } else {
            Text(
                addr,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
        }
        if (port.isNotBlank() && port != "0") {
            Text(
                ":$port",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

private fun formatAge(firstSeenMs: Long): String {
    val secs = (System.currentTimeMillis() - firstSeenMs) / 1_000
    return when {
        secs < 10   -> "just now"
        secs < 60   -> "${secs}s"
        secs < 3600 -> "${secs / 60}m"
        else        -> "${secs / 3600}h"
    }
}

private fun formatBytes(bytes: Long) = when {
    bytes >= 1_073_741_824L -> "%.1fG".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576L     -> "%.1fM".format(bytes / 1_048_576.0)
    bytes >= 1_024L         -> "%.1fK".format(bytes / 1_024.0)
    else                    -> "${bytes}B"
}
