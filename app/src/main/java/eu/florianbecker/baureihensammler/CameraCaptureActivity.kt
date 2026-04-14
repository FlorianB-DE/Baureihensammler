package eu.florianbecker.baureihensammler

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.Manifest
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImageView
import eu.florianbecker.baureihensammler.data.TrainSeriesOrigin
import eu.florianbecker.baureihensammler.ui.theme.BaureihensammlerTheme
import eu.florianbecker.baureihensammler.util.DebugLogStore
import java.io.File
import java.io.FileOutputStream

class CameraCaptureActivity : ComponentActivity() {

    private lateinit var baureihe: String
    private lateinit var origin: TrainSeriesOrigin

    private var captureUri: Uri? = null

    private val phase = mutableStateOf(Phase.RequestingPermission)
    private val cameraSessionId = mutableIntStateOf(0)

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (!success) {
                DebugLogStore.logError(
                    context = this,
                    source = "CameraCaptureActivity.TakePicture",
                    message = "TakePicture wurde abgebrochen oder lieferte kein Bild."
                )
                setResult(RESULT_CANCELED)
                finish()
            } else {
                phase.value = Phase.Cropping
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                phase.value = Phase.LaunchingCamera
            } else {
                DebugLogStore.logError(
                    context = this,
                    source = "CameraCaptureActivity.RequestPermission",
                    message = "CAMERA-Berechtigung wurde abgelehnt."
                )
                Toast.makeText(
                    this,
                    "Kamera-Berechtigung fehlt. Bitte in den App-Einstellungen aktivieren.",
                    Toast.LENGTH_LONG
                ).show()
                setResult(RESULT_CANCELED)
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            baureihe = intent.getStringExtra(EXTRA_BAUREIHE) ?: ""
            origin = TrainSeriesOrigin.fromName(intent.getStringExtra(EXTRA_ORIGIN))
        } catch (t: Throwable) {
            DebugLogStore.logError(
                context = this,
                source = "CameraCaptureActivity.onCreate",
                message = "Intent-Extras konnten nicht gelesen werden.",
                throwable = t
            )
            Toast.makeText(this, "Kamera konnte nicht geöffnet werden", Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        setContent {
            BaureihensammlerTheme {
                val currentPhase by phase
                val session by cameraSessionId
                when (currentPhase) {
                    Phase.RequestingPermission -> {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                        LaunchedEffect(Unit) {
                            if (hasCameraPermission()) {
                                phase.value = Phase.LaunchingCamera
                            } else {
                                requestCameraPermission.launch(Manifest.permission.CAMERA)
                            }
                        }
                    }
                    Phase.LaunchingCamera -> {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                        LaunchedEffect(session) {
                            try {
                                val uri = prepareCaptureUri()
                                takePicture.launch(uri)
                            } catch (t: Throwable) {
                                DebugLogStore.logError(
                                    context = this@CameraCaptureActivity,
                                    source = "CameraCaptureActivity.launchCamera",
                                    message = "Kamera-Start fehlgeschlagen (URI/FileProvider).",
                                    throwable = t
                                )
                                Toast.makeText(
                                    this@CameraCaptureActivity,
                                    "Kamera konnte nicht gestartet werden",
                                    Toast.LENGTH_SHORT
                                ).show()
                                setResult(RESULT_CANCELED)
                                finish()
                            }
                        }
                    }
                    Phase.Cropping -> {
                        val uri = captureUri
                        if (uri != null) {
                            CropReviewScreen(
                                imageUri = uri,
                                baureihe = baureihe,
                                onClose = {
                                    setResult(RESULT_CANCELED)
                                    finish()
                                },
                                onRetake = {
                                    phase.value = Phase.LaunchingCamera
                                    cameraSessionId.intValue++
                                },
                                onCroppedBitmap = { bitmap -> saveCroppedAndFinish(bitmap) }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun prepareCaptureUri(): Uri {
        val dir = File(cacheDir, "camera_capture").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        val uri =
            FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
        captureUri = uri
        return uri
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    private fun saveCroppedAndFinish(bitmap: Bitmap) {
        val snapshotsDir = File(filesDir, "snapshots").apply { mkdirs() }
        val outFile = File(snapshotsDir, "${baureihe}_${System.currentTimeMillis()}.jpg")
        try {
            FileOutputStream(outFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
        } catch (t: Exception) {
            DebugLogStore.logError(
                context = this,
                source = "CameraCaptureActivity.saveCroppedAndFinish",
                message = "Speichern des zugeschnittenen Bildes fehlgeschlagen.",
                throwable = t
            )
            Toast.makeText(this, "Speichern fehlgeschlagen", Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
            return
        } finally {
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        setResult(
            RESULT_OK,
            Intent()
                .putExtra(EXTRA_BAUREIHE, baureihe)
                .putExtra(EXTRA_ORIGIN, origin.name)
                .putExtra(EXTRA_IMAGE_PATH, outFile.absolutePath)
        )
        finish()
    }

    companion object {
        const val EXTRA_BAUREIHE = "extra_baureihe"
        const val EXTRA_IMAGE_PATH = "extra_image_path"
        const val EXTRA_ORIGIN = "extra_origin"

        fun createIntent(
            context: Context,
            baureihe: String,
            origin: TrainSeriesOrigin = TrainSeriesOrigin.DB
        ): Intent {
            return Intent(context, CameraCaptureActivity::class.java)
                .putExtra(EXTRA_BAUREIHE, baureihe)
                .putExtra(EXTRA_ORIGIN, origin.name)
        }
    }
}

private enum class Phase {
    RequestingPermission,
    LaunchingCamera,
    Cropping,
}

@Composable
private fun CropReviewScreen(
    imageUri: Uri,
    baureihe: String,
    onClose: () -> Unit,
    onRetake: () -> Unit,
    onCroppedBitmap: (Bitmap) -> Unit,
) {
    val context = LocalContext.current
    val cropViewHolder = remember { mutableStateOf<CropImageView?>(null) }
    val imageReady = remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        key(imageUri) {
            AndroidView(
                factory = { ctx ->
                    CropImageView(ctx).apply {
                        guidelines = CropImageView.Guidelines.ON
                        setAspectRatio(16, 9)
                        setFixedAspectRatio(true)
                        setOnSetImageUriCompleteListener { _, _, error ->
                            imageReady.value = error == null
                            if (error != null) {
                                DebugLogStore.logError(
                                    context = ctx,
                                    source = "CropReviewScreen.setImageUriAsync",
                                    message = "Bild konnte nicht in CropImageView geladen werden.",
                                    throwable = error
                                )
                            }
                        }
                        setImageUriAsync(imageUri)
                        cropViewHolder.value = this
                    }
                },
                modifier = Modifier.fillMaxSize().padding(bottom = 96.dp)
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        ) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = "Abbrechen",
                tint = Color.White
            )
        }

        Text(
            "BR $baureihe — 16:9 zuschneiden",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp)
        )

        Row(
            modifier =
                Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetake,
                enabled = !busy,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, Color.White),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                    )
            ) {
                Text("Neu aufnehmen")
            }
            Button(
                onClick = {
                    val view = cropViewHolder.value
                    if (view == null || !imageReady.value) {
                        Toast.makeText(context, "Bild wird noch geladen …", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    busy = true
                    val bmp =
                        try {
                            view.getCroppedImage(
                                4096,
                                2304,
                                CropImageView.RequestSizeOptions.RESIZE_INSIDE
                            )
                        } catch (t: Throwable) {
                            DebugLogStore.logError(
                                context = context,
                                source = "CropReviewScreen.getCroppedImage",
                                message = "Cropping ist mit Ausnahme fehlgeschlagen.",
                                throwable = t
                            )
                            null
                        }
                    busy = false
                    if (bmp != null) {
                        onCroppedBitmap(bmp)
                    } else {
                        Toast.makeText(context, "Zuschnitt fehlgeschlagen", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !busy && imageReady.value,
                modifier = Modifier.weight(1f)
            ) {
                Text("Übernehmen")
            }
        }
    }
}
