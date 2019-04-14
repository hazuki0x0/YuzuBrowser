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
import androidx.fragment.app.DialogFragment
import jp.hazuki.yuzubrowser.adblock.R
import jp.hazuki.yuzubrowser.adblock.repository.abp.AbpEntity

class AbpItemDeleteDialog : DialogFragment() {

    private var listener: OnAbpItemDeleteListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = arguments ?: throw IllegalArgumentException()
        val builder = AlertDialog.Builder(activity)
        val entity = arguments.getParcelable<AbpEntity>(ARG_ENTITY)!!
        builder.setTitle(R.string.pref_delete)
        val title = if (entity.title.isNullOrEmpty()) entity.url else entity.title
        builder.setMessage(getString(R.string.pref_ad_block_delete_confirm, title))
        builder.setPositiveButton(android.R.string.yes) { _, _ -> listener!!.onDelete(arguments.getInt(ARG_INDEX), entity) }
        builder.setNegativeButton(android.R.string.no, null)
        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnAbpItemDeleteListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    internal interface OnAbpItemDeleteListener {
        fun onDelete(index: Int, entity: AbpEntity)
    }

    companion object {
        private const val ARG_INDEX = "index"
        private const val ARG_ENTITY = "entity"

        operator fun invoke(index: Int, entity: AbpEntity): AbpItemDeleteDialog {
            return AbpItemDeleteDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_INDEX, index)
                    putParcelable(ARG_ENTITY, entity)
                }
            }
        }
    }
}
