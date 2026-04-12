package eu.florianbecker.baureihensammler.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.florianbecker.baureihensammler.collection.CollectionEntry
import eu.florianbecker.baureihensammler.data.TrainSeriesOrigin
import eu.florianbecker.baureihensammler.data.menuLabel
import eu.florianbecker.baureihensammler.data.plateAbbrev

@Composable
fun CollectionScreen(
    collection: List<CollectionEntry>,
    emptyFilterHintOrigin: TrainSeriesOrigin = TrainSeriesOrigin.DB,
    hasAnyCollectionEntry: Boolean = true,
    onResetCollection: () -> Unit,
    onDeletePhoto: (CollectionEntry) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    Text("Meine Sammlung", fontWeight = FontWeight.Bold, color = colors.onBackground)
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedButton(onClick = { showResetDialog = true }, enabled = hasAnyCollectionEntry) {
        Text("Sammlung zurucksetzen")
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Sammlung zurucksetzen?") },
            text = {
                Text(
                    "Bist du sicher? All dein Fortschritt wird geloscht. Diese Aktion kann nicht ruckgangig gemacht werden."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetCollection()
                    }
                ) { Text("Ja, loschen") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Abbrechen") }
            }
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    if (collection.isEmpty()) {
        Text(
            text =
                if (hasAnyCollectionEntry) {
                    "Keine Einträge für ${emptyFilterHintOrigin.menuLabel()} in der Sammlung."
                } else {
                    "Noch nichts gesammelt."
                },
            color = colors.onSurface
        )
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(collection.sortedByDescending { it.seenAt }) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "${entry.origin.plateAbbrev()} · BR ${entry.baureihe} — ${entry.name}",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("${entry.category} - Vmax ${entry.vmaxKmh} km/h")
                    Text("Gesammelt am: ${entry.seenAt}")
                    Text("Punkte: ${entry.totalPoints}")
                    entry.imagePath?.let { path ->
                        val bmp = BitmapFactory.decodeFile(path)
                        if (bmp != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Schnappschuss BR ${entry.baureihe}",
                                modifier = Modifier.fillMaxWidth().height(170.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(onClick = { onDeletePhoto(entry) }) {
                                Text("Foto loschen")
                            }
                        }
                    }
                }
            }
        }
    }
}
