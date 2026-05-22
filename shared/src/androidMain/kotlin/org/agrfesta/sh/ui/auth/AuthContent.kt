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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

internal enum class CameraPermissionState { Granted, Denied, PermanentlyDenied }

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

    DisposableEffect(Unit) {
        onDispose { backgroundExecutor.shutdown() }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val barcodeScanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
            )
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(backgroundExecutor) { imageProxy ->
                            processQrCode(barcodeScanner, imageProxy, resultDelivered) { value ->
                                ContextCompat.getMainExecutor(ctx).execute {
                                    cameraProvider.unbindAll()
                                    onResult(value)
                                }
                            }
                        }
                    }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    android.util.Log.e("QrScanner", "Failed to bind camera", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
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
internal fun QrAuthContent(
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
        Text(
            text = "Inquadra il QR code",
            style = MaterialTheme.typography.headlineMedium
        )
        when (permissionState) {
            CameraPermissionState.Granted -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("auth_qr_viewfinder")
            ) {
                qrScannerContent(onTokenSaved)
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
                Text("Permesso fotocamera negato. Abilitalo dalle impostazioni.")
                Button(onClick = onOpenSettings) { Text("Vai alle impostazioni") }
            }
        }
    }
}
