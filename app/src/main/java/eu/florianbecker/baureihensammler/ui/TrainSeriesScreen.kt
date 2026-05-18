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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import eu.florianbecker.baureihensammler.data.TrainSeriesOrigin
import eu.florianbecker.baureihensammler.collection.CollectionEntry
import eu.florianbecker.baureihensammler.collection.collectionDateFormatter
import eu.florianbecker.baureihensammler.collection.loadCollection
import eu.florianbecker.baureihensammler.collection.loadDebugMode
import eu.florianbecker.baureihensammler.collection.loadPrivacyOfflineMode
import eu.florianbecker.baureihensammler.collection.loadOnboardingShown
import eu.florianbecker.baureihensammler.collection.loadPrivacyTooltipShown
import eu.florianbecker.baureihensammler.collection.saveOnboardingShown
import eu.florianbecker.baureihensammler.collection.saveCollection
import eu.florianbecker.baureihensammler.collection.saveDebugMode
import eu.florianbecker.baureihensammler.collection.savePrivacyOfflineMode
import eu.florianbecker.baureihensammler.collection.savePrivacyTooltipShown
import eu.florianbecker.baureihensammler.search.calculatePoints
import eu.florianbecker.baureihensammler.search.catalogForOrigin
import eu.florianbecker.baureihensammler.search.findSeries
import eu.florianbecker.baureihensammler.search.matchesStoredBaureihe
import eu.florianbecker.baureihensammler.search.needsOverlapVehicleField
import eu.florianbecker.baureihensammler.ui.theme.BaureihensammlerTheme
import eu.florianbecker.baureihensammler.util.clearAllSnapshots
import eu.florianbecker.baureihensammler.util.deleteSnapshotFile
import eu.florianbecker.baureihensammler.util.DebugLogStore
import eu.florianbecker.baureihensammler.util.openUrl
import java.io.File
import java.time.LocalDateTime
import android.widget.Toast
import kotlinx.coroutines.launch

private val MinHeightToShowStatsWithIme = 280.dp

