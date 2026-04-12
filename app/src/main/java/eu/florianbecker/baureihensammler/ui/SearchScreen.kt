package eu.florianbecker.baureihensammler.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import eu.florianbecker.baureihensammler.R
import eu.florianbecker.baureihensammler.data.TrainSeries
import eu.florianbecker.baureihensammler.data.fetchWikipediaSummary
import eu.florianbecker.baureihensammler.search.calculatePoints
import eu.florianbecker.baureihensammler.search.ghostBaureiheSuffix
import java.io.File

/** DB Corporate Red */
private val DbBrandRed = Color(0xFFEC0016)

@Composable
fun SearchInputPlate(query: String, onQueryChange: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val ghostSuffix = ghostBaureiheSuffix(query)
    val contentPadding = TextFieldDefaults.contentPaddingWithoutLabel()
    val plateUnderline = DbBrandRed
    val fieldColors =
        TextFieldDefaults.colors(
            focusedTextColor = Color.Transparent,
            unfocusedTextColor = Color.Transparent,
            disabledTextColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedPlaceholderColor = colors.onSurfaceVariant.copy(alpha = 0.55f),
            unfocusedPlaceholderColor = colors.onSurfaceVariant.copy(alpha = 0.55f),
            cursorColor = colors.primary,
            focusedIndicatorColor = plateUnderline,
            unfocusedIndicatorColor = colors.onSurfaceVariant.copy(alpha = 0.45f),
        )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceVariant,
        modifier =
            Modifier.fillMaxWidth().border(2.dp, colors.outline, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier =
                    Modifier.width(50.dp)
                        .fillMaxHeight()
                        .background(DbBrandRed)
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "DB",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    lineHeight = MaterialTheme.typography.labelLarge.fontSize
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "BR",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    lineHeight = MaterialTheme.typography.labelLarge.fontSize,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Box(
                modifier =
                    Modifier.weight(1f)
                        .fillMaxHeight()
                        .background(colors.surfaceVariant)
                        .padding(start = 4.dp, end = 10.dp)
            ) {
                if (query.isNotEmpty()) {
                    Text(
                        text =
                            buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = colors.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                ) { append(query) }
                                if (ghostSuffix.isNotEmpty()) {
                                    withStyle(
                                        SpanStyle(
                                            color =
                                                colors.onSurfaceVariant.copy(
                                                    alpha = 0.5f
                                                )
                                        )
                                    ) { append(ghostSuffix) }
                                }
                            },
                        style = LocalTextStyle.current,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier =
                            Modifier.align(Alignment.CenterStart)
                                .padding(contentPadding)
                                .wrapContentWidth()
                    )
                }
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            "z. B. 401",
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    },
                    colors = fieldColors,
                )
            }
        }
    }
}

@Composable
fun SearchView(
    validSeries: TrainSeries?,
    alreadyCollected: Boolean,
    hasCollectionPhoto: Boolean,
    collectionSnapshotPath: String?,
    imeVisible: Boolean,
    blockExternalWikiSummaries: Boolean,
    onTakeSnapshot: () -> Unit,
    onSaveCollected: () -> Unit,
    onOpenWiki: (String) -> Unit
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var showMoreInfos by rememberSaveable(validSeries?.baureihe) { mutableStateOf(true) }
    val detailsVisible = !imeVisible && showMoreInfos

    validSeries?.let { series ->
        var wikiSummary by remember(series.baureihe) { mutableStateOf<String?>(null) }
        var wikiSummaryLoading by remember(series.baureihe) { mutableStateOf(false) }
        LaunchedEffect(series.baureihe, detailsVisible, blockExternalWikiSummaries) {
            if (!detailsVisible) return@LaunchedEffect
            if (blockExternalWikiSummaries) {
                wikiSummary = null
                wikiSummaryLoading = false
                return@LaunchedEffect
            }
            if (wikiSummary != null) return@LaunchedEffect
            wikiSummaryLoading = true
            wikiSummary = fetchWikipediaSummary(series.wikiSummaryApiUrl)
            wikiSummaryLoading = false
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
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
                            contentDescription =
                                if (detailsVisible) "Infos ausblenden" else "Mehr Infos",
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.rotate(if (detailsVisible) 180f else 0f)
                        )
                    }
                    if (alreadyCollected) {
                        OutlinedButton(onClick = onTakeSnapshot) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Outlined.CameraAlt, contentDescription = null)
                                Text(if (hasCollectionPhoto) "Ändern" else "Hinzufügen")
                            }
                        }
                    }
                }
                if (detailsVisible) {
                    Text("${series.category} - Vmax ${series.vmaxKmh} km/h")
                    Text("Haufigkeit (Schätzung): ${series.fleetEstimate} Fahrzeuge")
                    Text("Punkte beim Markieren: ${calculatePoints(series.fleetEstimate)}")
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Mein Schnappschuss",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                        val snapshotBmp =
                            remember(collectionSnapshotPath) {
                                collectionSnapshotPath?.takeIf { File(it).exists() }?.let { path
                                    ->
                                    BitmapFactory.decodeFile(path)
                                }
                            }
                        if (snapshotBmp != null) {
                            Image(
                                bitmap = snapshotBmp.asImageBitmap(),
                                contentDescription = "Mein Schnappschuss BR ${series.baureihe}",
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Noch kein Foto",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (blockExternalWikiSummaries) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            "Externe Kurztexte sind deaktiviert (Offline-/Datenschutzmodus).",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    } else if (wikiSummaryLoading && wikiSummary == null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                "Wikipedia …",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                    wikiSummary?.let { summary ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Image(
                            painter = painterResource(R.drawable.cc_by_sa_4_80x15),
                            contentDescription =
                                "CC BY-SA 4.0 — Creative Commons Namensnennung, Weitergabe unter gleichen Bedingungen",
                            modifier =
                                Modifier.height(10.dp).wrapContentWidth().clickable {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(
                                                "https://creativecommons.org/licenses/by-sa/4.0/deed.de"
                                            )
                                        )
                                    )
                                },
                            contentScale = ContentScale.FillHeight
                        )
                    }
                    OutlinedButton(onClick = { onOpenWiki(series.wikiArticleUrl) }) {
                        Text("Wikipedia")
                    }
                }
            }
        }
    }
        ?: Text("Bei gültiger Baureihe erscheinen die Infos direkt.", color = colors.onSurface)
}
