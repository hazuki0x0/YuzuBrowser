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

package jp.hazuki.yuzubrowser.legacy.pattern.action

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.widget.Toast
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.legacy.Constants.intent.EXTRA_OPEN_FROM_YUZU
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.pattern.PatternAction
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.ui.utils.PackageUtils
import java.io.IOException
import java.net.URISyntaxException

class OpenOthersPatternAction : PatternAction {
    var openType: Int = 0
        private set
    private var mUrl: String? = null

    override val typeId: Int
        get() = OPEN_OTHERS

    val intent: Intent?
        get() {
            try {
                return Intent.parseUri(mUrl, 0)
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }

            return null
        }

    constructor(intent: Intent) {
        openType = TYPE_NORMAL
        mUrl = intent.toUri(0)
    }

    constructor(type: Int) {
        openType = type
    }

    @Throws(IOException::class)
    constructor(reader: JsonReader) {
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                FIELD_TYPE -> openType = reader.nextInt()
                FIELD_INTENT -> {
                    if (reader.peek() == JsonReader.Token.STRING) {
                        mUrl = reader.nextString()
                    } else {
                        reader.skipValue()
                    }
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
    }

    @Throws(IOException::class)
    override fun write(writer: JsonWriter): Boolean {
        writer.value(OPEN_OTHERS)
        writer.beginObject()
        writer.name(FIELD_TYPE)
        writer.value(openType)
        writer.name(FIELD_INTENT)
        writer.value(mUrl)
        writer.endObject()
        return true
    }

    override fun getTitle(context: Context): String {
        when (openType) {
            TYPE_NORMAL -> {
                val pre = context.getString(R.string.pattern_open_others)
                try {
                    val pm = context.packageManager
                    return "$pre : ${pm.getActivityInfo(intent!!.component, 0).loadLabel(pm)}"
                } catch (e: NameNotFoundException) {
                    e.printStackTrace()
                }

                return pre
            }
            TYPE_APP_LIST -> return context.getString(R.string.pattern_open_app_list)
            TYPE_APP_CHOOSER -> return context.getString(R.string.pattern_open_app_chooser)
            else -> throw IllegalStateException()
        }
    }

    override fun run(context: Context, tab: MainTabData, url: String): Boolean {
        var intent: Intent?
        when (openType) {
            TYPE_NORMAL -> {
                intent = this.intent
                intent!!.data = Uri.parse(url)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            TYPE_APP_LIST -> {
                intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            TYPE_APP_CHOOSER -> {
                intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                intent = PackageUtils.createChooser(context, url, context.getText(R.string.open))
            }
            else -> throw IllegalStateException()
        }
        try {
            intent!!.putExtra(EXTRA_OPEN_FROM_YUZU, true)
            context.startActivity(intent)
            return true
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(context, R.string.app_notfound, Toast.LENGTH_SHORT).show()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            Toast.makeText(context, R.string.app_notfound, Toast.LENGTH_SHORT).show()
        }

        return false
    }

    companion object {
        private const val FIELD_TYPE = "0"
        private const val FIELD_INTENT = "1"
        const val TYPE_NORMAL = 0
        const val TYPE_APP_LIST = 1
        const val TYPE_APP_CHOOSER = 2
    }
}
