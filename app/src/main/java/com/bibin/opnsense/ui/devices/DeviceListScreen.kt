package com.bibin.opnsense.ui.devices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
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
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showBlockedOnly by viewModel.showBlockedOnly.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<Device?>(null) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            if (isSearchActive) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onClose = {
                        isSearchActive = false
                        viewModel.onSearchQueryChange("")
                        focusManager.clearFocus()
                    },
                    focusRequester = focusRequester,
                )
            } else {
                TopAppBar(
                    title = { Text("Devices") },
                    actions = {
                        FilterChip(
                            selected = showBlockedOnly,
                            onClick = { viewModel.toggleShowBlockedOnly() },
                            label = { Text("Blocked") },
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { viewModel.loadDevices() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    },
                )
            }
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
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
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

                state.devices.isEmpty() && searchQuery.isNotBlank() -> Text(
                    "No devices match \"$searchQuery\"",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    color = MaterialTheme.colorScheme.outline,
                )

                state.devices.isEmpty() && showBlockedOnly -> Text(
                    "No blocked devices",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.outline,
                )

                state.devices.isEmpty() -> Text(
                    "No devices found",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.outline,
                )

                else -> LazyColumn {
                    items(state.devices, key = { "${it.mac}-${it.ip}" }) { device ->
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

    // Auto-focus search field when search bar opens
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
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
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    focusRequester: FocusRequester,
) {
    Surface(shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close search")
            }
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search by name, IP or MAC…") },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* already filtered live */ }),
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        }
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
