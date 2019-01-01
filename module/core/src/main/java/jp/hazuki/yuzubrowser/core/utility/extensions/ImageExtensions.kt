/*
 * Copyright (C) 2017-2019 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.core.utility.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import androidx.annotation.RequiresApi

fun Drawable.getBitmap(): Bitmap {
    return if (this is BitmapDrawable) {
        bitmap
    } else if (this is VectorDrawable) {
        getBitmap()
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this is AdaptiveIconDrawable) {
        getBitmap()
    } else {
        throw IllegalArgumentException("unsupported drawable type : ${javaClass.name}")
    }
}

fun VectorDrawable.getBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
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