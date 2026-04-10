package eu.florianbecker.baureihensammler

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsRailway
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Star
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import eu.florianbecker.baureihensammler.data.AlphaTrainSeriesRepository
import eu.florianbecker.baureihensammler.data.TrainSeries
import eu.florianbecker.baureihensammler.ui.theme.BaureihensammlerTheme
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            BaureihensammlerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TrainSeriesScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TrainSeriesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var currentView by rememberSaveable { mutableStateOf("search") }
    val collection = remember { mutableStateListOf<CollectionEntry>() }
    val validSeries = findSeries(query)
    val alreadyCollected = validSeries?.let { series ->
        collection.any { it.baureihe == series.baureihe }
    } ?: false
    val hasCollectionPhoto = validSeries?.let { series ->
        collection.firstOrNull { it.baureihe == series.baureihe }
            ?.imagePath
            ?.isNotBlank() == true
    } ?: false

    LaunchedEffect(Unit) {
        collection.clear()
        collection.addAll(loadCollection(context))
    }

    val takeSnapshotLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != android.app.Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val data = result.data ?: return@rememberLauncherForActivityResult
        val baureihe = data.getStringExtra(CameraCaptureActivity.EXTRA_BAUREIHE) ?: return@rememberLauncherForActivityResult
        val imagePath = data.getStringExtra(CameraCaptureActivity.EXTRA_IMAGE_PATH) ?: return@rememberLauncherForActivityResult
        if (!File(imagePath).exists()) return@rememberLauncherForActivityResult
        val idx = collection.indexOfFirst { it.baureihe == baureihe }
        if (idx >= 0) {
            val existing = collection[idx]
            collection[idx] = existing.copy(imagePath = imagePath)
            saveCollection(context, collection)
        }
    }

    val totalPoints = collection.sumOf { it.totalPoints }
    val progress = if (AlphaTrainSeriesRepository.items.isEmpty()) 0f else {
        collection.size.toFloat() / AlphaTrainSeriesRepository.items.size.toFloat()
    }

    BackHandler(enabled = currentView == "collection") {
        currentView = "search"
    }

    val imeVisible = rememberImeVisible()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 4.dp)
    ) {
        val showStatsRow = !imeVisible || maxHeight >= MinHeightToShowStatsWithIme
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TopHeader(
                    isCollection = currentView == "collection",
                    onSearchClick = { currentView = "search" },
                    onCollectionClick = { currentView = "collection" }
                )

                if (currentView == "search") {
                    SearchInputPlate(query = query, onQueryChange = { query = it })
                    SearchView(
                        validSeries = validSeries,
                        alreadyCollected = alreadyCollected,
                        hasCollectionPhoto = hasCollectionPhoto,
                        imeVisible = imeVisible,
                        onTakeSnapshot = {
                            val target = validSeries?.baureihe ?: return@SearchView
                            val intent = CameraCaptureActivity.createIntent(context, target)
                            takeSnapshotLauncher.launch(intent)
                        },
                        onSaveCollected = {
                            validSeries?.let { series ->
                                val now = LocalDateTime.now().format(DATE_FORMATTER)
                                val existingIndex = collection.indexOfFirst { it.baureihe == series.baureihe }
                                val pointsGain = calculatePoints(series.fleetEstimate)
                                if (existingIndex >= 0) {
                                    val existing = collection[existingIndex]
                                    collection[existingIndex] = existing.copy(
                                        seenAt = now,
                                        totalPoints = existing.totalPoints + pointsGain
                                    )
                                } else {
                                    collection.add(
                                        CollectionEntry(
                                            baureihe = series.baureihe,
                                            name = series.name,
                                            category = series.category,
                                            vmaxKmh = series.vmaxKmh,
                                            fleetEstimate = series.fleetEstimate,
                                            seenAt = now,
                                            totalPoints = pointsGain,
                                            imagePath = null
                                        )
                                    )
                                }
                                saveCollection(context, collection)
                            }
                        },
                        onOpenWiki = { url -> openUrl(context, url) }
                    )
                } else {
                    CollectionView(
                        collection = collection,
                        onResetCollection = {
                            clearAllSnapshots(collection)
                            collection.clear()
                            saveCollection(context, collection)
                        },
                        onDeletePhoto = { entry ->
                            deleteSnapshotFile(entry.imagePath)
                            val index = collection.indexOfFirst { it.baureihe == entry.baureihe }
                            if (index >= 0) {
                                collection[index] = collection[index].copy(imagePath = null)
                                saveCollection(context, collection)
                            }
                        }
                    )
                }
            }

            if (showStatsRow) {
                StatsRow(
                    points = totalPoints,
                    discovered = collection.size,
                    total = AlphaTrainSeriesRepository.items.size,
                    progress = progress
                )
            }
        }
    }
}

