package com.example.hugbunadarver2.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

fun decodeMoviePoster(imageValue: String?): Bitmap? {
    if (imageValue.isNullOrBlank()) return null

    return try {
        // Support both raw Base64 and data URI payloads like data:image/jpeg;base64,...
        val normalized = imageValue
            .substringAfter("base64,", imageValue)
            .replace("\n", "")
            .replace("\r", "")
            .trim()

        val bytes = Base64.decode(normalized, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (_: Exception) {
        null
    }
}

