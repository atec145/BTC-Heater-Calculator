package atec.btcheater.calculator.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import atec.btcheater.calculator.domain.model.ProfitabilityResult
import atec.btcheater.calculator.domain.model.SaleMode
import atec.btcheater.calculator.presentation.components.PriceBarChart
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BTC-Heizung v1.3.2") },
                actions = {
                    if (uiState.lastUpdated.isNotEmpty()) {
                        Text(
                            text = "Stand: ${uiState.lastUpdated}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Einstellungen")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingContent(Modifier.padding(paddingValues))
            uiState.error != null -> ErrorContent(
                message = uiState.error!!,
                onRetry = { viewModel.refresh() },
                modifier = Modifier.padding(paddingValues)
            )
            uiState.activeMiners.none { it.isActive } -> NoMinersContent(
                onNavigateToSettings = onNavigateToSettings,
                modifier = Modifier.padding(paddingValues)
            )
            else -> DashboardContent(
                uiState = uiState,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Fehler beim Laden", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Erneut versuchen") }
    }
}

@Composable
private fun NoMinersContent(onNavigateToSettings: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Kein Miner aktiv", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("Bitte konfiguriere mindestens einen Miner.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onNavigateToSettings) { Text("Miner konfigurieren") }
    }
}

@Composable
private fun DashboardContent(uiState: HomeUiState, modifier: Modifier = Modifier) {
    val result = uiState.result ?: return
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hodl-Hinweis wenn Zielpreis unter aktuellem Kurs
        val saleMode = uiState.saleMode
        if (saleMode is SaleMode.Hodl && saleMode.targetPriceEur < uiState.btcPriceEur && uiState.btcPriceEur > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Text(
                    text = "Hinweis: Ziel-BTC-Preis (%.0f €) liegt unter aktuellem Kurs (%.0f €)".format(
                        saleMode.targetPriceEur, uiState.btcPriceEur
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Hauptentscheidung
        DecisionCard(result)

        // Tagesübersicht Strompreise
        if (uiState.hourlyPrices.isNotEmpty()) {
            ChartCard(uiState, result)
        }

        // Kennzahlen
        MetricsCard(result)
    }
}

@Composable
private fun DecisionCard(result: ProfitabilityResult) {
    val color = if (result.isWorthIt) Color(0xFF2E7D32) else Color(0xFFC62828)
    val label = if (result.isWorthIt) "JA, lohnt sich!" else "NEIN, lohnt sich nicht"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LabelValue("Aktuell", "%.1f ct/kWh".format(result.currentStrompreisEurKwh * 100), Color.White)
                LabelValue("Schwelle", "%.1f ct/kWh".format(result.breakEvenStrompreisEurKwh * 100), Color.White)
                val deltaSign = if (result.deltaEurKwh >= 0) "+" else ""
                LabelValue("Delta", "$deltaSign%.1f ct/kWh".format(result.deltaEurKwh * 100), Color.White)
            }
        }
    }
}

@Composable
private fun MetricsCard(result: ProfitabilityResult) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Mining-Kennzahlen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            HorizontalDivider()
            MetricRow("Erw. BTC/Tag", "%.8f BTC".format(result.expectedBtcPerDay))
            MetricRow("Erw. Ertrag/Tag", "%.2f €".format(result.expectedEurPerDay))
            HorizontalDivider()
            val vorteilSign = if (result.nettovorteilProStunde >= 0) "+" else ""
            val vorteilColor = if (result.nettovorteilProStunde >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
            MetricRow(
                label = "Nettovorteil/h (vs. Öl)",
                value = "$vorteilSign%.3f €".format(result.nettovorteilProStunde),
                valueColor = vorteilColor
            )
            HorizontalDivider()
            MetricRow("Ölheizung kostet", "%.1f ct/kWh".format(result.heizungskostenOelEurKwh * 100))

            if (result.profitableHoursToday > 0) {
                HorizontalDivider()
                Text("Heute (${result.profitableHoursToday} profitable Stunden)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                MetricRow("Stromkosten", "%.2f €".format(result.stromkostenProfitableEur))
                MetricRow("BTC-Ertrag", "%.2f €".format(result.eurProfitableHours))
                MetricRow("Nettoertrag", "%.2f €".format(result.nettoProfitableEur))
                HorizontalDivider()
                Text("Heizleistung heute", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                MetricRow("Wärme in Puffer", "%.1f kWh".format(result.waermeEnergyKwhProfitable))
                MetricRow("Heizöl ersetzt", "%.2f L".format(result.oilLiterAvoided))
                MetricRow("Ersparnis vs. Öl", "%.2f €".format(result.oilEurSaved))
                MetricRow("CO₂ eingespart", "%.2f kg".format(result.co2KgAvoided))
                HorizontalDivider()
                Text("10L Wasser +10°C erwärmen", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                MetricRow("Mit Miner (jetzt)", "%.3f €".format(result.kostenWasser10L10K_Miner))
                MetricRow("Mit Ölheizung", "%.3f €".format(result.kostenWasser10L10K_Oel))
            }
        }
    }
}

@Composable
private fun ChartCard(uiState: HomeUiState, result: ProfitabilityResult) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Strompreise heute", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Grün = günstiger als Ölheizung",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            PriceBarChart(
                prices = uiState.hourlyPrices,
                breakEvenPrice = result.breakEvenStrompreisEurKwh
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

@Composable
private fun LabelValue(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}
