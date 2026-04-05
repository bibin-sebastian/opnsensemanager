package com.bibin.opnsense.ui.devices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bibin.opnsense.domain.model.BlockSchedule
import com.bibin.opnsense.domain.model.Device
import com.bibin.opnsense.domain.model.displayName
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailSheet(
    device: Device,
    onDismiss: () -> Unit,
    viewModel: DeviceDetailViewModel = hiltViewModel<DeviceDetailViewModel, DeviceDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(device) }
    ),
) {
    val schedules by viewModel.schedules.collectAsState()
    var nameInput by remember { mutableStateOf(device.friendlyName ?: device.hostname) }
    var showSchedulePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(device.displayName, style = MaterialTheme.typography.titleLarge)
            Text(
                "${device.ip} · ${device.mac}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )

            Spacer(Modifier.height(20.dp))
            Text("Rename Device", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Friendly Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.saveFriendlyName(nameInput); onDismiss() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Name")
            }

            Spacer(Modifier.height(24.dp))
            Text("Block Schedules", style = MaterialTheme.typography.titleMedium)

            if (schedules.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "No schedules set.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            } else {
                schedules.forEach { schedule ->
                    ListItem(
                        headlineContent = {
                            Text(
                                "Block %02d:%02d – %02d:%02d".format(
                                    schedule.startHour, schedule.startMinute,
                                    schedule.endHour, schedule.endMinute,
                                )
                            )
                        },
                        supportingContent = {
                            Text(schedule.daysOfWeek.joinToString(", ") { dayLabel(it) })
                        },
                        trailingContent = {
                            IconButton(onClick = { viewModel.deleteSchedule(schedule.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete schedule")
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showSchedulePicker = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add Schedule")
            }
        }
    }

    if (showSchedulePicker) {
        SchedulePickerDialog(
            deviceMac = device.mac,
            onSave = { schedule ->
                viewModel.saveSchedule(schedule)
                showSchedulePicker = false
            },
            onDismiss = { showSchedulePicker = false },
        )
    }
}

@Composable
private fun SchedulePickerDialog(
    deviceMac: String,
    onSave: (BlockSchedule) -> Unit,
    onDismiss: () -> Unit,
) {
    var startHour by remember { mutableIntStateOf(22) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(7) }
    var endMinute by remember { mutableIntStateOf(0) }

    val days = listOf(
        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
        Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY,
    )
    val dayLabels = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
    var selectedDays by remember {
        mutableStateOf(
            setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Block Schedule") },
        text = {
            Column {
                Text("Block from", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startHour.toString(),
                        onValueChange = { startHour = it.toIntOrNull()?.coerceIn(0, 23) ?: startHour },
                        label = { Text("Hour") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = "%02d".format(startMinute),
                        onValueChange = { startMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: startMinute },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("Until", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = endHour.toString(),
                        onValueChange = { endHour = it.toIntOrNull()?.coerceIn(0, 23) ?: endHour },
                        label = { Text("Hour") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = "%02d".format(endMinute),
                        onValueChange =oreplace@ { endMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: endMinute },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text("Days", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    days.forEachIndexed { i, day ->
                        FilterChip(
                            selected = day in selectedDays,
                            onClick = {
                                selectedDays = if (day in selectedDays) selectedDays - day
                                               else selectedDays + day
                            },
                            label = { Text(dayLabels[i]) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        BlockSchedule(
                            deviceMac = deviceMac,
                            startHour = startHour,
                            startMinute = startMinute,
                            endHour = endHour,
                            endMinute = endMinute,
                            daysOfWeek = selectedDays,
                        )
                    )
                },
                enabled = selectedDays.isNotEmpty(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun dayLabel(day: Int): String = when (day) {
    Calendar.MONDAY -> "Mon"
    Calendar.TUESDAY -> "Tue"
    Calendar.WEDNESDAY -> "Wed"
    Calendar.THURSDAY -> "Thu"
    Calendar.FRIDAY -> "Fri"
    Calendar.SATURDAY -> "Sat"
    Calendar.SUNDAY -> "Sun"
    else -> day.toString()
}
