package eu.florianbecker.baureihensammler.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

const val FAHRZEUGNUMMER_INFO_URL = "https://bahninfos.com/fahrzeugnummernsystematik/"

private const val TUTORIAL_IMAGE_ASSET = "101.png"
private const val TUTORIAL_IMAGE_MAX_WIDTH_PX = 1200

private fun decodeTutorialAsset(context: Context, assetPath: String): ImageBitmap? {
    return try {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.assets.open(assetPath).use { BitmapFactory.decodeStream(it, null, boundsOpts) }
        val sampleSize =
            maxOf(1, boundsOpts.outWidth / TUTORIAL_IMAGE_MAX_WIDTH_PX)
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        context.assets.open(assetPath).use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOpts)?.asImageBitmap()
        }
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun TutorialLocomotiveImage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageBitmap =
        remember(context, TUTORIAL_IMAGE_ASSET) {
            decodeTutorialAsset(context, TUTORIAL_IMAGE_ASSET)
        }
    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "Lokomotive DB Baureihe 101 mit Fahrzeugnummer 101 020-6 an der Front",
            contentScale = ContentScale.FillWidth,
            modifier =
                modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
        )
    }
}

@Composable
fun OnboardingTutorialDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Willkommen beim Baureihensammler") },
        text = {
            Column(
                modifier =
                    Modifier
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "So funktioniert die App:",
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "1. Lies an der Lok oder am Triebwagen die sichtbare Fahrzeugnummer ab.\n" +
                        "2. Gib die Baureihe in die Suche ein (z. B. 101).\n" +
                        "3. Triff die Baureihe in deiner Sammlung – optional mit Foto.",
                )
                Text(
                    "Wo steht was?",
                    fontWeight = FontWeight.SemiBold,
                )
                TutorialLocomotiveImage()
                NumberingBreakdownText()
                Text(
                    "Bei manchen Baureihen brauchst du zusätzlich die Wagennummer " +
                        "(Feld rechts neben der Baureihe in der Suche). Details dazu findest du " +
                        "jederzeit über das Fragezeichen oben.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Los geht's") }
        },
    )
}

@Composable
fun FahrzeugnummerInfoScreen(
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Scrolling is handled by TrainSeriesScreen (info view); no nested verticalScroll here.
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            "Infos zur Fahrzeugnummer",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "An vielen Fahrzeugen findest du vorne oder an der Seite eine Nummer aus Baureihe, " +
                "Wagennummer und Prüfziffer. Baureihe und Wagennummer stehen meist durch ein Leerzeichen " +
                "getrennt (z. B. 101 020). Die Prüfziffer steht nach einem Bindestrich (z. B. 020-6).",
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TutorialLocomotiveImage()
                NumberingBreakdownText()
            }
        }
        Text(
            "Erweiterte Systematik",
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            "Neben dieser einfachen Aufteilung gibt es die offizielle UIC-Fahrzeugnummer " +
                "(12-stellig, mit weiteren Bedeutungen für Land, Fahrzeugart, Baureihe und mehr). " +
                "Für den Alltag reicht meist die sichtbare Kennzeichnung am Fahrzeug; die volle " +
                "Systematik ist auf bahninfos.com gut erklärt.",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedButton(
            onClick = { onOpenUrl(FAHRZEUGNUMMER_INFO_URL) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Fahrzeugnummernsystematik auf bahninfos.com")
        }
    }
}

@Composable
private fun NumberingBreakdownText() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberingPartRow(
            label = "101",
            title = "Baureihe",
            description = "Die Baureihe des Fahrzeugs (hier: DB-Baureihe 101).",
        )
        NumberingPartRow(
            label = "020",
            title = "Wagennummer",
            description =
                "Die laufende Nummer innerhalb der Baureihe – hier also etwa die 20. Lok dieser Baureihe " +
                    "(führende Nullen sind nur Schreibweise). Sie folgt auf die Baureihe, getrennt durch ein Leerzeichen.",
        )
        NumberingPartRow(
            label = "6",
            title = "Prüfziffer",
            description =
                "Die letzte Ziffer nach dem einzigen Bindestrich in der Kennzeichnung – nicht zwischen Baureihe " +
                    "und Wagennummer.",
        )
        Text(
            buildAnnotatedString {
                append("Am Beispiel ")
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)) {
                    append("101 020-6")
                }
                append(": ")
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("101") }
                append(" = Baureihe, ")
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("020") }
                append(" = Wagennummer (Leerzeichen dazwischen), ")
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("-6") }
                append(" = Prüfziffer (nur hier steht der Bindestrich).")
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun NumberingPartRow(
    label: String,
    title: String,
    description: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    )
                ) {
                    append(label)
                }
                append(" — ")
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(title) }
            },
        )
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
