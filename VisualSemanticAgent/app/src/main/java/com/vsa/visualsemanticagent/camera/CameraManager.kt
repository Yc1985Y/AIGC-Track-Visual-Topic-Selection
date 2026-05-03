package com.vsa.visualsemanticagent.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.vsa.visualsemanticagent.utils.ImageEncodingUtils
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CameraManager {

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraExecutor: ExecutorService? = null
    private var boundPreviewView: PreviewView? = null

    fun bindCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        if (boundPreviewView === previewView && imageCapture != null && cameraProvider != null) {
            return
        }

        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                cameraProvider = provider
                boundPreviewView = previewView

                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
                    .build()

                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                imageCapture = null
                boundPreviewView = null
                Timber.e(e, "Failed to bind camera preview")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    suspend fun captureBase64Image(): String {
        val capture = imageCapture ?: throw IllegalStateException("Camera is not ready")
        val executor = getCameraExecutor()

        return suspendCancellableCoroutine { continuation ->
            capture.takePicture(
                executor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        try {
                            val bitmap = ImageEncodingUtils.imageProxyToBitmap(image)
                            val rotated = bitmap.rotate(image.imageInfo.rotationDegrees.toFloat())
                            val encoded = ImageEncodingUtils.bitmapToBase64(rotated)
                            if (continuation.isActive) {
                                continuation.resume(encoded)
                            } else {
                                Timber.d("Capture result dropped because coroutine was cancelled")
                            }
                        } catch (e: Exception) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(e)
                            } else {
                                Timber.w(e, "Capture failed after coroutine cancellation")
                            }
                        } finally {
                            image.close()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(exception)
                        } else {
                            Timber.w(exception, "Capture callback received after cancellation")
                        }
                    }
                }
            )

            continuation.invokeOnCancellation {
                Timber.d("Capture coroutine cancelled before camera callback completed")
            }
        }
    }

    fun shutdown() {
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Timber.w(e, "Failed to unbind camera")
        }
        cameraExecutor?.shutdown()
        cameraExecutor = null
        cameraProvider = null
        imageCapture = null
        boundPreviewView = null
    }

    private fun getCameraExecutor(): ExecutorService {
        if (cameraExecutor == null) {
            cameraExecutor = Executors.newSingleThreadExecutor()
        }
        return cameraExecutor!!
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        if (degrees == 0f) return this
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}
