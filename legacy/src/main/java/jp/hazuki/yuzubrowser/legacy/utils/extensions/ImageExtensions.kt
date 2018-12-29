package jp.hazuki.yuzubrowser.legacy.utils.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.support.annotation.RequiresApi
import jp.hazuki.yuzubrowser.legacy.utils.ImageUtils

fun Drawable.getBitmap(): Bitmap {
    return if (this is BitmapDrawable) {
        bitmap
    } else if (this is VectorDrawable) {
        ImageUtils.getBitmap(this)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this is AdaptiveIconDrawable) {
        getBitmap()
    } else {
        throw IllegalArgumentException("unsupported drawable type : ${javaClass.name}")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun AdaptiveIconDrawable.getBitmap(): Bitmap {
    val width = intrinsicWidth
    val height = intrinsicHeight

    val icon = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val iconCanvas = Canvas(icon)

    setBounds(0, 0, iconCanvas.width, iconCanvas.height)
    draw(iconCanvas)

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    canvas.clipPath(iconMask)
    canvas.drawBitmap(icon, 0f, 0f, null)
    return bitmap
}