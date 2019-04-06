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

package jp.hazuki.yuzubrowser.legacy.action.item.startactivity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.core.utility.utils.ImageUtils
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.legacy.utils.graphics.LauncherIconDrawable
import jp.hazuki.yuzubrowser.ui.app.StartActivityInfo
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.URISyntaxException

class StartActivitySingleAction : SingleAction, Parcelable {
    private var mIntent: Intent? = null
    private var mName: String? = null
    private var mIcon: Bitmap? = null
    private var mIconCache: WeakReference<Drawable>? = null

    @Throws(IOException::class)
    constructor(id: Int, reader: JsonReader?) : super(id) {
        if (reader != null) {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
            reader.beginObject()
            while (reader.hasNext()) {
                if (reader.peek() != JsonReader.Token.NAME) return
                when (reader.nextName()) {
                    FIELD_NAME_INTENT -> {
                        if (reader.peek() == JsonReader.Token.NULL) {
                            reader.skipValue()
                        } else {
                            try {
                                mIntent = Intent.parseUri(reader.nextString(), 0)
                            } catch (e: URISyntaxException) {
                                ErrorReport.printAndWriteLog(e)
                            }
                        }
                    }
                    FIELD_NAME_ACTION_NAME -> {
                        if (reader.peek() == JsonReader.Token.STRING) {
                            mName = reader.nextString()
                        } else {
                            reader.skipValue()
                        }
                    }
                    FIELD_NAME_ACTION_ICON -> {
                        if (reader.peek() == JsonReader.Token.STRING) {
                            mIcon = ImageUtils.convertToBitmap(Base64.decode(reader.nextString(), Base64.DEFAULT))
                        } else {
                            reader.skipValue()
                        }
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        }
    }

    @Throws(IOException::class)
    override fun writeIdAndData(writer: JsonWriter) {
        writer.value(id)
        writer.beginObject()
        writer.name(FIELD_NAME_INTENT)
        writer.value(mIntent?.toUri(0))
        val name = mName
        if (name != null) {
            writer.name(FIELD_NAME_ACTION_NAME)
            writer.value(name)
        }
        if (mIcon != null) {
            val icon = Base64.encodeToString(ImageUtils.convertToByteArray(mIcon), Base64.DEFAULT)
            writer.name(FIELD_NAME_ACTION_ICON)
            writer.value(icon)
        }
        writer.endObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeParcelable(mIntent, flags)
        dest.writeString(mName)
        dest.writeParcelable(mIcon, flags)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        mIntent = source.readParcelable(Intent::class.java.classLoader)
        mName = source.readString()
        mIcon = source.readParcelable(Bitmap::class.java.classLoader)
    }

    override fun showMainPreference(context: ActionActivity): StartActivityInfo? {
        return showSubPreference(context)
    }

    @Suppress("DEPRECATION")
    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val intent = Intent(context.applicationContext, StartActivityPreferenceActivity::class.java)
        intent.putExtra(Intent.EXTRA_INTENT, mIntent)
        return StartActivityInfo(intent) { _, resultCode, data ->
            if (resultCode != Activity.RESULT_OK || data == null)
                return@StartActivityInfo
            val name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)
            var icon: Bitmap? = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON)
            if (icon == null) {
                val iconRes = data.getParcelableExtra<ShortcutIconResource>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
                if (iconRes != null) {
                    try {
                        val foreignResources = context.packageManager.getResourcesForApplication(iconRes.packageName)
                        val id = foreignResources.getIdentifier(iconRes.resourceName, null, null)
                        icon = BitmapFactory.decodeResource(foreignResources, id)
                    } catch (e: NameNotFoundException) {
                        e.printStackTrace()
                    }

                }
            }
            val sIntent = data.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)
                    ?: return@StartActivityInfo
            mName = name
            mIcon?.recycle()
            mIcon = icon
            mIntent = sIntent
            mIconCache = null
        }
    }

    fun getIntent(tab: MainTabData): Intent? {
        if (mIntent == null) return null

        val intent = Intent(mIntent)
        var data = intent.dataString

        if (data != null) {
            val type = intent.type
            data = replaceString(tab, data)
            intent.setDataAndType(Uri.parse(data), type)
        }

        val extras = intent.extras
        if (extras != null) {
            for (key in extras.keySet()) {
                val obj = extras.get(key)
                if (obj is CharSequence) {
                    intent.putExtra(key, replaceString(tab, obj.toString()))
                }
            }
        }
        return intent
    }

    fun getIconDrawable(context: Context): Drawable? {
        if (mIntent == null)
            return null

        var icon: Drawable? = mIconCache?.get()
        if (icon == null) {
            icon = if (mIcon == null) {
                try {
                    LauncherIconDrawable(context.packageManager.getActivityIcon(mIntent!!.component))//Does not return null.
                } catch (e: NameNotFoundException) {
                    e.printStackTrace()
                    return null
                }
            } else {
                LauncherIconDrawable(BitmapDrawable(context.resources, mIcon))
            }
            mIconCache = WeakReference(icon)
        }
        return icon
    }

    companion object {
        private const val TAG = "StartActivitySingleAction"
        private const val FIELD_NAME_INTENT = "0"
        private const val FIELD_NAME_ACTION_NAME = "1"
        private const val FIELD_NAME_ACTION_ICON = "2"
        const val REPLACE_URI = "http://jp.hazuki.yuzubrowser/REPLACE_URI"
        const val REPLACE_TITLE = "http://jp.hazuki.yuzubrowser/REPLACE_TITLE"

        @JvmField
        val CREATOR: Parcelable.Creator<StartActivitySingleAction> = object : Parcelable.Creator<StartActivitySingleAction> {
            override fun createFromParcel(source: Parcel): StartActivitySingleAction {
                return StartActivitySingleAction(source)
            }

            override fun newArray(size: Int): Array<StartActivitySingleAction?> {
                return arrayOfNulls(size)
            }
        }

        private fun replaceString(tab: MainTabData, data: String): String {
            var result = data
            if (tab.url != null)
                result = result.replace(REPLACE_URI, tab.url)
            if (tab.title != null)
                result = result.replace(REPLACE_TITLE, tab.title)
            return result
        }
    }
}
