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

package jp.hazuki.yuzubrowser.legacy.speeddial

import android.content.Context
import android.webkit.WebResourceResponse
import jp.hazuki.yuzubrowser.core.utility.extensions.getNoCacheResponse
import jp.hazuki.yuzubrowser.core.utility.utils.FileUtils
import jp.hazuki.yuzubrowser.core.utility.utils.IOUtils
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.utils.HtmlUtils
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import java.io.*
import java.nio.charset.StandardCharsets

class SpeedDialHtml(context: Context) {

    private val cache: File
    private val cacheIndex: File

    private val manager: SpeedDialManager
    private val context: Context = context.applicationContext

    init {
        val cacheFolder = File(context.cacheDir, FOLDER)

        cache = File(cacheFolder, "cache")
        cacheIndex = File(cacheFolder, "index")

        manager = SpeedDialManager(context.applicationContext)
    }

    val baseCss: WebResourceResponse
        get() = WebResourceResponse("text/css", "UTF-8", context.resources.openRawResource(R.raw.speeddial_css))

    val customCss: WebResourceResponse
        get() {
            val builder = StringBuilder(400)

            if (!AppPrefs.speeddial_show_header.get())
                builder.append(".browserName{display:none}")

            if (!AppPrefs.speeddial_show_search.get())
                builder.append("#searchBox{display:none}")

            if (!AppPrefs.speeddial_show_icon.get())
                builder.append(".box img{display:none;}")

            // portrait
            val pColumn = AppPrefs.speeddial_column.get()
            builder.append("@media screen and (orientation:portrait){.linkBox{max-width:")
                .append(AppPrefs.speeddial_column_width.get() * pColumn).append("px}")
            // = (100 - marginSize * (column + 1) * 2 + 2 * marginSize) / column
            builder.append(".box{width:").append((100f - 2f * pColumn) / pColumn).append("%}")
            builder.append(".box:nth-child(n+").append(pColumn + 1).append("){order:1}}")

            // landscape
            val lColumn = AppPrefs.speeddial_column_landscape.get()
            builder.append("@media screen and (orientation:landscape){.linkBox{max-width:")
                .append(AppPrefs.speeddial_column_width.get() * lColumn).append("px}")
            // = (100 - marginSize * (column + 1) * 2 + 2 * marginSize) / column
            builder.append(".box{width:").append((100f - 2f * lColumn) / lColumn).append("%}")
            builder.append(".box:nth-child(n+").append(lColumn + 1).append("){order:1}}")

            val fontSize = AppPrefs.font_size.speeddial_item.get()
            if (fontSize >= 0) {
                builder.append(".name{font-size:").append(getFontSize(fontSize)).append("}")
            }

            if (AppPrefs.speeddial_dark_theme.get()) {
                builder.append(DARK_THEME)
            }

            when (AppPrefs.speeddial_layout.get()) {
                1 -> builder.append(SMALL_ICON)
            }

            return getNoCacheResponse("text/css", builder)
        }

    private fun createCache(context: Context): CharSequence {
        val time = System.currentTimeMillis()
        val index = manager.indexData
        val builder = StringBuilder(8000)
        val start = getResourceString(context, R.raw.speeddial_start)
        builder.append(start)

        for ((id, url, title, updateTime) in index) {
            builder.append("<div class=\"box\"><a href=\"")
                    .append(url)
                    .append("\"><img src=\"yuzu:speeddial/img/")
                    .append(id)
                    .append("?")
                    .append(updateTime)
                    .append("\" /><div class=\"name\">")
                    .append(HtmlUtils.sanitize(title))
                    .append("</div></a></div>")
        }

        builder.append(getResourceString(context, R.raw.speeddial_end))

        if (!cache.parentFile.exists())
            cache.parentFile.mkdirs()

        try {
            FileOutputStream(cache).use { fos -> fos.write(builder.toString().toByteArray(StandardCharsets.UTF_8)) }
        } catch (e: IOException) {
            e.printStackTrace()
            return builder
        }

        try {
            FileOutputStream(cacheIndex).use { fos -> fos.write(time.toString().toByteArray(StandardCharsets.UTF_8)) }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return builder
    }

    fun createResponse(): WebResourceResponse {

        if (cacheIndex.exists() && cache.exists()) {
            try {
                val time = IOUtils.readFile(cacheIndex, "UTF-8").toLong()
                if (manager.listUpdateTime < time) {
                    return getNoCacheResponse("text/html", FileInputStream(cache))
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return getNoCacheResponse("text/html", createCache(context))
    }

    fun getImage(path: String): WebResourceResponse? {
        val index = path.indexOf('?')
        if (index < 14) return null

        val id = path.substring(14, index)
        val image = manager.getImage(id) ?: return null

        return WebResourceResponse("image/png", null, ByteArrayInputStream(image))
    }

    private fun getResourceString(context: Context, id: Int): String {
        return IOUtils.readString(context.resources.openRawResource(id))
    }

    private fun getFontSize(size: Int): String {
        return when (size) {
            0 -> "42%"
            1 -> "50%"
            2 -> "60%"
            3 -> "75%"
            4 -> "88.8%"
            5 -> "100%"
            6 -> "120%"
            else -> "75%"
        }
    }

    companion object {
        private const val FOLDER = "speeddial"

        private const val DARK_THEME = "body{background-color:#2a2a2a}.browserName{color:#f0f0f0}" +
                ".search{color:#fff;box-shadow:2px 2px 6px rgba(0,0,0,.4);background-color:#707070}" +
                ".box,.search{border:1px solid #101010}.box{background-color:#404040}" +
                ".name{color:#f5f5f5}footer a{color:#afafaf}"

        private const val SMALL_ICON = ".box{position:relative}img{position:absolute;top:0;bottom:0;" +
                "width:20px;margin:auto 3px}.name{width:auto;-webkit-line-clamp:1;margin:10px 0 10px 26px}"

        @JvmStatic
        fun clearCache(context: Context) {
            FileUtils.deleteFile(File(context.cacheDir, FOLDER))
        }
    }
}
