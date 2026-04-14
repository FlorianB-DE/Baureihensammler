package eu.florianbecker.baureihensammler.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.florianbecker.baureihensammler.util.DebugLogEntry

@Composable
fun LogsScreen(logs: List<DebugLogEntry>) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var selectedLog by remember { mutableStateOf<DebugLogEntry?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Logs",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onBackground,
            fontWeight = FontWeight.Bold
        )
        if (logs.isEmpty()) {
            Text(
                "Keine Logs in dieser App-Sitzung.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Zeit", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("Quelle", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("Nachricht", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs, key = { it.fileName }) { entry ->
                            Row(
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .clickable { selectedLog = entry }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(entry.timestamp, modifier = Modifier.weight(1f))
                                Text(entry.source, modifier = Modifier.weight(1f))
                                Text(entry.message, modifier = Modifier.weight(2f))
                            }
                        }
                    }
                }
            }
        }
    }

    selectedLog?.let { entry ->
        AlertDialog(
            onDismissRequest = { selectedLog = null },
            title = { Text("Logeintrag") },
            text = {
                Text(
                    text = entry.fullText,
                    modifier = Modifier.clickable {
                        clipboard.setText(AnnotatedString(entry.fullText))
                        Toast.makeText(context, "Log in Zwischenablage kopiert", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(entry.fullText))
                        Toast.makeText(context, "Log in Zwischenablage kopiert", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Kopieren")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLog = null }) {
                    Text("Schließen")
                }
            }
        )
    }
}
