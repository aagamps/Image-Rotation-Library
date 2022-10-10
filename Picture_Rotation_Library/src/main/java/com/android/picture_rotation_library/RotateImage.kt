package com.android.picture_rotation_library

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import com.bumptech.glide.load.resource.bitmap.TransformationUtils

class RotateImage {

    internal fun rotateImage(imagePath: String): Bitmap? {

        val options = BitmapFactory.Options()
        options.inSampleSize = 4
        val bitmap = BitmapFactory.decodeFile(imagePath, options)

        val orientation: Int = ExifInterface(imagePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> TransformationUtils.rotateImage(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> TransformationUtils.rotateImage(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> TransformationUtils.rotateImage(bitmap, 270)
            ExifInterface.ORIENTATION_NORMAL -> bitmap
            else -> bitmap
        }
    }

    internal fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}