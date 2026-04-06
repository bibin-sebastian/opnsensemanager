package com.bibin.opnsense.ui.devices

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bibin.opnsense.domain.model.Device
import com.bibin.opnsense.domain.model.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailSheet(
    device: Device,
    onDismiss: () -> Unit,
    viewModel: DeviceDetailViewModel = hiltViewModel<DeviceDetailViewModel, DeviceDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(device) }
    ),
) {
    val uiState by viewModel.uiState.collectAsState()
    var nameInput by remember { mutableStateOf(device.friendlyName ?: device.hostname) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(device.displayName, style = MaterialTheme.typography.titleLarge)
            Text(
                "${device.ip}  \u00b7  ${device.mac.ifBlank { "no MAC" }}",
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
            Spacer(Modifier.height(20.dp))

            // Traffic
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Traffic", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                if (uiState.isLoadingTraffic && uiState.trafficHistory.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }
            Spacer(Modifier.height(12.dp))

            when {
                uiState.trafficError != null && uiState.trafficHistory.isEmpty() ->
                    Text(
                        "Traffic unavailable: ${uiState.trafficError}",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodySmall,
                    )

                uiState.trafficHistory.isEmpty() ->
                    Text(
                        "Waiting for first sample\u2026",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodySmall,
                    )

                else -> {
                    val colorIn  = MaterialTheme.colorScheme.primary
                    val colorOut = MaterialTheme.colorScheme.tertiary
                    val latest   = uiState.trafficHistory.last()

                    // Rate cards
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RateCard("\u2193 Download", latest.kbpsIn,  MaterialTheme.colorScheme.primaryContainer,  Modifier.weight(1f))
                        RateCard("\u2191 Upload",   latest.kbpsOut, MaterialTheme.colorScheme.tertiaryContainer, Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(12.dp))

                    // Dual-line sparkline
                    val history = uiState.trafficHistory
                    val maxKbps = remember(history) {
                        history.maxOf { maxOf(it.kbpsIn, it.kbpsOut) }.coerceAtLeast(1f)
                    }
                    Canvas(Modifier.fillMaxWidth().height(100.dp)) {
                        if (history.size < 2) return@Canvas
                        val w = size.width; val h = size.height
                        val step = w / (history.size - 1).coerceAtLeast(1)

                        fun path(values: List<Float>) = Path().also { path ->
                            values.forEachIndexed { i, v ->
                                val x = i * step
                                val y = h - (v / maxKbps) * h
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                        }
                        drawPath(path(history.map { it.kbpsIn }),  colorIn,  style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                        drawPath(path(history.map { it.kbpsOut }), colorOut, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                        drawLine(colorIn.copy(alpha = 0.12f), Offset(0f, h), Offset(w, h), 1.dp.toPx())
                    }

                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LegendDot(colorIn,  "\u2193 Download")
                            LegendDot(colorOut, "\u2191 Upload")
                        }
                        val mins = (history.size * 10) / 60
                        Text(
                            if (mins < 1) "Last ${history.size * 10}s" else "Last ${mins}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }

                    // Session totals from pf state table
                    val totalIn  = uiState.totalBytesIn
                    val totalOut = uiState.totalBytesOut
                    if (totalIn > 0 || totalOut > 0) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))
                        Text("Active session totals", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("\u2193 ${formatBytes(totalIn)}",  style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                            Text("\u2191 ${formatBytes(totalOut)}", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RateCard(label: String, kbps: Float, bg: Color, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = bg), modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(formatKbps(kbps), style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Canvas(Modifier.size(8.dp)) { drawCircle(color) }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

private fun formatKbps(kbps: Float) = when {
    kbps >= 1_000_000f -> "%.1f Gbps".format(kbps / 1_000_000f)
    kbps >= 1_000f     -> "%.1f Mbps".format(kbps / 1_000f)
    kbps == 0f         -> "0 kbps"
    else               -> "%.0f kbps".format(kbps)
}

private fun formatBytes(bytes: Long) = when {
    bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576L     -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024L         -> "%.1f KB".format(bytes / 1_024.0)
    else                    -> "$bytes B"
}
