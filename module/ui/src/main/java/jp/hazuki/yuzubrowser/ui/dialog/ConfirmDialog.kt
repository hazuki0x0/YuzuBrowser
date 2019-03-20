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

package jp.hazuki.yuzubrowser.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ConfirmDialog : DialogFragment() {

    private var listener: OnConfirmedListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = arguments ?: throw IllegalArgumentException()

        val id = arguments.getInt(ID)
        return AlertDialog.Builder(activity)
                .setTitle(arguments.getString(TITLE))
                .setMessage(arguments.getString(MESSAGE))
                .setPositiveButton(android.R.string.yes) { _, _ -> listener?.onConfirmed(id) }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnConfirmedListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnConfirmedListener {
        fun onConfirmed(id: Int)
    }

    companion object {
        private const val ID = "id"
        private const val TITLE = "title"
        private const val MESSAGE = "mes"

        operator fun invoke(id: Int, title: String, message: String): ConfirmDialog {
            return ConfirmDialog().apply {
                arguments = Bundle().apply {
                    putInt(ID, id)
                    putString(TITLE, title)
                    putString(MESSAGE, message)
                }
            }
        }
    }
}