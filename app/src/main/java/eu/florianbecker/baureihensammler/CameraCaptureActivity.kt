package eu.florianbecker.baureihensammler

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import eu.florianbecker.baureihensammler.ui.theme.BaureihensammlerTheme
import java.io.File
import java.io.FileOutputStream

class CameraCaptureActivity : ComponentActivity() {
    private var imageCapture: ImageCapture? = null
    lateinit var previewView: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val baureihe = intent.getStringExtra(EXTRA_BAUREIHE) ?: ""
        setContent {
            BaureihensammlerTheme {
                CameraCaptureScreen(
                    baureihe = baureihe,
                    onBack = { finish() },
                    onBindCamera = { bindCamera() },
                    onCapture = { capture(baureihe) }
                )
            }
        }
    }

    private fun bindCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capture(baureihe: String) {
        val capture = imageCapture ?: return
        val snapshotsDir = File(filesDir, "snapshots")
        if (!snapshotsDir.exists()) snapshotsDir.mkdirs()
        val file = File(snapshotsDir, "${baureihe}_${System.currentTimeMillis()}.jpg")
        val output = ImageCapture.OutputFileOptions.Builder(file).build()
        capture.takePicture(
            output,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    cropCenter16by9(file)
                    val resultIntent = Intent()
                        .putExtra(EXTRA_BAUREIHE, baureihe)
                        .putExtra(EXTRA_IMAGE_PATH, file.absolutePath)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraCaptureActivity, "Foto fehlgeschlagen", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun cropCenter16by9(file: File) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return
        val w = bitmap.width
        val h = bitmap.height
        val ratio = 16f / 9f
        val srcRatio = w.toFloat() / h.toFloat()
        val cropW: Int
        val cropH: Int
        val x: Int
        val y: Int
        if (srcRatio > ratio) {
            cropH = h
            cropW = (cropH * ratio).toInt()
            x = (w - cropW) / 2
            y = 0
        } else {
            cropW = w
            cropH = (cropW / ratio).toInt()
            x = 0
            y = (h - cropH) / 2
        }
        val cropped = Bitmap.createBitmap(bitmap, x, y, cropW, cropH)
        bitmap.recycle()
        FileOutputStream(file).use { out ->
            cropped.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        cropped.recycle()
    }

    companion object {
        const val EXTRA_BAUREIHE = "extra_baureihe"
        const val EXTRA_IMAGE_PATH = "extra_image_path"

        fun createIntent(context: Context, baureihe: String): Intent {
            return Intent(context, CameraCaptureActivity::class.java)
                .putExtra(EXTRA_BAUREIHE, baureihe)
        }
    }
}

@Composable
private fun CameraCaptureScreen(
    baureihe: String,
    onBack: () -> Unit,
    onBindCamera: () -> Unit,
    onCapture: () -> Unit
) {
    val context = LocalContext.current
    val hasPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission.value = granted
        if (granted) onBindCamera()
    }

    LaunchedEffect(Unit) {
        if (hasPermission.value) onBindCamera() else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).also { view ->
                    (context as CameraCaptureActivity).previewView = view
                }
            }
        )

        GuideOverlay()

        IconButton(onClick = onBack, modifier = Modifier.padding(16.dp).align(Alignment.TopStart)) {
            Icon(Icons.Outlined.Close, contentDescription = "Schliessen", tint = Color.White)
        }

        Column(modifier = Modifier.align(Alignment.BottomCenter).padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("16:9 Fenster - BR $baureihe", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onCapture, modifier = Modifier.padding(top = 10.dp).size(82.dp), shape = CircleShape) {
                Text("Foto")
            }
        }
    }
}

@Composable
private fun GuideOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f)
            .drawWithContent {
                val w = size.width
                val h = size.height
                val targetRatio = 16f / 9f
                val containerRatio = w / h
                val windowWidth: Float
                val windowHeight: Float
                if (containerRatio > targetRatio) {
                    windowHeight = h
                    windowWidth = h * targetRatio
                } else {
                    windowWidth = w
                    windowHeight = w / targetRatio
                }
                val left = (w - windowWidth) / 2f
                val top = (h - windowHeight) / 2f
                val topLeft = Offset(left, top)
                val windowSize = Size(windowWidth, windowHeight)
                drawRect(Color.Black.copy(alpha = 0.58f))
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = topLeft,
                    size = windowSize,
                    cornerRadius = CornerRadius(20f, 20f),
                    blendMode = BlendMode.Clear
                )
                drawRoundRect(
                    color = Color(0xFF64B5F6),
                    topLeft = topLeft,
                    size = windowSize,
                    cornerRadius = CornerRadius(20f, 20f),
                    style = Stroke(width = 4f)
                )
            }
    )
}
