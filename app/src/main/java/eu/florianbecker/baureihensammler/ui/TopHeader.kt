package eu.florianbecker.baureihensammler.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsRailway
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TopHeader(
    currentView: String,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCollectionClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val showSearchIcon = currentView == "collection" || currentView == "settings"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Outlined.Menu, contentDescription = "Menü", tint = colors.onBackground)
            }
            Text("Baureihensammler", color = colors.onBackground, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = if (showSearchIcon) onSearchClick else onCollectionClick) {
            Icon(
                if (showSearchIcon) Icons.Outlined.Search else Icons.Outlined.DirectionsRailway,
                contentDescription = if (showSearchIcon) "Zur Suche" else "Zur Sammlung",
                tint = colors.onBackground
            )
        }
    }
}
