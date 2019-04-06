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

package jp.hazuki.yuzubrowser.legacy.userjs

import android.os.Parcel
import android.os.Parcelable
import jp.hazuki.yuzubrowser.core.utility.extensions.forEachLine
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader
import java.util.regex.Pattern

class UserScript : Parcelable {

    val info: UserScriptInfo

    var name: String? = null
    var version: String? = null
    var author: String? = null
    var description: String? = null
    val include = ArrayList<Pattern>(0)
    val exclude = ArrayList<Pattern>(0)
    var isUnwrap: Boolean = false
        private set
    var runAt: RunAt = RunAt.END

    var id: Long
        get() = info.id
        set(id) {
            info.id = id
        }

    var data: String
        get() = info.data
        set(data) {
            info.data = data
            loadHeaderData()
        }

    val runnable: String
        get() = if (isUnwrap) {
            info.data
        } else {
            "(function() {\n${info.data}\n})()"
        }

    var isEnabled: Boolean
        get() = info.isEnabled
        set(enabled) {
            info.isEnabled = enabled
        }

    constructor() {
        info = UserScriptInfo()
    }

    constructor(id: Long, data: String, enabled: Boolean) {
        info = UserScriptInfo(id, data, enabled)
        loadHeaderData()
    }

    constructor(data: String) {
        info = UserScriptInfo(data)
        loadHeaderData()
    }

    constructor(info: UserScriptInfo) {
        this.info = info
        loadHeaderData()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(info.id)
        dest.writeString(info.data)
        dest.writeInt(if (info.isEnabled) 1 else 0)
    }

    constructor(source: Parcel) {
        val id = source.readLong()
        val data = source.readString()
        val enabled = source.readInt() == 1
        info = UserScriptInfo(id, data, enabled)
        loadHeaderData()
    }

    private fun loadHeaderData() {
        name = null
        version = null
        description = null
        include.clear()
        exclude.clear()

        try {
            val reader = BufferedReader(StringReader(info.data))

            if (reader.readLine()?.let { sHeaderStartPattern.matcher(it).matches() } != true) {
                Logger.w(TAG, "Header (start) parser error")
                return
            }

            reader.forEachLine { line ->
                val matcher = sHeaderMainPattern.matcher(line)
                if (!matcher.matches()) {
                    if (sHeaderEndPattern.matcher(line).matches()) {
                        return
                    }
                    Logger.w(TAG, "Unknown header : $line")
                } else {
                    val field = matcher.group(1)
                    val value = matcher.group(2)
                    readData(field, value, line)
                }
            }

            Logger.w(TAG, "Header (end) parser error")
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }

    }

    private fun readData(field: String?, value: String?, line: String) {
        if ("name".equals(field, ignoreCase = true)) {
            name = value
        } else if ("version".equals(field, ignoreCase = true)) {
            version = value
        } else if ("author".equals(field, ignoreCase = true)) {
            author = value
        } else if ("description".equals(field, ignoreCase = true)) {
            description = value
        } else if ("include".equals(field, ignoreCase = true)) {
            makeUrlPattern(value)?.let {
                include.add(it)
            }
        } else if ("exclude".equals(field, ignoreCase = true)) {
            makeUrlPattern(value)?.let {
                exclude.add(it)
            }
        } else if ("unwrap".equals(field, ignoreCase = true)) {
            isUnwrap = true
        } else if ("run-at".equals(field, ignoreCase = true)) {
            runAt = when (value) {
                "document-start" -> RunAt.START
                "document-idle" -> RunAt.IDLE
                else -> RunAt.END
            }
        } else if ("match".equals(field, ignoreCase = true) && value != null) {
            val patternUrl = "^" + value.replace("?", "\\?").replace(".", "\\.")
                    .replace("*", ".*").replace("+", ".+")
                    .replace("://.*\\.", "://((?![\\./]).)*\\.").replace("^\\.\\*://".toRegex(), "https?://")
            makeUrlPatternParsed(patternUrl)?.let {
                include.add(it)
            }
        } else {
            Logger.w(TAG, "Unknown header : $line")
        }
    }

    enum class RunAt {
        START,
        END,
        IDLE
    }

    companion object {
        private const val TAG = "UserScript"

        @JvmField
        val CREATOR: Parcelable.Creator<UserScript> = object : Parcelable.Creator<UserScript> {
            override fun createFromParcel(source: Parcel): UserScript = UserScript(source)

            override fun newArray(size: Int): Array<UserScript?> = arrayOfNulls(size)
        }

        private val sHeaderStartPattern = Pattern.compile("\\s*//\\s*==UserScript==\\s*", Pattern.CASE_INSENSITIVE)
        private val sHeaderEndPattern = Pattern.compile("\\s*//\\s*==/UserScript==\\s*", Pattern.CASE_INSENSITIVE)
        private val sHeaderMainPattern = Pattern.compile("\\s*//\\s*@(\\S+)(?:\\s+(.*))?", Pattern.CASE_INSENSITIVE)
    }
}
