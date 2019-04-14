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

package jp.hazuki.yuzubrowser.adblock.ui.original

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import jp.hazuki.yuzubrowser.adblock.repository.original.AdBlock
import jp.hazuki.yuzubrowser.core.utility.extensions.density

class AdBlockEditDialog : DialogFragment() {

    private var listener: AdBlockEditDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        val density = activity!!.density
        val marginWidth = (4 * density + 0.5f).toInt()
        val marginHeight = (16 * density + 0.5f).toInt()
        params.setMargins(marginWidth, marginHeight, marginWidth, marginHeight)
        val editText = EditText(activity).apply {
            layoutParams = params
            id = android.R.id.edit
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val arguments = arguments ?: throw NullPointerException()
        val text = arguments.getString(ARG_TEXT)
        if (!text.isNullOrEmpty())
            editText.setText(text)

        return AlertDialog.Builder(activity)
                .setView(editText)
                .setTitle(arguments.getString(ARG_TITLE))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    listener!!.onEdited(
                            arguments.getInt(ARG_INDEX, -1),
                            arguments.getInt(ARG_ID, -1),
                            editText.text.toString())
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (parentFragment is AdBlockEditDialogListener) {
            parentFragment as AdBlockEditDialogListener
        } else {
            activity as AdBlockEditDialogListener
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    internal interface AdBlockEditDialogListener {
        fun onEdited(index: Int, id: Int, text: String)
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_INDEX = "index"
        private const val ARG_ID = "id"
        private const val ARG_TEXT = "text"

        operator fun invoke(title: String, index: Int = -1, adBlock: AdBlock? = null): AdBlockEditDialog {
            return AdBlockEditDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putInt(ARG_INDEX, index)
                    if (adBlock != null) {
                        putInt(ARG_ID, adBlock.id)
                        putString(ARG_TEXT, adBlock.match)
                    }
                }
            }
        }
    }
}
