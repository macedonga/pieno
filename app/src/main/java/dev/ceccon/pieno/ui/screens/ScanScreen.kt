package dev.ceccon.pieno.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dev.ceccon.pieno.data.repo.ImportResult
import dev.ceccon.pieno.ui.components.EmptyState
import dev.ceccon.pieno.ui.components.PienoTopBar
import dev.ceccon.pieno.ui.components.PrimaryButton
import dev.ceccon.pieno.ui.components.SecondaryButton
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.rememberContainer
import dev.ceccon.pieno.ui.theme.Green
import dev.ceccon.pieno.ui.theme.Space
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun ScanScreen(onBack: () -> Unit) {
    val repo = rememberContainer().repository
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LaunchedEffect(Unit) { if (!granted) permLauncher.launch(Manifest.permission.CAMERA) }

    var handled by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ImportResult?>(null) }

    Column(Modifier.fillMaxSize()) {
        PienoTopBar("Scansiona QR", onBack)
        when {
            !granted -> EmptyState(
                title = "Serve l'accesso alla fotocamera",
                subtitle = "Consenti l'accesso per inquadrare il QR di una tessera.",
                icon = PienoIcons.Scan,
                action = { PrimaryButton("Consenti", onClick = { permLauncher.launch(Manifest.permission.CAMERA) }) },
            )
            result != null -> ScanResult(
                result = result!!,
                onRetry = { handled = false; result = null },
                onDone = onBack,
            )
            else -> Box(Modifier.fillMaxSize()) {
                CameraScanner(onQr = { payload ->
                    if (!handled) {
                        handled = true
                        scope.launch { result = repo.importCondivisa(payload) }
                    }
                })
                ScannerOverlay()
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraScanner(onQr: (String) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build(),
        )
    }
    val executor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            runCatching { scanner.close() }
        }
    }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                runCatching {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    analysis.setAnalyzer(executor) { proxy -> analyze(scanner, proxy, onQr) }
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
    )
}

@OptIn(ExperimentalGetImage::class)
private fun analyze(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    proxy: ImageProxy,
    onQr: (String) -> Unit,
) {
    val media = proxy.image
    if (media == null) {
        proxy.close()
        return
    }
    val input = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
    scanner.process(input)
        .addOnSuccessListener { barcodes ->
            barcodes.firstOrNull()?.rawValue?.let(onQr)
        }
        .addOnCompleteListener { proxy.close() }
}

@Composable
private fun ScannerOverlay() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(Space.s5))
                .border(3.dp, Color.White, RoundedCornerShape(Space.s5)),
        )
        Column(Modifier.fillMaxWidth().padding(Space.s6), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(340.dp))
            Box(Modifier.clip(RoundedCornerShape(Space.s3)).background(Color.Black.copy(alpha = 0.55f)).padding(horizontal = Space.s4, vertical = Space.s2)) {
                Text("Inquadra il QR della tessera", style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        }
    }
}

@Composable
private fun ScanResult(result: ImportResult, onRetry: () -> Unit, onDone: () -> Unit) {
    when (result) {
        is ImportResult.Success -> EmptyState(
            title = "Tessera importata",
            subtitle = "Targa ${result.targa}. " + if (result.verified) "Firma verificata." else "Firma non verificata.",
            icon = PienoIcons.Check,
            action = { PrimaryButton("Fatto", onClick = onDone) },
        )
        is ImportResult.InvalidSignature -> EmptyState(
            title = "Firma non valida",
            subtitle = "Il QR potrebbe essere falso o alterato. Non è stato importato.",
            icon = PienoIcons.Close,
            action = { SecondaryButton("Scansiona ancora", onClick = onRetry) },
        )
        is ImportResult.Error -> EmptyState(
            title = "QR non valido",
            subtitle = result.message,
            icon = PienoIcons.Info,
            action = { SecondaryButton("Scansiona ancora", onClick = onRetry) },
        )
    }
}