@Composable
private fun TopHeader(
    isCollection: Boolean,
    onSearchClick: () -> Unit,
    onCollectionClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Menu, contentDescription = "Menu", tint = colors.onBackground)
            }
            Text("Baureihensammler", color = colors.onBackground, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = if (isCollection) onSearchClick else onCollectionClick) {
            Icon(
                if (isCollection) Icons.Outlined.Search else Icons.Outlined.DirectionsRailway,
                contentDescription = "View switch",
                tint = colors.onBackground
            )
        }
    }
}

@Composable
private fun rememberImeVisible(): Boolean {
    val view = LocalView.current
    var imeVisible by remember { mutableStateOf(false) }
    DisposableEffect(view) {
        val root = view.rootView
        fun update() {
            val insets = ViewCompat.getRootWindowInsets(root)
            imeVisible = insets?.isVisible(WindowInsetsCompat.Type.ime()) == true
        }
        val listener = android.view.ViewTreeObserver.OnGlobalLayoutListener { update() }
        root.viewTreeObserver.addOnGlobalLayoutListener(listener)
        root.post { update() }
        onDispose { root.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }
    return imeVisible
}

@Composable
private fun SearchInputPlate(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val ghostSuffix = ghostBaureiheSuffix(query)
    val contentPadding = OutlinedTextFieldDefaults.contentPadding()
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Transparent,
        unfocusedTextColor = Color.Transparent,
        disabledTextColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedPlaceholderColor = colors.onSurfaceVariant.copy(alpha = 0.55f),
        unfocusedPlaceholderColor = colors.onSurfaceVariant.copy(alpha = 0.55f),
        cursorColor = colors.primary,
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.outline,
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, colors.outline, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Baureihe", color = colors.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            Box(Modifier.fillMaxWidth()) {
                if (query.isNotEmpty()) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colors.onSurface)) {
                                append(query)
                            }
                            if (ghostSuffix.isNotEmpty()) {
                                withStyle(SpanStyle(color = colors.onSurfaceVariant.copy(alpha = 0.5f))) {
                                    append(ghostSuffix)
                                }
                            }
                        },
                        style = LocalTextStyle.current,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(contentPadding)
                            .wrapContentWidth()
                    )
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("z. B. 401") },
                    colors = fieldColors,
                )
            }
        }
    }
}

@Composable
private fun SearchView(
    validSeries: TrainSeries?,
    alreadyCollected: Boolean,
    hasCollectionPhoto: Boolean,
    imeVisible: Boolean,
    onTakeSnapshot: () -> Unit,
    onSaveCollected: () -> Unit,
    onOpenWiki: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var showMoreInfos by rememberSaveable(validSeries?.baureihe) { mutableStateOf(true) }
    val detailsVisible = !imeVisible && showMoreInfos

    validSeries?.let { series ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = series.name,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onSaveCollected,
                        enabled = !alreadyCollected,
                        modifier = Modifier.widthIn(min = 152.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Star, contentDescription = null)
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = if (alreadyCollected) "Gespeichert" else "Speichern",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (imeVisible) {
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                            } else {
                                showMoreInfos = !showMoreInfos
                            }
                        }
                    ) {
                        Icon(
                            Icons.Outlined.KeyboardArrowDown,
                            contentDescription = if (detailsVisible) "Infos ausblenden" else "Mehr Infos",
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.rotate(if (detailsVisible) 180f else 0f)
                        )
                    }
                    if (alreadyCollected) {
                        OutlinedButton(onClick = onTakeSnapshot) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.CameraAlt, contentDescription = null)
                                Text(if (hasCollectionPhoto) "Ändern" else "Hinzufügen")
                            }
                        }
                    }
                }
                if (detailsVisible) {
                    Text("${series.category} - Vmax ${series.vmaxKmh} km/h")
                    Text("Haufigkeit (Schatzung): ${series.fleetEstimate} Fahrzeuge")
                    Text("Punkte beim Markieren: ${calculatePoints(series.fleetEstimate)}")
                    OutlinedButton(onClick = { onOpenWiki(series.wikiArticleUrl) }) {
                        Text("Wikipedia")
                    }
                }
            }
        }
    } ?: Text("Bei gültiger Baureihe erscheinen die Infos direkt.", color = colors.onSurface)
}

