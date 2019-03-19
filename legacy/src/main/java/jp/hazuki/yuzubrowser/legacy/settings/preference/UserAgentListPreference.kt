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

package jp.hazuki.yuzubrowser.legacy.settings.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.useragent.UserAgent
import jp.hazuki.yuzubrowser.legacy.useragent.UserAgentList
import jp.hazuki.yuzubrowser.ui.BrowserApplication

class UserAgentListPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ListPreference(context, attrs) {

    override fun onClick() {
        init(context)
        super.onClick()
    }

    private fun init(context: Context) {
        val mUserAgentList = UserAgentList()
        val moshi = (context.applicationContext as BrowserApplication).moshi
        mUserAgentList.read(context, moshi)

        val entries = arrayOfNulls<String>(mUserAgentList.size + 1)
        val entryValues = arrayOfNulls<String>(mUserAgentList.size + 1)

        entries[0] = getContext().getString(R.string.default_text)
        entryValues[0] = ""

        var userAgent: UserAgent

        var i = 1
        while (mUserAgentList.size > i - 1) {
            userAgent = mUserAgentList[i - 1]
            entries[i] = userAgent.name
            entryValues[i] = userAgent.useragent
            i++
        }

        setEntries(entries)
        setEntryValues(entryValues)
    }
}
