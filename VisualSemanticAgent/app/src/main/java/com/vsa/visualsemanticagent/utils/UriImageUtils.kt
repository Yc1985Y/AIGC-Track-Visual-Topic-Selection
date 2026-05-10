package com.vsa.visualsemanticagent.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

object UriImageUtils {

    fun uriToBitmap(
        context: Context,
        uri: Uri
    ): Bitmap {
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Cannot open image input stream" }
            return BitmapFactory.decodeStream(input)
                ?: throw IllegalArgumentException("Cannot decode image")
        }
    }

    fun bitmapToBase64Jpeg(
        bitmap: Bitmap,
        quality: Int = 85
    ): String {
        return try {
            ImageEncodingUtils.bitmapToBase64(bitmap, quality)
        } finally {
            bitmap.recycle()
        }
    }

    fun uriToBase64Jpeg(
        context: Context,
        uri: Uri,
        quality: Int = 85
    ): String {
        val bitmap = uriToBitmap(context, uri)
        return bitmapToBase64Jpeg(bitmap, quality)
    }
}
