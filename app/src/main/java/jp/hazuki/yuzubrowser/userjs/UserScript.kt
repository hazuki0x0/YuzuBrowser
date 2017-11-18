package jp.hazuki.yuzubrowser.userjs

import android.os.Parcel
import android.os.Parcelable
import jp.hazuki.yuzubrowser.utils.ErrorReport
import jp.hazuki.yuzubrowser.utils.Logger
import jp.hazuki.yuzubrowser.utils.WebUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader
import java.util.*
import java.util.regex.Pattern

class UserScript : Parcelable {

    val info: UserScriptInfo

    var name: String? = null
    var version: String? = null
    var author: String? = null
    var description: String? = null
    var include: MutableList<Pattern>? = null
        private set
    var exclude: MutableList<Pattern>? = null
        private set
    var isUnwrap: Boolean = false
        private set
    var isRunStart: Boolean = false

    var id: Long
        get() = info.id
        set(id) {
            info.id = id
        }

    var data: String
        get() = info.data
        set(data) {
            info.data = data
            loadData()
        }

    val runnable: String
        get() = if (isUnwrap) {
            info.data
        } else {
            "(function() {\n" + info.data + "\n})()"
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
        loadData()
    }

    constructor(data: String) {
        info = UserScriptInfo(data)
        loadData()
    }

    constructor(info: UserScriptInfo) {
        this.info = info
        loadData()
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
        loadData()
    }

    private fun loadData() {
        name = null
        version = null
        description = null
        include = null
        exclude = null

        try {
            val reader = BufferedReader(StringReader(info.data))
            var line = ""

            if (reader.readLine().also { line = it } == null || !sHeaderStartPattern.matcher(line).matches()) {
                Logger.w(TAG, "Header (start) parser error")
                return
            }

            while (reader.readLine().also { line = it } != null) {
                val matcher = sHeaderMainPattern.matcher(line)
                if (!matcher.matches()) {
                    if (sHeaderEndPattern.matcher(line).matches()) {
                        return
                    }
                    Logger.w(TAG, "Unknown header : " + line)
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

    private fun readData(field: String, value: String, line: String) {
        if ("name".equals(field, ignoreCase = true)) {
            name = value
        } else if ("version".equals(field, ignoreCase = true)) {
            version = value
        } else if ("author".equals(field, ignoreCase = true)) {
            author = value
        } else if ("description".equals(field, ignoreCase = true)) {
            description = value
        } else if ("include".equals(field, ignoreCase = true)) {
            if (include == null)
                include = ArrayList()
            val pattern = WebUtils.makeUrlPattern(value)
            if (pattern != null)
                include!!.add(pattern)
        } else if ("exclude".equals(field, ignoreCase = true)) {
            if (exclude == null)
                exclude = ArrayList()
            val pattern = WebUtils.makeUrlPattern(value)
            if (pattern != null)
                exclude!!.add(pattern)
        } else if ("unwrap".equals(field, ignoreCase = true)) {
            isUnwrap = true
        } else if ("run-at".equals(field, ignoreCase = true)) {
            isRunStart = "document-start".equals(value, ignoreCase = true)
        } else if ("match".equals(field, ignoreCase = true)) {
            if (include == null)
                include = ArrayList()
            val patternUrl = "?^" + value.replace("?", "\\?").replace(".", "\\.")
                    .replace("*", ".*").replace("+", ".+")
                    .replace("://.*\\.", "://((?![\\./]).)*\\.").replace("^\\.\\*://".toRegex(), "https?://")
            val pattern = WebUtils.makeUrlPattern(patternUrl)
            if (pattern != null)
                include!!.add(pattern)
        } else {
            Logger.w(TAG, "Unknown header : " + line)
        }
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
