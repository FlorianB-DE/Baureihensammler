package eu.florianbecker.baureihensammler.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsRailway
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawerNavigation(
    currentView: String,
    debugModeEnabled: Boolean,
    onNavigate: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Navigation",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp)
        )
        HorizontalDivider()
        NavigationDrawerItem(
            label = { Text("Suche") },
            selected = currentView == "search",
            icon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            onClick = { onNavigate("search") },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text("Sammlung") },
            selected = currentView == "collection",
            icon = { Icon(Icons.Outlined.DirectionsRailway, contentDescription = null) },
            onClick = { onNavigate("collection") },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text("Verzeichnis") },
            selected = currentView == "directory",
            icon = { Icon(Icons.Outlined.MenuBook, contentDescription = null) },
            onClick = { onNavigate("directory") },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text("Einstellungen") },
            selected = currentView == "settings",
            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
            onClick = { onNavigate("settings") },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        if (debugModeEnabled) {
            NavigationDrawerItem(
                label = { Text("Logs") },
                selected = currentView == "logs",
                icon = { Icon(Icons.Outlined.List, contentDescription = null) },
                onClick = { onNavigate("logs") },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}