@Composable
fun TrainSeriesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var vehicleQuery by rememberSaveable { mutableStateOf("") }
    var currentView by rememberSaveable { mutableStateOf("search") }
    var selectedOriginName by rememberSaveable { mutableStateOf(TrainSeriesOrigin.DB.name) }
    val selectedOrigin = TrainSeriesOrigin.fromName(selectedOriginName)
    val collection = remember { mutableStateListOf<CollectionEntry>() }
    LaunchedEffect(query, selectedOrigin) {
        if (!needsOverlapVehicleField(query, selectedOrigin)) {
            vehicleQuery = ""
        }
    }

    val validSeries =
        findSeries(
            brQuery = query,
            vehicleQuery = vehicleQuery.ifBlank { null },
            origin = selectedOrigin
        )
    val overlapVehicleHint =
        needsOverlapVehicleField(query, selectedOrigin) && query.isNotBlank() &&
            vehicleQuery.isBlank()
    val alreadyCollected =
        validSeries?.let { series ->
            collection.any { series.matchesStoredBaureihe(it.baureihe) && it.origin == series.origin }
        }
            ?: false
    val collectionSnapshotPath =
        validSeries?.let { series ->
            collection
                .firstOrNull { series.matchesStoredBaureihe(it.baureihe) && it.origin == series.origin }
                ?.imagePath
                ?.takeIf { it.isNotBlank() }
        }
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
            val photoOrigin =
                TrainSeriesOrigin.fromName(data.getStringExtra(CameraCaptureActivity.EXTRA_ORIGIN))
            val imagePath =
                data.getStringExtra(CameraCaptureActivity.EXTRA_IMAGE_PATH)
                    ?: return@rememberLauncherForActivityResult
            if (!File(imagePath).exists()) return@rememberLauncherForActivityResult
            val matchedSeries =
                findSeries(
                    brQuery = baureihe,
                    vehicleQuery = null,
                    origin = photoOrigin
                ) ?: return@rememberLauncherForActivityResult
            val idx =
                collection.indexOfFirst {
                    it.origin == photoOrigin && matchedSeries.matchesStoredBaureihe(it.baureihe)
                }
            if (idx >= 0) {
                val existing = collection[idx]
                collection[idx] = existing.copy(imagePath = imagePath)
                saveCollection(context, collection)
            }
        }

    val collectionForSelectedOrigin = collection.filter { it.origin == selectedOrigin }
    val catalogSize = catalogForOrigin(selectedOrigin).size
    val totalPoints = collectionForSelectedOrigin.sumOf { it.totalPoints }
    val discoveredForOrigin = collectionForSelectedOrigin.size
    val progress =
        if (catalogSize == 0) 0f
        else discoveredForOrigin.toFloat() / catalogSize.toFloat()

    val imeVisible = rememberImeVisible()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var blockExternalWikiSummaries by remember { mutableStateOf(loadPrivacyOfflineMode(context)) }
    var debugModeEnabled by remember { mutableStateOf(loadDebugMode(context)) }
    var showOnboarding by remember { mutableStateOf(!loadOnboardingShown(context)) }
    var showPrivacyTooltip by remember { mutableStateOf(false) }
    var highlightPrivacySetting by remember { mutableStateOf(false) }

    fun dismissOnboarding() {
        showOnboarding = false
        saveOnboardingShown(context, true)
        if (!loadPrivacyTooltipShown(context)) {
            showPrivacyTooltip = true
        }
    }

    LaunchedEffect(Unit) {
        if (loadOnboardingShown(context) && !loadPrivacyTooltipShown(context)) {
            showPrivacyTooltip = true
        }
    }

    BackHandler(
        enabled =
            drawerState.currentValue == DrawerValue.Open ||
                currentView == "collection" ||
                currentView == "directory" ||
                currentView == "feedback" ||
                currentView == "settings" ||
                currentView == "logs" ||
                currentView == "info"
    ) {
        when {
            drawerState.currentValue == DrawerValue.Open -> scope.launch { drawerState.close() }
            currentView == "collection" -> currentView = "search"
            currentView == "directory" -> currentView = "search"
            currentView == "info" -> currentView = "search"
            currentView == "feedback" -> currentView = "search"
            currentView == "settings" -> currentView = "search"
            currentView == "logs" -> currentView = "search"
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                AppDrawerNavigation(
                    currentView = currentView,
                    debugModeEnabled = debugModeEnabled,
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
                val feedbackScroll = rememberScrollState()
                val infoScroll = rememberScrollState()
                Column(
                    modifier =
                        Modifier.weight(1f)
                            .fillMaxWidth()
                            .then(
                                when (currentView) {
                                    "search" -> Modifier.verticalScroll(searchScroll)
                                    "settings" -> Modifier.verticalScroll(settingsScroll)
                                    "feedback" -> Modifier.verticalScroll(feedbackScroll)
                                    "info" -> Modifier.verticalScroll(infoScroll)
                                    else -> Modifier
                                }
                            ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TopHeader(
                        currentView = currentView,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onSearchClick = { currentView = "search" },
                        onCollectionClick = { currentView = "collection" },
                        onInfoClick = { currentView = "info" },
                    )

                    if (currentView == "search") {
                        SearchInputPlate(
                            query = query,
                            onQueryChange = { query = it },
                            vehicleQuery = vehicleQuery,
                            onVehicleQueryChange = { vehicleQuery = it },
                            showVehicleSlot = needsOverlapVehicleField(query, selectedOrigin),
                            selectedOrigin = selectedOrigin,
                            onOriginChange = { origin ->
                                selectedOriginName = origin.name
                                query = ""
                                vehicleQuery = ""
                            }
                        )
                        SearchView(
                            validSeries = validSeries,
                            overlapVehicleHint = overlapVehicleHint,
                            alreadyCollected = alreadyCollected,
                            collectionSnapshotPath = collectionSnapshotPath,
                            imeVisible = imeVisible,
                            blockExternalWikiSummaries = blockExternalWikiSummaries,
                            onTakeSnapshot = {
                                val series = validSeries ?: return@SearchView
                                try {
                                    val intent =
                                        CameraCaptureActivity.createIntent(
                                            context,
                                            series.baureihe,
                                            series.origin,
                                            fromGallery = false,
                                        )
                                    takeSnapshotLauncher.launch(intent)
                                } catch (t: Throwable) {
                                    DebugLogStore.logError(
                                        context = context,
                                        source = "TrainSeriesScreen.onTakeSnapshot",
                                        message = "Kamera konnte nicht gestartet werden (BR=${series.baureihe}).",
                                        throwable = t
                                    )
                                    Toast.makeText(
                                        context,
                                        "Kamera konnte nicht gestartet werden.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onPickSnapshotFromGallery = {
                                val series = validSeries ?: return@SearchView
                                try {
                                    val intent =
                                        CameraCaptureActivity.createIntent(
                                            context,
                                            series.baureihe,
                                            series.origin,
                                            fromGallery = true,
                                        )
                                    takeSnapshotLauncher.launch(intent)
                                } catch (t: Throwable) {
                                    DebugLogStore.logError(
                                        context = context,
                                        source = "TrainSeriesScreen.onPickSnapshotFromGallery",
                                        message =
                                            "Galerie konnte nicht geöffnet werden (BR=${series.baureihe}).",
                                        throwable = t
                                    )
                                    Toast.makeText(
                                        context,
                                        "Galerie konnte nicht geöffnet werden.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onToggleCollected = {
                                validSeries?.let { series ->
                                    val existingIndex =
                                        collection.indexOfFirst {
                                            series.matchesStoredBaureihe(it.baureihe) &&
                                                it.origin == series.origin
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
                                                imagePath = null,
                                                origin = series.origin
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
                            collection = collectionForSelectedOrigin,
                            emptyFilterHintOrigin = selectedOrigin,
                            hasAnyCollectionEntry = collection.isNotEmpty(),
                            onResetCollection = {
                                clearAllSnapshots(collection)
                                collection.clear()
                                saveCollection(context, collection)
                            },
                            onDeletePhoto = { entry ->
                                deleteSnapshotFile(entry.imagePath)
                                val index =
                                    collection.indexOfFirst {
                                        it.baureihe == entry.baureihe &&
                                            it.origin == entry.origin
                                    }
                                if (index >= 0) {
                                    collection[index] = collection[index].copy(imagePath = null)
                                    saveCollection(context, collection)
                                }
                            }
                        )
                    } else if (currentView == "directory") {
                        DirectoryScreen(
                            catalog = catalogForOrigin(selectedOrigin),
                            selectedOrigin = selectedOrigin
                        )
                    } else if (currentView == "info") {
                        FahrzeugnummerInfoScreen(onOpenUrl = { url -> openUrl(context, url) })
                    } else if (currentView == "feedback") {
                        FeedbackScreen(
                            privacyModeEnabled = blockExternalWikiSummaries,
                            onOpenMail = { openUrl(context, "mailto:baureihensammler@florianbecker.eu") },
                            onOpenGitHub = { openUrl(context, "https://github.com/FlorianB-DE/Baureihensammler") },
                            onOpenSupport = { openUrl(context, "https://buymeacoffee.com/becker.software") }
                        )
                    } else if (currentView == "settings") {
                        SettingsScreen(
                            blockExternalWikiSummaries = blockExternalWikiSummaries,
                            onBlockExternalWikiSummariesChange = { v ->
                                blockExternalWikiSummaries = v
                                savePrivacyOfflineMode(context, v)
                            },
                            highlightPrivacySetting = highlightPrivacySetting,
                            onPrivacyHighlightShown = { highlightPrivacySetting = false },
                            debugModeEnabled = debugModeEnabled,
                            onDebugModeEnabledChange = { v ->
                                debugModeEnabled = v
                                saveDebugMode(context, v)
                                if (!v && currentView == "logs") currentView = "settings"
                            },
                        )
                    } else {
                        LogsScreen(logs = DebugLogStore.listLogs(context))
                    }
                }
                if (
                    showStatsRow &&
                        currentView != "settings" &&
                        currentView != "logs" &&
                        currentView != "directory" &&
                        currentView != "info" &&
                        currentView != "feedback"
                ) {
                    StatsRow(
                        points = totalPoints,
                        discovered = discoveredForOrigin,
                        total = catalogSize,
                        progress = progress
                    )
                }
            }
        }
    }

    if (showOnboarding) {
        OnboardingTutorialDialog(onDismiss = { dismissOnboarding() })
    }

    if (showPrivacyTooltip) {
        AlertDialog(
            onDismissRequest = {
                showPrivacyTooltip = false
                savePrivacyTooltipShown(context, true)
            },
            title = { Text("Datenschutzmodus") },
            text = {
                Text(
                    "Der Datenschutzmodus befindet sich in den Einstellungen. " +
                        "Wenn du ihn aktivierst, läuft die App zu 100% offline."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPrivacyTooltip = false
                        savePrivacyTooltipShown(context, true)
                        highlightPrivacySetting = true
                        currentView = "settings"
                    }
                ) { Text("Zu Einstellungen") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPrivacyTooltip = false
                        savePrivacyTooltipShown(context, true)
                    }
                ) { Text("Verstanden") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrainSeriesPreview() {
    BaureihensammlerTheme { TrainSeriesScreen() }
}
