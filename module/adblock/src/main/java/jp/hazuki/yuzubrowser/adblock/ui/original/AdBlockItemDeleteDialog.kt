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
import jp.hazuki.yuzubrowser.adblock.R

class AdBlockItemDeleteDialog : androidx.fragment.app.DialogFragment() {

    private var listener: OnBlockItemDeleteListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = arguments ?: throw IllegalArgumentException()
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.pref_delete)
        builder.setMessage(getString(R.string.pref_ad_block_delete_confirm, arguments.getString(ARG_ITEM)))
        builder.setPositiveButton(android.R.string.yes) { _, _ -> listener!!.onDelete(arguments.getInt(ARG_INDEX), arguments.getInt(ARG_ID)) }
        builder.setNegativeButton(android.R.string.no, null)
        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnBlockItemDeleteListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    internal interface OnBlockItemDeleteListener {
        fun onDelete(index: Int, id: Int)
    }

    companion object {
        private const val ARG_INDEX = "index"
        private const val ARG_ID = "id"
        private const val ARG_ITEM = "item"

        operator fun invoke(index: Int, id: Int, item: String): AdBlockItemDeleteDialog {
            return AdBlockItemDeleteDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_INDEX, index)
                    putInt(ARG_ID, id)
                    putString(ARG_ITEM, item)
                }
            }
        }
    }
}
