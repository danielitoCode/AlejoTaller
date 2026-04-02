package com.elitec.alejotallerscan.feature.scan.presentation.component

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun OperatorQrScannerSection(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    onDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(isEnabled, lifecycleOwner) {
        if (!isEnabled) {
            onDispose { }
        } else {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val cameraExecutor = Executors.newSingleThreadExecutor()
            val scanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
            )
            val consumed = AtomicBoolean(false)

            val listener = Runnable {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { imageAnalysis ->
                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage == null || consumed.get()) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    val value = barcodes.firstNotNullOfOrNull { it.rawValue?.trim() }
                                    if (!value.isNullOrBlank() && consumed.compareAndSet(false, true)) {
                                        onDetected(value)
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            }

            cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))

            onDispose {
                runCatching { cameraProviderFuture.get().unbindAll() }
                scanner.close()
                cameraExecutor.shutdown()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        )
        ScannerOverlay(modifier = Modifier.fillMaxSize())
        Text(
            text = "Alinea el QR dentro del marco",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun ScannerOverlay(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
    ) {
        val frameWidth = size.width * 0.62f
        val frameHeight = size.height * 0.56f
        val left = (size.width - frameWidth) / 2f
        val top = (size.height - frameHeight) / 2f
        val corner = 28.dp.toPx()
        val strokeWidth = 5.dp.toPx()
        val markerLength = 34.dp.toPx()

        drawRoundRect(
            color = Color(0x88_00_00_00),
            size = size,
            cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
        )
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(frameWidth, frameHeight),
            cornerRadius = CornerRadius(corner, corner),
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )

        val frameColor = Color.White
        drawRoundRect(
            color = frameColor.copy(alpha = 0.7f),
            topLeft = Offset(left, top),
            size = Size(frameWidth, frameHeight),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = 2.dp.toPx())
        )

        val points = listOf(
            Offset(left, top) to listOf(
                Offset(left + markerLength, top),
                Offset(left, top + markerLength)
            ),
            Offset(left + frameWidth, top) to listOf(
                Offset(left + frameWidth - markerLength, top),
                Offset(left + frameWidth, top + markerLength)
            ),
            Offset(left, top + frameHeight) to listOf(
                Offset(left + markerLength, top + frameHeight),
                Offset(left, top + frameHeight - markerLength)
            ),
            Offset(left + frameWidth, top + frameHeight) to listOf(
                Offset(left + frameWidth - markerLength, top + frameHeight),
                Offset(left + frameWidth, top + frameHeight - markerLength)
            )
        )

        points.forEach { (start, ends) ->
            ends.forEach { end ->
                drawLine(
                    color = frameColor,
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
