package com.example.hugbunadarver2.mybookings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

object BookingQrStorage {
    private const val QR_DIR = "booking_qr"

    fun getOrCreateQrBitmap(context: Context, bookingId: Long, sizePx: Int = 512): Bitmap? {
        return try {
            val fromDisk = loadQrBitmap(context, bookingId)
            if (fromDisk != null) return fromDisk

            val generated = generateQrBitmap(payload = "booking:$bookingId", sizePx = sizePx) ?: return null
            saveQrBitmap(context, bookingId, generated)
            generated
        } catch (_: Exception) {
            null
        }
    }

    fun deleteQrBitmap(context: Context, bookingId: Long) {
        val file = qrFile(context, bookingId)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun loadQrBitmap(context: Context, bookingId: Long): Bitmap? {
        val file = qrFile(context, bookingId)
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    private fun saveQrBitmap(context: Context, bookingId: Long, bitmap: Bitmap) {
        val dir = File(context.filesDir, QR_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val file = qrFile(context, bookingId)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    private fun qrFile(context: Context, bookingId: Long): File {
        val dir = File(context.filesDir, QR_DIR)
        return File(dir, "booking_$bookingId.png")
    }

    private fun generateQrBitmap(payload: String, sizePx: Int): Bitmap? {
        return try {
            val bitMatrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, sizePx, sizePx)
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
            for (x in 0 until sizePx) {
                for (y in 0 until sizePx) {
                    val color = if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                    bitmap.setPixel(x, y, color)
                }
            }
            bitmap
        } catch (_: Exception) {
            null
        }
    }
}

