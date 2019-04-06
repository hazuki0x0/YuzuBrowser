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
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import android.widget.*
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToPx
import jp.hazuki.yuzubrowser.ui.R
import jp.hazuki.yuzubrowser.ui.extensions.setTextAppearanceCompat

class JsAlertDialog(private val context: Context) {
    private val builder = AlertDialog.Builder(context)

    fun setAlertMode(url: String, message: CharSequence, isShowCheckBox: Boolean, callback: (result: Boolean, blockAlert: Boolean) -> Unit): JsAlertDialog {
        val layout = setMessage(message)
        val checkBox = if (isShowCheckBox) addCheckBox(layout, context.getText(R.string.prevent_additional_dialogues)) else null
        builder.run {
            setTitle(url.getTitle())
            setPositiveButton(android.R.string.ok) { _, _ ->
                callback(true, checkBox?.isChecked ?: false)
            }
            setOnCancelListener { callback(false, checkBox?.isChecked ?: false) }

        }
        return this
    }

    fun setConfirmMode(url: String, message: CharSequence, isShowCheckBox: Boolean, callback: (result: Boolean, blockAlert: Boolean) -> Unit): JsAlertDialog {
        val layout = setMessage(message)
        val checkBox = if (isShowCheckBox) addCheckBox(layout, context.getText(R.string.prevent_additional_dialogues)) else null
        builder.run {
            setTitle(url.getTitle())
            setPositiveButton(android.R.string.yes) { _, _ ->
                callback(true, checkBox?.isChecked ?: false)
            }
            setNegativeButton(android.R.string.no) { _, _ ->
                callback(false, checkBox?.isChecked ?: false)
            }
            setOnCancelListener { callback(false, checkBox?.isChecked ?: false) }
        }
        return this
    }

    fun setPromptMode(url: String, message: String, defaultValue: String, isShowCheckBox: Boolean, callback: (result: String?, blockAlert: Boolean) -> Unit): JsAlertDialog {
        val layout = setMessage(message)
        val editText = EditText(context)
        editText.setText(defaultValue)
        layout.addView(editText)
        val checkBox = if (isShowCheckBox) addCheckBox(layout, context.getText(R.string.prevent_additional_dialogues)) else null
        builder.run {
            setTitle(url.getTitle())
            setPositiveButton(android.R.string.yes) { _, _ ->
                callback(editText.text.toString(), checkBox?.isChecked ?: false)
            }
            setNegativeButton(android.R.string.no) { _, _ ->
                callback(null, checkBox?.isChecked ?: false)
            }
            setOnCancelListener { callback(null, checkBox?.isChecked ?: false) }
        }
        return this
    }

    fun show() {
        builder.create().show()
    }


    private fun String.getTitle(): String {
        val host = Uri.parse(this).host
        return if (host != null) {
            context.getString(R.string.host_page_says, host)
        } else {
            context.getString(R.string.this_page_says)
        }
    }


    private fun getViewPadding(): Int {
        val attr = context.obtainStyledAttributes(intArrayOf(R.attr.listPreferredItemPaddingLeft))
        val padding = attr.getDimensionPixelSize(0, 1)
        attr.recycle()
        return padding
    }

    private val dialogPadding: Int
        get() = context.convertDpToPx(24)

    private val dialogTopPadding: Int
        get() = context.convertDpToPx(8)

    private fun getCheckBoxTextColor(): Int {
        val attr = context.obtainStyledAttributes(intArrayOf(android.R.attr.textColorSecondary))
        val padding = attr.getColor(0, Color.BLACK)
        attr.recycle()
        return padding
    }

    private fun addCheckBox(parent: ViewGroup?, message: CharSequence?): CheckBox {
        val parentView = parent ?: FrameLayout(context).apply {
            val padding = getViewPadding()
            setPadding(padding, 0, padding, 0)
            builder.setView(this)
        }
        val padding = FrameLayout(context).apply { setPadding(0, dialogPadding, 0, 0) }
        parentView.addView(padding)
        val checkBox = CheckBox(context)
        message?.let {
            checkBox.text = it
            checkBox.setTextColor(getCheckBoxTextColor())
        }
        parentView.addView(checkBox)
        return checkBox
    }

    private fun setMessage(message: CharSequence): LinearLayout {
        val scrollView = ScrollView(context)
        val container = LinearLayout(context)
        val horizontalPadding = dialogPadding
        val verticalPadding = dialogTopPadding
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

        if (message.isNotEmpty()) {
            val textView = TextView(context)
            textView.text = message
            textView.setTextAppearanceCompat(R.style.TextAppearance_AppCompat_Subhead)

            container.addView(textView)
        }
        scrollView.addView(container)
        builder.setView(scrollView)
        return container
    }
}