package com.android.picture_rotation_library

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.os.Bundle
import com.android.picture_rotation_library.Constants.Companion.BUNDLE
import com.android.picture_rotation_library.Constants.Companion.NAVIGATION_URL
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import java.net.URL

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

    fun payout(context: Activity, url: String) {
        val intent = Intent(context, WebActivity::class.java)
        val bundle = Bundle()
        bundle.putString(NAVIGATION_URL, url)
        intent.putExtra(BUNDLE, bundle)
        context.startActivity(intent)
    }
}