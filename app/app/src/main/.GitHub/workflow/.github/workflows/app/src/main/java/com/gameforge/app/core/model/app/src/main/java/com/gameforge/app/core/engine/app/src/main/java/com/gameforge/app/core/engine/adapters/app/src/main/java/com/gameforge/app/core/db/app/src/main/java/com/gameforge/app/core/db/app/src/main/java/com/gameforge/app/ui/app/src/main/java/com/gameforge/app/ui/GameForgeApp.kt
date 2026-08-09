package com.gameforge.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gameforge.app.core.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameForgeMainScreen(viewModel: GameForgeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GameForge", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadGameData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = null) },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Layers, contentDescription = null) },
                    label = { Text("Level Editor") }
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Code, contentDescription = null) },
                    label = { Text("Dev Mode") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            GameHeaderCard(
                manifest = uiState.selectedGame,
                status = uiState.connectionStatus
            )

            if (uiState.connectionStatus == ConnectionStatus.UNSUPPORTED) {
                UnsupportedWarningCard()
            } else {
                when (currentTab) {
                    0 -> CustomizationDashboard(uiState, viewModel)
                    1 -> LevelEditorView()
                    2 -> DeveloperModeView()
                }
            }
        }
    }
}

@Composable
fun GameHeaderCard(manifest: GameManifest?, status: ConnectionStatus) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = manifest?.title ?: "No Game Loaded",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${manifest?.engine ?: "Unknown"} | ${manifest?.gameVersion ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(status = status)
        }
    }
}

@Composable
fun StatusChip(status: ConnectionStatus) {
    val (color, label) = when (status) {
        ConnectionStatus.CONNECTED -> Color(0xFF2E7D32) to "Connected"
        ConnectionStatus.DISCONNECTED -> Color.Gray to "Disconnected"
        ConnectionStatus.UNSUPPORTED -> Color(0xFFC62828) to "Unsupported"
        ConnectionStatus.COMPATIBILITY_WARNING -> Color(0xFFEF6C00) to "Warning"
    }

    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(
            text = label,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun UnsupportedWarningCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Unsupported Game or Build",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Unsupported — no compatible integration available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun CustomizationDashboard(uiState: GameForgeUiState, viewModel: GameForgeViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ParameterCategory.values()) { category ->
                FilterChip(
                    selected = uiState.selectedCategory == category,
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category.name) }
                )
            }
        }

        val filteredParams = uiState.parameters.filter { it.category == uiState.selectedCategory }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            items(filteredParams) { param ->
                ParameterControlItem(param = param, onValueChanged = { newValue ->
                    viewModel.onParameterChanged(param.id, newValue)
                })
            }
        }

        Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.resetChanges() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("RESET")
                }

                Button(
                    onClick = { viewModel.applyChanges() },
                    modifier = Modifier.weight(1f),
                    enabled = uiState.modifiedParameters.isNotEmpty()
                ) {
                    Text("APPLY (${uiState.modifiedParameters.size})")
                }
            }
        }
    }
}

@Composable
fun ParameterControlItem(param: GameParameter, onValueChanged: (ParameterValue) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(param.displayName, fontWeight = FontWeight.SemiBold)
                Text(text = formatVal(param.currentValue), color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (val v = param.currentValue) {
                is ParameterValue.IntVal -> {
                    var sliderPos by remember(param.id) { mutableStateOf(v.value.toFloat()) }
                    val min = param.validationRule.minValue?.toFloat() ?: 0f
                    val max = param.validationRule.maxValue?.toFloat() ?: 100f

                    Slider(
                        value = sliderPos,
                        onValueChange = {
                            sliderPos = it
                            onValueChanged(ParameterValue.IntVal(it.toInt()))
                        },
                        valueRange = min..max
                    )
                }
                is ParameterValue.FloatVal -> {
                    var sliderPos by remember(param.id) { mutableStateOf(v.value) }
                    val min = param.validationRule.minValue?.toFloat() ?: 0f
                    val max = param.validationRule.maxValue?.toFloat() ?: 10f

                    Slider(
                        value = sliderPos,
                        onValueChange = {
                            sliderPos = it
                            onValueChanged(ParameterValue.FloatVal(it))
                        },
                        valueRange = min..max
                    )
                }
                is ParameterValue.BoolVal -> {
                    Switch(
                        checked = v.value,
                        onCheckedChange = { onValueChanged(ParameterValue.BoolVal(it)) }
                    )
                }
                is ParameterValue.StringVal -> {
                    OutlinedTextField(
                        value = v.value,
                        onValueChange = { onValueChanged(ParameterValue.StringVal(it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

fun formatVal(pv: ParameterValue): String = when (pv) {
    is ParameterValue.IntVal -> pv.value.toString()
    is ParameterValue.FloatVal -> String.format("%.2f", pv.value)
    is ParameterValue.BoolVal -> if (pv.value) "ON" else "OFF"
    is ParameterValue.StringVal -> pv.value
}

@Composable
fun LevelEditorView() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Level Control", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) { index ->
                Button(onClick = {}) { Text("Lvl ${index + 1}") }
            }
        }
    }
}

@Composable
fun DeveloperModeView() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Developer Adapter Creator", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = "{\n  \"adapterId\": \"com.mygame.test\"\n}",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }
}
