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

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import android.util.TypedValue
import android.view.WindowManager
import android.webkit.WebSettings
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import java.io.File

@ColorInt
fun Context.getResColor(@ColorRes id: Int): Int {
    return if (Build.VERSION.SDK_INT >= 23) {
        resources.getColor(id, theme)
    } else {
        @Suppress("DEPRECATION")
        resources.getColor(id)
    }
}

fun Context.getThemeResId(@AttrRes id: Int): Int {
    val outValue = TypedValue()
    theme.resolveAttribute(id, outValue, true)
    return outValue.resourceId
}

fun Context.dimension(@DimenRes id: Int): Int = resources.getDimensionPixelSize(id)

var Context.clipboardText: String
    get() {
        val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = manager.primaryClip ?: return ""
        return clip.getItemAt(0).text?.toString() ?: ""
    }
    set(text) {
        val clipData = ClipData("text_data", arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), ClipData.Item(text))
        val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.primaryClip = clipData
    }

fun Context.convertDpToPx(dp: Int): Int = (resources.displayMetrics.density * dp + 0.5f).toInt()

fun Context.convertDpToFloatPx(dp: Int): Float = resources.displayMetrics.density * dp + 0.5f

val Context.density: Float
    get() = resources.displayMetrics.density

fun Context.getFakeChromeUserAgent(): String {
    val ua = StringBuilder(WebSettings.getDefaultUserAgent(this))
    ua.replace("; wv", "")
    ua.replace("Version/4.0 ", "")
    return ua.toString()
}

fun Context.readAssetsText(fileName: String): String {
    return assets.open(fileName).reader().use { it.readText() }
}

fun Context.getVersionName(): String {
    val info = packageManager.getPackageInfo(packageName, 0)
    return info.versionName
}

fun Context.getVersionCode(): Int {
    val info = packageManager.getPackageInfo(packageName, 0)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        info.longVersionCode.toInt()
    } else {
        @Suppress("DEPRECATION")
        info.versionCode
    }
}

fun Context.getBitmap(drawableId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(this, drawableId)!!
    return drawable.getBitmap()
}

fun Context.getDisplayHeight(): Int {
    val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    val point = Point()
    display.getSize(point)
    return point.y
}

val Context.appCacheFile: File
    get() = getDir("appcache", Context.MODE_PRIVATE)

val Context.appCacheFilePath: String
    get() = appCacheFile.absolutePath