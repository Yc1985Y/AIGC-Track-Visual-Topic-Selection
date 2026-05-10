package com.vsa.visualsemanticagent.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Base64
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import kotlin.math.min

object ImageEncodingUtils {

    fun bitmapToBase64(bitmap: Bitmap, jpegQuality: Int = 80): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    }

    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        return when {
            image.planes.size >= 3 -> yuv420888ToBitmap(image)
            image.planes.isNotEmpty() -> decodeSinglePlaneBitmap(image)
            else -> throw IllegalStateException("ImageProxy has no planes")
        }
    }

    private fun yuv420888ToBitmap(image: ImageProxy): Bitmap {
        val nv21 = yuv420888ToNv21(image)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val bytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IllegalStateException("Failed to decode YUV image into bitmap")
    }

    private fun decodeSinglePlaneBitmap(image: ImageProxy): Bitmap {
        val plane = image.planes.first()
        val buffer = plane.buffer.duplicate().apply { rewind() }
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.let { return it }

        return grayscaleFromLumaPlane(
            plane = plane,
            width = image.width,
            height = image.height
        )
    }

    private fun yuv420888ToNv21(image: ImageProxy): ByteArray {
        val width = image.width
        val height = image.height
        val ySize = width * height
        val uvSize = width * height / 2
        val nv21 = ByteArray(ySize + uvSize)

        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        copyLumaPlane(
            plane = yPlane,
            width = width,
            height = height,
            output = nv21
        )
        copyChromaPlanes(
            uPlane = uPlane,
            vPlane = vPlane,
            width = width,
            height = height,
            output = nv21,
            outputOffset = ySize
        )
        return nv21
    }

    private fun copyLumaPlane(
        plane: ImageProxy.PlaneProxy,
        width: Int,
        height: Int,
        output: ByteArray
    ) {
        val buffer = plane.buffer
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride
        val rowBuffer = ByteArray(rowStride)
        var outputOffset = 0

        repeat(height) { row ->
            val length = min(rowStride, buffer.remaining())
            buffer.get(rowBuffer, 0, length)

            if (pixelStride == 1) {
                System.arraycopy(rowBuffer, 0, output, outputOffset, width)
                outputOffset += width
            } else {
                repeat(width) { col ->
                    output[outputOffset++] = rowBuffer[col * pixelStride]
                }
            }

            val nextRowPosition = min(buffer.capacity(), (row + 1) * rowStride)
            buffer.position(nextRowPosition)
        }
    }

    private fun grayscaleFromLumaPlane(
        plane: ImageProxy.PlaneProxy,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val buffer = plane.buffer.duplicate().apply { rewind() }
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride.coerceAtLeast(1)
        val rowBuffer = ByteArray(rowStride.coerceAtLeast(width))
        val pixels = IntArray(width * height)

        repeat(height) { row ->
            val rowLength = minOf(rowBuffer.size, buffer.remaining())
            buffer.get(rowBuffer, 0, rowLength)

            repeat(width) { col ->
                val sourceIndex = (col * pixelStride).coerceAtMost(rowLength - 1).coerceAtLeast(0)
                val gray = rowBuffer[sourceIndex].toInt() and 0xFF
                pixels[row * width + col] = Color.rgb(gray, gray, gray)
            }

            val nextRowPosition = minOf(buffer.capacity(), (row + 1) * rowStride)
            buffer.position(nextRowPosition)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private fun copyChromaPlanes(
        uPlane: ImageProxy.PlaneProxy,
        vPlane: ImageProxy.PlaneProxy,
        width: Int,
        height: Int,
        output: ByteArray,
        outputOffset: Int
    ) {
        val chromaWidth = width / 2
        val chromaHeight = height / 2
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer
        val uRowStride = uPlane.rowStride
        val vRowStride = vPlane.rowStride
        val uPixelStride = uPlane.pixelStride
        val vPixelStride = vPlane.pixelStride
        val uRow = ByteArray(uRowStride)
        val vRow = ByteArray(vRowStride)
        var offset = outputOffset

        repeat(chromaHeight) { row ->
            val uLength = min(uRowStride, uBuffer.remaining())
            val vLength = min(vRowStride, vBuffer.remaining())
            uBuffer.get(uRow, 0, uLength)
            vBuffer.get(vRow, 0, vLength)

            repeat(chromaWidth) { col ->
                output[offset++] = vRow[col * vPixelStride]
                output[offset++] = uRow[col * uPixelStride]
            }

            val nextUPosition = min(uBuffer.capacity(), (row + 1) * uRowStride)
            val nextVPosition = min(vBuffer.capacity(), (row + 1) * vRowStride)
            uBuffer.position(nextUPosition)
            vBuffer.position(nextVPosition)
        }
    }
}

object JsonCleansingUtils {

    fun extractJsonFromDirtyText(dirtyText: String): String {
        val startIndex = dirtyText.indexOf('{')
        if (startIndex == -1) {
            throw IllegalArgumentException("No valid JSON structure found in text")
        }

        var depth = 0
        var inString = false
        var escaped = false

        for (index in startIndex until dirtyText.length) {
            val char = dirtyText[index]
            if (inString) {
                when {
                    escaped -> escaped = false
                    char == '\\' -> escaped = true
                    char == '"' -> inString = false
                }
                continue
            }

            when (char) {
                '"' -> inString = true
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return dirtyText.substring(startIndex, index + 1)
                    }
                }
            }
        }

        throw IllegalArgumentException("No complete JSON object found in text")
    }

    fun removeMarkdownWrappers(jsonString: String): String {
        return jsonString
            .replace(Regex("^```json\\s*"), "")
            .replace(Regex("^```\\s*"), "")
            .replace(Regex("\\s*```$"), "")
            .trim()
    }
}
