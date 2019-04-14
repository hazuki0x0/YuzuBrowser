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

class AbpMenuDialog : DialogFragment() {

    private var listener: OnAbpMenuListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = arguments ?: throw IllegalArgumentException()
        val builder = AlertDialog.Builder(activity)
        builder.setItems(R.array.pref_ad_block_abp_menu) { _, which ->
            val index = arguments.getInt(ARG_INDEX)
            val entity: AbpEntity = arguments.getParcelable(ARG_ENTITY)!!
            when (which) {
                0 -> listener!!.onEdit(index, entity)
                1 -> listener!!.onAskDelete(index, entity)
                2 -> listener!!.onRefresh(index, entity)
            }
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnAbpMenuListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    internal interface OnAbpMenuListener {
        fun onAskDelete(index: Int, entity: AbpEntity)

        fun onEdit(index: Int, entity: AbpEntity)

        fun onRefresh(index: Int, entity: AbpEntity)
    }

    companion object {
        private const val ARG_INDEX = "index"
        private const val ARG_ENTITY = "entity"

        operator fun invoke(index: Int, entity: AbpEntity): AbpMenuDialog {
            return AbpMenuDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_INDEX, index)
                    putParcelable(ARG_ENTITY, entity)
                }
            }
        }
    }
}
