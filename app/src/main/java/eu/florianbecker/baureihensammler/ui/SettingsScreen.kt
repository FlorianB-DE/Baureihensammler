package eu.florianbecker.baureihensammler.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    blockExternalWikiSummaries: Boolean,
    onBlockExternalWikiSummariesChange: (Boolean) -> Unit,
    debugModeEnabled: Boolean,
    onDebugModeEnabledChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Einstellungen",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Hier kannst du die App anpassen. Weitere Optionen können folgen.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        "Datenschutz/Offline Modus",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Deaktiviert die von extern geladenen Informationstexte.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Switch(
                    checked = blockExternalWikiSummaries,
                    onCheckedChange = onBlockExternalWikiSummariesChange
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        "Debug-Menü",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Blendet den Logs-Reiter im Menü ein.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Switch(
                    checked = debugModeEnabled,
                    onCheckedChange = onDebugModeEnabledChange
                )
            }
        }
    }
}
