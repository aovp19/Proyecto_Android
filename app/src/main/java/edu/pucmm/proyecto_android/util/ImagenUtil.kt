package edu.pucmm.proyecto_android.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import android.graphics.Matrix
import android.media.ExifInterface

object ImagenUtil {
    private const val MAX_LADO = 800
    private const val MAX_BYTES = 400_000

    fun comprimir(resolver: ContentResolver, uri: Uri): ByteArray? {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }

        var muestra = 1
        while (limites.outWidth / muestra > MAX_LADO * 2 || limites.outHeight / muestra > MAX_LADO * 2) {
            muestra *= 2
        }

        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = muestra })
        } ?: return null

        val escala = minOf(1f, MAX_LADO.toFloat() / maxOf(bitmap.width, bitmap.height))
        val reducido = if (escala < 1f) {
            Bitmap.createScaledBitmap(
                bitmap, (bitmap.width * escala).toInt(), (bitmap.height * escala).toInt(), true
            )
        } else bitmap

        val rotacion = leerRotacion(resolver, uri)
        val girado = if (rotacion != 0f) {
            val matriz = Matrix().apply { postRotate(rotacion) }
            Bitmap.createBitmap(reducido, 0, 0, reducido.width, reducido.height, matriz, true)
        } else reducido

        var calidad = 70
        var bytes: ByteArray
        do {
            val salida = ByteArrayOutputStream()
            girado.compress(Bitmap.CompressFormat.JPEG, calidad, salida)
            bytes = salida.toByteArray()
            calidad -= 10
        } while (bytes.size > MAX_BYTES && calidad >= 20)

        return bytes
    }

    // lee cuantos grados hay que girar la imagen segun su metadato EXIF
    private fun leerRotacion(resolver: ContentResolver, uri: Uri): Float {
        val orientacion = resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

        return when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }
}