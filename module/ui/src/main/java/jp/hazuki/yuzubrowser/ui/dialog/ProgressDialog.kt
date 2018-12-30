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

package jp.hazuki.yuzubrowser.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import jp.hazuki.yuzubrowser.ui.R


open class ProgressDialog : androidx.fragment.app.DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException()
        val arguments = arguments ?: throw IllegalArgumentException()

        val view = View.inflate(activity, R.layout.dialog_progress, null)

        view.findViewById<TextView>(R.id.progress_message).text = arguments.getString(ARG_MESSAGE)

        isCancelable = arguments.getBoolean(ARG_CANCELABLE)

        return AlertDialog.Builder(activity)
                .setView(view)
                .create()
                .apply {
                    setCanceledOnTouchOutside(arguments.getBoolean(ARG_CANCEL_TOUCH_OUTSIDE))
                }
    }

    companion object {
        private const val ARG_MESSAGE = "message"
        private const val ARG_CANCELABLE = "cancelable"
        private const val ARG_CANCEL_TOUCH_OUTSIDE = "cancelTouchOutside"

        operator fun invoke(message: String, cancelable: Boolean = true, cancelOnTouchOutside: Boolean = true): ProgressDialog {
            return ProgressDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_MESSAGE, message)
                    putBoolean(ARG_CANCELABLE, cancelable)
                    putBoolean(ARG_CANCEL_TOUCH_OUTSIDE, cancelOnTouchOutside)
                }
            }
        }
    }
}