@Composable
private fun CollectionView(
    collection: List<CollectionEntry>,
    onResetCollection: () -> Unit,
    onDeletePhoto: (CollectionEntry) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    Text("Meine Sammlung", fontWeight = FontWeight.Bold, color = colors.onBackground)
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedButton(
        onClick = { showResetDialog = true },
        enabled = collection.isNotEmpty()
    ) {
        Text("Sammlung zurucksetzen")
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Sammlung zurucksetzen?") },
            text = {
                Text("Bist du sicher? All dein Fortschritt wird geloscht. Diese Aktion kann nicht ruckgangig gemacht werden.")
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
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    if (collection.isEmpty()) {
        Text("Noch nichts gesammelt.", color = colors.onSurface)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(collection.sortedByDescending { it.seenAt }) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("BR ${entry.baureihe} - ${entry.name}", fontWeight = FontWeight.SemiBold)
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp),
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

@Composable
private fun StatsRow(
    points: Int,
    discovered: Int,
    total: Int,
    progress: Float
) {
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
private fun StatChip(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.Bold)
            Text(title, color = colors.onSurfaceVariant)
        }
    }
}

private fun normalizedBaureiheQuery(raw: String): String {
    return raw.trim().removePrefix("BR ").removePrefix("br ")
}

private fun ghostBaureiheSuffix(raw: String): String {
    val normalized = normalizedBaureiheQuery(raw)
    if (normalized.isEmpty()) return ""
    val candidates = AlphaTrainSeriesRepository.items.filter { series ->
        series.baureihe.contains('.') &&
            series.baureihe.startsWith(normalized, ignoreCase = true) &&
            !series.baureihe.equals(normalized, ignoreCase = true)
    }
    if (candidates.size != 1) return ""
    val full = candidates.first().baureihe
    return full.substring(normalized.length)
}

private fun findSeries(query: String): TrainSeries? {
    val cleanedQuery = normalizedBaureiheQuery(query)
    return AlphaTrainSeriesRepository.items.firstOrNull { series ->
        series.baureihe.equals(cleanedQuery, ignoreCase = true)
    }
}

private data class CollectionEntry(
    val baureihe: String,
    val name: String,
    val category: String,
    val vmaxKmh: Int,
    val fleetEstimate: Int,
    val seenAt: String,
    val totalPoints: Int,
    val imagePath: String?
)

private fun calculatePoints(fleetEstimate: Int): Int {
    return (1200 / fleetEstimate.coerceAtLeast(20)).coerceAtLeast(1)
}

private fun deleteSnapshotFile(path: String?) {
    if (path.isNullOrBlank()) return
    val file = File(path)
    if (file.exists()) {
        file.delete()
    }
}

private fun clearAllSnapshots(collection: List<CollectionEntry>) {
    collection.forEach { entry ->
        deleteSnapshotFile(entry.imagePath)
    }
}

private fun saveCollection(context: Context, items: List<CollectionEntry>) {
    val array = JSONArray()
    items.forEach { item ->
        val obj = JSONObject()
            .put("baureihe", item.baureihe)
            .put("name", item.name)
            .put("category", item.category)
            .put("vmaxKmh", item.vmaxKmh)
            .put("fleetEstimate", item.fleetEstimate)
            .put("seenAt", item.seenAt)
            .put("totalPoints", item.totalPoints)
            .put("imagePath", item.imagePath)
        array.put(obj)
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_COLLECTION, array.toString())
        .apply()
}

private fun loadCollection(context: Context): List<CollectionEntry> {
    val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_COLLECTION, null)
        ?: return emptyList()
    val array = JSONArray(raw)
    val items = mutableListOf<CollectionEntry>()
    for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        items.add(
            CollectionEntry(
                baureihe = obj.getString("baureihe"),
                name = obj.getString("name"),
                category = obj.getString("category"),
                vmaxKmh = obj.getInt("vmaxKmh"),
                fleetEstimate = obj.getInt("fleetEstimate"),
                seenAt = obj.getString("seenAt"),
                totalPoints = obj.getInt("totalPoints"),
                imagePath = obj.optString("imagePath").ifBlank { null }
            )
        )
    }
    return items
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun TrainSeriesPreview() {
    BaureihensammlerTheme {
        TrainSeriesScreen()
    }
}

private val MinHeightToShowStatsWithIme = 280.dp

private const val PREFS_NAME = "baureihen_prefs"
private const val KEY_COLLECTION = "collection_entries"
private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")