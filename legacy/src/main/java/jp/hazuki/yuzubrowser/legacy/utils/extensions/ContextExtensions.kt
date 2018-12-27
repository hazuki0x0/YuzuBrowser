/*
 * Copyright (C) 2017-2018 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.utils.extensions

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.webkit.WebSettings
import android.widget.Toast
import jp.hazuki.yuzubrowser.legacy.BrowserApplication
import jp.hazuki.yuzubrowser.legacy.R

@ColorInt
fun Context.getResColor(@ColorRes id: Int): Int {
    return if (Build.VERSION.SDK_INT >= 23) {
        resources.getColor(id, theme)
    } else {
        @Suppress("DEPRECATION")
        resources.getColor(id)
    }
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

fun Context.setClipboardWithToast(text: String?) {
    if (text == null) return

    clipboardText = text
    Toast.makeText(this, getString(R.string.copy_clipboard_mes_before) + text, Toast.LENGTH_SHORT).show()
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

inline val Context.browserApplicationContext: BrowserApplication
    get() = applicationContext as BrowserApplication

fun Context.getVersionName(): String {
    val info = packageManager.getPackageInfo(packageName, 0)
    return info.versionName
}

fun Context.getVersionCode(): Int {
    val info = packageManager.getPackageInfo(packageName, 0)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        info.longVersionCode.toInt()
    } else {
        info.versionCode
    }

}