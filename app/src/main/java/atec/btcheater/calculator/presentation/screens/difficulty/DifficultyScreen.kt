package atec.btcheater.calculator.presentation.screens.difficulty

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import atec.btcheater.calculator.presentation.components.DifficultyLineChart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultyScreen(
    viewModel: DifficultyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Difficulty") },
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.error != null -> Column(
                Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Fehler beim Laden", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(uiState.error!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.load() }) { Text("Erneut versuchen") }
            }

            uiState.epochs.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { Text("Keine Daten verfügbar") }

            else -> Column(
                Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val latest = uiState.epochs.last()
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Aktuelle Difficulty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "%.2f T".format(latest.difficulty / 1_000_000_000_000.0),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Block ${latest.blockHeight} · ${dateFormat.format(Date(latest.timestamp * 1000))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Verlauf – letzte ${uiState.epochs.size} Epochen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "ca. ${uiState.epochs.size * 14} Tage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        DifficultyLineChart(epochs = uiState.epochs)
                    }
                }
            }
        }
    }
}
