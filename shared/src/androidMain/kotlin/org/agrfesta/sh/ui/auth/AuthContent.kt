package org.agrfesta.sh.ui.auth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean

enum class CameraPermissionState { Granted, Denied, PermanentlyDenied }

@Composable
actual fun AuthContent(onTokenSaved: (String) -> Unit, tokenInvalid: Boolean) {
    val context = LocalContext.current
    var permissionState by remember {
        mutableStateOf(
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) CameraPermissionState.Granted
            else CameraPermissionState.Denied
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionState = if (granted) {
            CameraPermissionState.Granted
        } else {
            val activity = context as? Activity
            if (activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.CAMERA
                )
            ) CameraPermissionState.Denied
            else CameraPermissionState.PermanentlyDenied
        }
    }
    QrAuthContent(
        permissionState = permissionState,
        onRequestPermission = { launcher.launch(Manifest.permission.CAMERA) },
        onTokenSaved = onTokenSaved,
        tokenInvalid = tokenInvalid,
        onOpenSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        },
        qrScannerContent = { onResult -> CameraPreviewWithQrScanner(onResult) }
    )
}

@Composable
private fun CameraPreviewWithQrScanner(onResult: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val resultDelivered = remember { AtomicBoolean(false) }
    val backgroundExecutor = remember { Executors.newSingleThreadExecutor() }
    val isDisposed = remember { AtomicBoolean(false) }
    val safeExecutor = remember {
        Executor { command ->
            if (!isDisposed.get()) {
                try {
                    backgroundExecutor.execute(command)
                } catch (e: RejectedExecutionException) {
                    // Ignora i task sottomessi durante o dopo lo shutdown
                }
            }
        }
    }
    val currentOnResult by rememberUpdatedState(onResult)
    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            isDisposed.set(true)
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                android.util.Log.e("QrScanner", "Failed to unbind camera on dispose", e)
            }
            try {
                barcodeScanner.close()
            } catch (e: Exception) {
                android.util.Log.e("QrScanner", "Failed to close barcode scanner", e)
            }
            backgroundExecutor.shutdown()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                cameraProviderFuture.addListener({
                    if (isDisposed.get()) return@addListener
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(safeExecutor) { imageProxy ->
                                    if (isDisposed.get()) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }
                                    try {
                                        processQrCode(barcodeScanner, imageProxy, resultDelivered) { value ->
                                            ContextCompat.getMainExecutor(ctx).execute {
                                                if (!isDisposed.get()) {
                                                    cameraProvider.unbindAll()
                                                    currentOnResult(value)
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("QrScanner", "Error processing QR code", e)
                                        imageProxy.close()
                                    }
                                }
                            }
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("QrScanner", "Failed to initialize or bind camera", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            }
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 4.dp.toPx()
            val halfStroke = strokeWidth / 2
            val viewfinderSize = minOf(size.width, size.height) * 0.8f
            val left = (size.width - viewfinderSize) / 2
            val top = (size.height - viewfinderSize) / 2
            val right = left + viewfinderSize
            val bottom = top + viewfinderSize

            val overlayColor = Color.Black.copy(alpha = 0.6f)
            drawRect(overlayColor, topLeft = Offset(0f, 0f), size = Size(size.width, top))
            drawRect(overlayColor, topLeft = Offset(0f, bottom), size = Size(size.width, size.height - bottom))
            drawRect(overlayColor, topLeft = Offset(0f, top), size = Size(left, viewfinderSize))
            drawRect(overlayColor, topLeft = Offset(right, top), size = Size(size.width - right, viewfinderSize))

            val cornerLength = viewfinderSize * 0.12f
            val white = Color.White

            drawLine(white, Offset(left + halfStroke, top + halfStroke), Offset(left + cornerLength, top + halfStroke), strokeWidth, StrokeCap.Square)
            drawLine(white, Offset(left + halfStroke, top + halfStroke), Offset(left + halfStroke, top + cornerLength), strokeWidth, StrokeCap.Square)

            drawLine(white, Offset(right - halfStroke, top + halfStroke), Offset(right - cornerLength, top + halfStroke), strokeWidth, StrokeCap.Square)
            drawLine(white, Offset(right - halfStroke, top + halfStroke), Offset(right - halfStroke, top + cornerLength), strokeWidth, StrokeCap.Square)

            drawLine(white, Offset(left + halfStroke, bottom - halfStroke), Offset(left + cornerLength, bottom - halfStroke), strokeWidth, StrokeCap.Square)
            drawLine(white, Offset(left + halfStroke, bottom - halfStroke), Offset(left + halfStroke, bottom - cornerLength), strokeWidth, StrokeCap.Square)

            drawLine(white, Offset(right - halfStroke, bottom - halfStroke), Offset(right - cornerLength, bottom - halfStroke), strokeWidth, StrokeCap.Square)
            drawLine(white, Offset(right - halfStroke, bottom - halfStroke), Offset(right - halfStroke, bottom - cornerLength), strokeWidth, StrokeCap.Square)
        }
    }
}

private fun processQrCode(
    barcodeScanner: BarcodeScanner,
    imageProxy: ImageProxy,
    resultDelivered: AtomicBoolean,
    onResult: (String) -> Unit
) {
    if (resultDelivered.get()) { imageProxy.close(); return }
    val mediaImage = imageProxy.image
    if (mediaImage == null) { imageProxy.close(); return }
    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    barcodeScanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            barcodes.firstOrNull()?.rawValue?.trim()?.takeIf { it.isNotEmpty() }?.let { value ->
                if (resultDelivered.compareAndSet(false, true)) {
                    onResult(value)
                }
            }
        }
        .addOnCompleteListener { imageProxy.close() }
}

@Composable
fun QrAuthContent(
    permissionState: CameraPermissionState,
    onRequestPermission: () -> Unit,
    onTokenSaved: (String) -> Unit,
    tokenInvalid: Boolean = false,
    onOpenSettings: () -> Unit = {},
    qrScannerContent: @Composable ((String) -> Unit) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (tokenInvalid) {
            Text(
                text = "Token non valido, inseriscine uno nuovo.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("auth_token_invalid_banner")
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (permissionState) {
                CameraPermissionState.Granted -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("auth_qr_viewfinder")
                ) {
                    qrScannerContent(onTokenSaved)
                    // Persistent label for the scanning area: overlaid on the bottom of
                    // the viewfinder with a dark scrim so it stays legible against the
                    // camera feed in both light and dark themes.
                    Text(
                        text = "Inquadra il QR code",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .navigationBarsPadding()
                            .padding(vertical = 12.dp)
                    )
                }
                CameraPermissionState.Denied -> Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.testTag("auth_request_permission_button")
                ) {
                    Text("Concedi permesso fotocamera")
                }
                CameraPermissionState.PermanentlyDenied -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Permesso fotocamera negato. Abilitalo dalle impostazioni.",
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onOpenSettings) { Text("Vai alle impostazioni") }
                }
            }
        }
    }
}
