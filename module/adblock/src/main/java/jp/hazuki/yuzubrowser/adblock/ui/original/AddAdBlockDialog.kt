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
import jp.hazuki.yuzubrowser.adblock.R
import jp.hazuki.yuzubrowser.adblock.repository.original.AdBlock
import jp.hazuki.yuzubrowser.adblock.repository.original.AdBlockManager
import jp.hazuki.yuzubrowser.core.utility.extensions.density

class AddAdBlockDialog : DialogFragment() {

    private var listener: OnAdBlockListUpdateListener? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException()
        val arguments = arguments ?: throw IllegalArgumentException()

        val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        val density = activity.density
        val marginWidth = (8 * density + 0.5f).toInt()
        val marginHeight = (16 * density + 0.5f).toInt()
        params.setMargins(marginWidth, marginHeight, marginWidth, marginHeight)
        val editText = EditText(activity).apply {
            layoutParams = params
            id = android.R.id.edit
            inputType = InputType.TYPE_CLASS_TEXT

            setText(arguments.getString(ARG_URL))
        }

        val builder = AlertDialog.Builder(activity).apply {
            setTitle(arguments.getInt(ARG_TITLE))
            setView(editText)
            setPositiveButton(android.R.string.ok) { _, _ ->
                val provider = AdBlockManager.getProvider(activity, arguments.getInt(ARG_TYPE))
                provider.update(AdBlock(editText.text.toString()))
                listener!!.onAdBlockListUpdate()
            }
            setNegativeButton(android.R.string.cancel, null)
        }
        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = activity as OnAdBlockListUpdateListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnAdBlockListUpdateListener {
        fun onAdBlockListUpdate()
    }

    companion object {
        private const val ARG_TYPE = "type"
        private const val ARG_TITLE = "title"
        private const val ARG_URL = "url"


        fun addBackListInstance(url: String?): AddAdBlockDialog {
            val bundle = Bundle().apply {
                putInt(ARG_TYPE, 1)
                putInt(ARG_TITLE, R.string.pref_ad_block_black)
                putString(ARG_URL, trimUrl(url))
            }
            return newInstance(bundle)
        }

        fun addWhiteListInstance(url: String?): AddAdBlockDialog {
            val bundle = Bundle().apply {
                putInt(ARG_TYPE, 2)
                putInt(ARG_TITLE, R.string.pref_ad_block_white)
                putString(ARG_URL, trimUrl(url))
            }
            return newInstance(bundle)
        }

        fun addWhitePageListInstance(url: String?): AddAdBlockDialog {
            val bundle = Bundle().apply {
                putInt(ARG_TYPE, 3)
                putInt(ARG_TITLE, R.string.pref_ad_block_white_page)
                putString(ARG_URL, trimUrl(url))
            }
            return newInstance(bundle)
        }

        private fun trimUrl(url: String?): String? {
            if (url != null) {
                val index = url.indexOf("://")
                if (index > -1) {
                    return url.substring(index + 3)
                }
            }
            return url
        }

        private fun newInstance(bundle: Bundle): AddAdBlockDialog {
            return AddAdBlockDialog().apply {
                arguments = bundle
            }
        }
    }
}
