package eu.florianbecker.baureihensammler.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatsRow(points: Int, discovered: Int, total: Int, progress: Float) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatChip(
            modifier = Modifier.weight(1f),
            title = "Punkte",
            value = points.toString(),
            color = Color(0xFFFBC02D)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.size(72.dp),
                strokeWidth = 7.dp,
                trackColor = colors.surfaceVariant,
                color = colors.primary
            )
            Text("$discovered/$total", color = colors.onBackground)
        }
        Spacer(modifier = Modifier.width(20.dp))
        StatChip(
            modifier = Modifier.weight(1f),
            title = "Sammlung",
            value = discovered.toString(),
            color = Color(0xFF42A5F5)
        )
    }
}

@Composable
private fun StatChip(modifier: Modifier = Modifier, title: String, value: String, color: Color) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = colors.surfaceVariant) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.Bold)
            Text(title, color = colors.onSurfaceVariant)
        }
    }
}
