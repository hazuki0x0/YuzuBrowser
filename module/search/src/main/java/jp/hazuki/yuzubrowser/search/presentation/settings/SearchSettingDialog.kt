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

package jp.hazuki.yuzubrowser.search.presentation.settings

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.android.colorpicker.ColorPickerDialog
import com.android.colorpicker.ColorPickerSwatch
import jp.hazuki.yuzubrowser.search.R
import jp.hazuki.yuzubrowser.search.domain.COLORS
import jp.hazuki.yuzubrowser.search.domain.getIdentityColor
import jp.hazuki.yuzubrowser.search.domain.randomColor
import jp.hazuki.yuzubrowser.search.model.provider.SearchUrl

class SearchSettingDialog : DialogFragment(), ColorPickerSwatch.OnColorSelectedListener {

    companion object {
        private const val ARG_INDEX = "index"
        private const val ARG_URL = "url"

        @JvmStatic
        fun newInstance(index: Int, url: SearchUrl?): SearchSettingDialog {
            val dialog = SearchSettingDialog()
            dialog.arguments = Bundle().apply {
                putInt(ARG_INDEX, index)
                putSerializable(ARG_URL, url)
            }
            return dialog
        }
    }

    private var color = 0
    private var nowColor = 0
    private lateinit var iconColorButton: FrameLayout

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = arguments ?: throw IllegalArgumentException()

        val searchUrl = arguments.getSerializable(ARG_URL) as SearchUrl?
        val view = View.inflate(activity, R.layout.search_url_edit, null)
        val titleEditText: EditText = view.findViewById(R.id.titleEditText)
        val urlEditText: EditText = view.findViewById(R.id.urlEditText)
        val isUseFavicon: CheckBox = view.findViewById(R.id.isUseFaviconCheckBox)
        iconColorButton = view.findViewById(R.id.iconColorButton)

        val id: Int
        if (searchUrl != null) {
            titleEditText.setText(searchUrl.title)
            urlEditText.setText(searchUrl.url)
            isUseFavicon.isChecked = searchUrl.isUseFavicon
            color = searchUrl.color
            id = searchUrl.id
        } else {
            id = -1
        }

        if (color != 0 || titleEditText.text.isEmpty()) {
            setColorToView()
        } else {
            setIconColor(titleEditText.text.toString().getIdentityColor())
        }


        titleEditText.setOnFocusChangeListener { _, b ->
            if (!b) {
                if (titleEditText.text.isNotEmpty()) {
                    setIconColor(titleEditText.text.toString().getIdentityColor())
                } else {
                    setIconColor(randomColor)
                }
            }
        }
        val errorText = getText(R.string.pref_search_url_error)
        urlEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                urlEditText.error = if (urlEditText.text.contains("%s")) null else errorText
            }

            override fun afterTextChanged(p0: Editable?) = Unit
        })
        iconColorButton.setOnClickListener {
            ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                COLORS, color, 5, COLORS.size)
                .show(childFragmentManager, "pick")
        }

        val dialog = AlertDialog.Builder(activity).apply {
            setTitle(R.string.pref_search_url)
            setView(view)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok, null)
        }.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (urlEditText.text.contains("%s")) {
                    val target = when {
                        targetFragment is OnUrlEditedListener -> targetFragment as OnUrlEditedListener
                        parentFragment is OnUrlEditedListener -> parentFragment as OnUrlEditedListener
                        activity is OnUrlEditedListener -> activity as OnUrlEditedListener
                        else -> null
                    }

                    if (target != null) {
                        val newUrl = SearchUrl(id, titleEditText.text.toString(), urlEditText.text.toString(), color, isUseFavicon.isChecked)
                        target.onUrlEdited(arguments.getInt(ARG_INDEX), newUrl)
                    }
                    dialog.dismiss()
                } else {
                    urlEditText.error = errorText
                }
            }
        }
        return dialog
    }


    override fun onColorSelected(color: Int) {
        this.color = color
        setColorToView()
    }

    private fun setColorToView() {
        if (color == 0) {
            setIconColor(randomColor)
        } else {
            setIconColor(color)
        }
    }

    private fun setIconColor(color: Int) {
        nowColor = color
        iconColorButton.background.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    interface OnUrlEditedListener {
        fun onUrlEdited(index: Int, url: SearchUrl)
    }
}
