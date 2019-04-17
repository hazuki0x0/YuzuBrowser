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

package jp.hazuki.yuzubrowser.adblock.ui.abp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import jp.hazuki.yuzubrowser.adblock.R
import jp.hazuki.yuzubrowser.adblock.repository.abp.AbpEntity

class AddAbpDialog : DialogFragment() {
    private var listener: OnAddItemListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException()
        val arguments = arguments ?: throw IllegalArgumentException()

        val view = View.inflate(activity, R.layout.add_abp_dialog, null)
        val editText = view.findViewById<EditText>(R.id.urlEditText)
        editText.setSingleLine(true)
        editText.inputType = EditorInfo.TYPE_TEXT_VARIATION_URI

        val entity = arguments.getParcelable<AbpEntity?>(ARG_ENTITY)
        entity?.let { editText.setText(it.url) }

        return AlertDialog.Builder(activity)
                .setTitle(getString(if (entity != null) R.string.resblock_edit else R.string.add))
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    if (entity != null) {
                        entity.url = editText.text.toString()
                        listener?.onAddEntity(entity)
                    } else {
                        listener?.onAddEntity(AbpEntity(url = editText.text.toString()))
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (parentFragment is OnAddItemListener) {
            parentFragment as OnAddItemListener
        } else {
            activity as? OnAddItemListener
        }

    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnAddItemListener {
        fun onAddEntity(entity: AbpEntity)
    }

    companion object {
        private const val ARG_ENTITY = "entity"

        fun create(abpEntity: AbpEntity?): AddAbpDialog {
            return AddAbpDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ENTITY, abpEntity)
                }
            }
        }
    }
}
