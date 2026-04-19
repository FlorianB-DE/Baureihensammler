package eu.florianbecker.baureihensammler.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.florianbecker.baureihensammler.data.TrainSeries
import eu.florianbecker.baureihensammler.data.TrainSeriesOrigin
import eu.florianbecker.baureihensammler.data.menuLabel
import eu.florianbecker.baureihensammler.data.plateAbbrev

@Composable
fun DirectoryScreen(
    catalog: List<TrainSeries>,
    selectedOrigin: TrainSeriesOrigin,
) {
    val colors = MaterialTheme.colorScheme
    val sorted = catalog.sortedWith(compareBy<TrainSeries>({ it.baureihe }, { it.name }))

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Verzeichnis",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            "${selectedOrigin.menuLabel()} - ${sorted.size} Baureihen",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sorted, key = { "${it.origin.name}:${it.baureihe}:${it.name}" }) { series ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val brWithAliases =
                            buildString {
                                append(series.baureihe)
                                if (series.aliases.isNotEmpty()) {
                                    append(", ")
                                    append(series.aliases.joinToString(", "))
                                }
                            }
                        Text(
                            "${series.origin.plateAbbrev()} · BR $brWithAliases",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(series.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${series.category} - Vmax ${series.vmaxKmh} km/h",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
