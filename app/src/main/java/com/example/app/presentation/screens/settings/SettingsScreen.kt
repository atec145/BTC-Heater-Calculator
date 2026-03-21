package com.example.app.presentation.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app.domain.model.MinerConfig
import com.example.app.domain.model.SaleMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Miner-Sektion
            MinerSection(
                miners = uiState.miners,
                onAddMiner = { viewModel.showAddMinerDialog() },
                onEditMiner = { viewModel.showEditMinerDialog(it) },
                onDeleteMiner = { viewModel.deleteMiner(it) },
                onToggleActive = { viewModel.toggleMinerActive(it) }
            )

            // Verkaufsmodus
            SaleModeSection(
                saleMode = uiState.saleMode,
                hodlTargetInput = uiState.hodlTargetInput,
                onSaleModeChanged = { viewModel.onSaleModeChanged(it) },
                onHodlTargetChanged = { viewModel.onHodlTargetChanged(it) }
            )

            // Heizungseinstellungen
            HeatingSection(
                oilPriceInput = uiState.oilPriceInput,
                boilerEfficiencyInput = uiState.boilerEfficiencyInput,
                onOilPriceChanged = { viewModel.onOilPriceChanged(it) },
                onBoilerEfficiencyChanged = { viewModel.onBoilerEfficiencyChanged(it) }
            )

            Button(
                onClick = {
                    viewModel.saveSettings()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Speichern")
            }
        }
    }

    if (uiState.showMinerDialog) {
        MinerDialog(
            existing = uiState.editingMiner,
            minerCount = uiState.miners.size,
            onDismiss = { viewModel.dismissMinerDialog() },
            onSave = { label, hashrate, watt, active ->
                viewModel.saveMiner(label, hashrate, watt, active)
            }
        )
    }
}

@Composable
private fun MinerSection(
    miners: List<MinerConfig>,
    onAddMiner: () -> Unit,
    onEditMiner: (MinerConfig) -> Unit,
    onDeleteMiner: (Int) -> Unit,
    onToggleActive: (MinerConfig) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Miner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (miners.size < 2) {
                    IconButton(onClick = onAddMiner) {
                        Icon(Icons.Default.Add, contentDescription = "Miner hinzufügen")
                    }
                }
            }
            if (miners.isEmpty()) {
                Text("Noch kein Miner konfiguriert.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                miners.forEachIndexed { i, miner ->
                    if (i > 0) HorizontalDivider()
                    MinerRow(miner, onEdit = { onEditMiner(miner) }, onDelete = { onDeleteMiner(miner.id) }, onToggle = { onToggleActive(miner) })
                }
            }
        }
    }
}

@Composable
private fun MinerRow(miner: MinerConfig, onEdit: () -> Unit, onDelete: () -> Unit, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(miner.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("${miner.hashrateThs} TH/s  ·  ${miner.watt.toInt()} W", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = miner.isActive, onCheckedChange = { onToggle() })
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Bearbeiten") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Löschen") }
    }
}

@Composable
private fun SaleModeSection(
    saleMode: SaleMode,
    hodlTargetInput: String,
    onSaleModeChanged: (Boolean) -> Unit,
    onHodlTargetChanged: (String) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Verkaufsmodus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (saleMode is SaleMode.Hodl) "Hodl-Modus" else "Sofortverkauf")
                Switch(checked = saleMode is SaleMode.Hodl, onCheckedChange = onSaleModeChanged)
            }
            if (saleMode is SaleMode.Hodl) {
                OutlinedTextField(
                    value = hodlTargetInput,
                    onValueChange = onHodlTargetChanged,
                    label = { Text("Ziel-BTC-Preis (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (saleMode.targetPriceEur > 0 && hodlTargetInput.toDoubleOrNull() != null) {
                    // Kein Hinweis nötig wenn Zielpreis valide
                } else if (hodlTargetInput.isNotEmpty() && hodlTargetInput.toDoubleOrNull() == null) {
                    Text("Ungültige Zahl", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun HeatingSection(
    oilPriceInput: String,
    boilerEfficiencyInput: String,
    onOilPriceChanged: (String) -> Unit,
    onBoilerEfficiencyChanged: (String) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ölheizung", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = oilPriceInput,
                onValueChange = onOilPriceChanged,
                label = { Text("Heizölpreis (€/Liter)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = boilerEfficiencyInput,
                onValueChange = onBoilerEfficiencyChanged,
                label = { Text("Wirkungsgrad (%, Standard: 85)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun MinerDialog(
    existing: MinerConfig?,
    minerCount: Int,
    onDismiss: () -> Unit,
    onSave: (label: String, hashrateThs: Double, watt: Double, isActive: Boolean) -> Unit
) {
    var label by remember { mutableStateOf(existing?.label ?: "Miner ${minerCount + 1}") }
    var hashrate by remember { mutableStateOf(existing?.hashrateThs?.toString() ?: "") }
    var watt by remember { mutableStateOf(existing?.watt?.toString() ?: "") }
    var isActive by remember { mutableStateOf(existing?.isActive ?: true) }

    val isValid = label.isNotBlank() && hashrate.toDoubleOrNull()?.let { it > 0 } == true && watt.toDoubleOrNull()?.let { it > 0 } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Miner hinzufügen" else "Miner bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(
                    value = hashrate, onValueChange = { hashrate = it },
                    label = { Text("Hashrate (TH/s)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = watt, onValueChange = { watt = it },
                    label = { Text("Leistung (Watt)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Aktiv")
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(label, hashrate.toDouble(), watt.toDouble(), isActive) },
                enabled = isValid
            ) { Text("Speichern") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}
