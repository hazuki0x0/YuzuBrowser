/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.useragent

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.utils.extensions.getFakeChromeUserAgent

class UserAgentListDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException()
        val mUserAgentList = UserAgentList()
        mUserAgentList.read(activity)

        val entries = arrayOfNulls<String>(mUserAgentList.size + 1)
        val entryValues = arrayOfNulls<String>(mUserAgentList.size + 1)

        var ua = arguments!!.getString(UA)
        val defaultUserAgent = if (AppData.fake_chrome.get()) activity.getFakeChromeUserAgent() else ""

        if (ua == null) ua = ""

        var pos = if (defaultUserAgent == ua) 0 else -1

        entries[0] = context!!.getString(R.string.default_text)
        entryValues[0] = defaultUserAgent

        var userAgent: UserAgent

        var i = 1
        while (mUserAgentList.size > i - 1) {
            userAgent = mUserAgentList[i - 1]
            entries[i] = userAgent.name
            entryValues[i] = userAgent.useragent
            if (ua == userAgent.useragent) {
                pos = i
            }
            i++
        }

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.useragent)
                .setSingleChoiceItems(entries, pos) { _, which ->
                    val intent = Intent()
                    intent.putExtra(Intent.EXTRA_TEXT, entryValues[which])
                    activity.setResult(RESULT_OK, intent)
                    dismiss()
                }
                .setNegativeButton(R.string.cancel, null)
        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        activity?.finish()
    }

    companion object {

        private const val UA = "ua"

        fun newInstance(userAgent: String): UserAgentListDialog {
            val dialog = UserAgentListDialog()
            val bundle = Bundle()
            bundle.putString(UA, userAgent)
            dialog.arguments = bundle
            return dialog
        }
    }
}
