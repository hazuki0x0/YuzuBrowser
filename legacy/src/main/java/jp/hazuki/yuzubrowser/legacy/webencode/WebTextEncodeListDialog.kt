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

package jp.hazuki.yuzubrowser.legacy.webencode

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.squareup.moshi.Moshi
import dagger.android.support.AndroidSupportInjection
import jp.hazuki.yuzubrowser.legacy.R
import javax.inject.Inject

class WebTextEncodeListDialog : DialogFragment() {

    @Inject
    lateinit var moshi: Moshi

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException()
        AndroidSupportInjection.inject(this)

        val encodes = WebTextEncodeList()
        encodes.read(activity, moshi)

        val entries = arrayOfNulls<String>(encodes.size)

        var now = arguments!!.getString(ENCODING)

        if (now == null) now = ""

        var pos = -1

        var encode: WebTextEncode
        var i = 0
        while (encodes.size > i) {
            encode = encodes[i]
            entries[i] = encode.encoding
            if (now == encode.encoding) {
                pos = i
            }
            i++
        }

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.web_encode)
                .setSingleChoiceItems(entries, pos) { _, which ->
                    val intent = Intent()
                    intent.putExtra(Intent.EXTRA_TEXT, entries[which])
                    activity.setResult(RESULT_OK, intent)
                    dismiss()
                }
                .setNegativeButton(R.string.cancel, null)
        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val activity = activity
        activity?.finish()
    }

    companion object {

        private const val ENCODING = "enc"

        fun newInstance(webTextEncode: String): WebTextEncodeListDialog {
            val dialog = WebTextEncodeListDialog()
            val bundle = Bundle()
            bundle.putString(ENCODING, webTextEncode)
            dialog.arguments = bundle
            return dialog
        }
    }
}
