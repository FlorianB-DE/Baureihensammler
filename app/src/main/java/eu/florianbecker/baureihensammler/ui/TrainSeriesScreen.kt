package eu.florianbecker.baureihensammler.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.florianbecker.baureihensammler.CameraCaptureActivity
import eu.florianbecker.baureihensammler.data.AlphaTrainSeriesRepository
import eu.florianbecker.baureihensammler.collection.CollectionEntry
import eu.florianbecker.baureihensammler.collection.collectionDateFormatter
import eu.florianbecker.baureihensammler.collection.loadCollection
import eu.florianbecker.baureihensammler.collection.loadPrivacyOfflineMode
import eu.florianbecker.baureihensammler.collection.saveCollection
import eu.florianbecker.baureihensammler.collection.savePrivacyOfflineMode
import eu.florianbecker.baureihensammler.search.calculatePoints
import eu.florianbecker.baureihensammler.search.findSeries
import eu.florianbecker.baureihensammler.ui.theme.BaureihensammlerTheme
import eu.florianbecker.baureihensammler.util.clearAllSnapshots
import eu.florianbecker.baureihensammler.util.deleteSnapshotFile
import eu.florianbecker.baureihensammler.util.openUrl
import java.io.File
import java.time.LocalDateTime
import kotlinx.coroutines.launch

private val MinHeightToShowStatsWithIme = 280.dp

@Composable
fun TrainSeriesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var currentView by rememberSaveable { mutableStateOf("search") }
    val collection = remember { mutableStateListOf<CollectionEntry>() }
    val validSeries = findSeries(query)
    val alreadyCollected =
        validSeries?.let { series -> collection.any { it.baureihe == series.baureihe } }
            ?: false
    val collectionSnapshotPath =
        validSeries?.let { series ->
            collection.firstOrNull { it.baureihe == series.baureihe }?.imagePath?.takeIf {
                it.isNotBlank()
            }
        }
    val hasCollectionPhoto = collectionSnapshotPath != null

    LaunchedEffect(Unit) {
        collection.clear()
        collection.addAll(loadCollection(context))
    }

    val takeSnapshotLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode != android.app.Activity.RESULT_OK)
                return@rememberLauncherForActivityResult
            val data = result.data ?: return@rememberLauncherForActivityResult
            val baureihe =
                data.getStringExtra(CameraCaptureActivity.EXTRA_BAUREIHE)
                    ?: return@rememberLauncherForActivityResult
            val imagePath =
                data.getStringExtra(CameraCaptureActivity.EXTRA_IMAGE_PATH)
                    ?: return@rememberLauncherForActivityResult
            if (!File(imagePath).exists()) return@rememberLauncherForActivityResult
            val idx = collection.indexOfFirst { it.baureihe == baureihe }
            if (idx >= 0) {
                val existing = collection[idx]
                collection[idx] = existing.copy(imagePath = imagePath)
                saveCollection(context, collection)
            }
        }

    val totalPoints = collection.sumOf { it.totalPoints }
    val progress =
        if (AlphaTrainSeriesRepository.items.isEmpty()) 0f
        else {
            collection.size.toFloat() / AlphaTrainSeriesRepository.items.size.toFloat()
        }

    val imeVisible = rememberImeVisible()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var blockExternalWikiSummaries by remember { mutableStateOf(loadPrivacyOfflineMode(context)) }

    BackHandler(
        enabled =
            drawerState.currentValue == DrawerValue.Open ||
                currentView == "collection" ||
                currentView == "settings"
    ) {
        when {
            drawerState.currentValue == DrawerValue.Open -> scope.launch { drawerState.close() }
            currentView == "collection" -> currentView = "search"
            currentView == "settings" -> currentView = "search"
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                AppDrawerNavigation(
                    currentView = currentView,
                    onNavigate = { destination ->
                        scope.launch {
                            drawerState.close()
                            currentView = destination
                        }
                    }
                )
            }
        }
    ) {
        BoxWithConstraints(
            modifier =
                modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .imePadding()
                    .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 4.dp)
        ) {
            val showStatsRow = !imeVisible || maxHeight >= MinHeightToShowStatsWithIme
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val searchScroll = rememberScrollState()
                val settingsScroll = rememberScrollState()
                Column(
                    modifier =
                        Modifier.weight(1f)
                            .fillMaxWidth()
                            .then(
                                when (currentView) {
                                    "search" -> Modifier.verticalScroll(searchScroll)
                                    "settings" -> Modifier.verticalScroll(settingsScroll)
                                    else -> Modifier
                                }
                            ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TopHeader(
                        currentView = currentView,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onSearchClick = { currentView = "search" },
                        onCollectionClick = { currentView = "collection" }
                    )

                    if (currentView == "search") {
                        SearchInputPlate(query = query, onQueryChange = { query = it })
                        SearchView(
                            validSeries = validSeries,
                            alreadyCollected = alreadyCollected,
                            hasCollectionPhoto = hasCollectionPhoto,
                            collectionSnapshotPath = collectionSnapshotPath,
                            imeVisible = imeVisible,
                            blockExternalWikiSummaries = blockExternalWikiSummaries,
                            onTakeSnapshot = {
                                val target = validSeries?.baureihe ?: return@SearchView
                                val intent = CameraCaptureActivity.createIntent(context, target)
                                takeSnapshotLauncher.launch(intent)
                            },
                            onToggleCollected = {
                                validSeries?.let { series ->
                                    val existingIndex =
                                        collection.indexOfFirst {
                                            it.baureihe == series.baureihe
                                        }
                                    if (existingIndex >= 0) {
                                        deleteSnapshotFile(collection[existingIndex].imagePath)
                                        collection.removeAt(existingIndex)
                                    } else {
                                        val now =
                                            LocalDateTime.now().format(collectionDateFormatter)
                                        val pointsGain = calculatePoints(series.fleetEstimate)
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
                    } else if (currentView == "collection") {
                        CollectionScreen(
                            collection = collection,
                            onResetCollection = {
                                clearAllSnapshots(collection)
                                collection.clear()
                                saveCollection(context, collection)
                            },
                            onDeletePhoto = { entry ->
                                deleteSnapshotFile(entry.imagePath)
                                val index =
                                    collection.indexOfFirst {
                                        it.baureihe == entry.baureihe
                                    }
                                if (index >= 0) {
                                    collection[index] = collection[index].copy(imagePath = null)
                                    saveCollection(context, collection)
                                }
                            }
                        )
                    } else {
                        SettingsScreen(
                            blockExternalWikiSummaries = blockExternalWikiSummaries,
                            onBlockExternalWikiSummariesChange = { v ->
                                blockExternalWikiSummaries = v
                                savePrivacyOfflineMode(context, v)
                            }
                        )
                    }
                }

                if (showStatsRow && currentView != "settings") {
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
}

@Preview(showBackground = true)
@Composable
fun TrainSeriesPreview() {
    BaureihensammlerTheme { TrainSeriesScreen() }
}
