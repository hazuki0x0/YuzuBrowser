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

package jp.hazuki.yuzubrowser.legacy.settings.preference

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.webkit.WebViewProxy
import jp.hazuki.yuzubrowser.ui.preference.CustomDialogPreference
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs

class ProxySettingDialog @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : CustomDialogPreference(context, attrs) {
    private var mSaveSettings = true

    fun setSaveSettings(save: Boolean): ProxySettingDialog {
        mSaveSettings = save
        return this
    }

    override fun crateCustomDialog(): CustomDialogPreference.CustomDialogFragment {
        return SettingDialog.newInstance(mSaveSettings)
    }

    class SettingDialog : CustomDialogPreference.CustomDialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = LayoutInflater.from(context).inflate(R.layout.proxy_dialog, null)
            val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
            val editText = view.findViewById<EditText>(R.id.editText)
            val httpsCheckBox = view.findViewById<CheckBox>(R.id.httpsCheckBox)
            val httpsText = view.findViewById<EditText>(R.id.httpsEditText)

            httpsCheckBox.setOnCheckedChangeListener { _, isChecked -> httpsText.isEnabled = isChecked }

            checkBox.isChecked = AppPrefs.proxy_set.get()
            editText.setText(AppPrefs.proxy_address.get())
            httpsCheckBox.isChecked = AppPrefs.proxy_https_set.get()
            httpsText.setText(AppPrefs.proxy_https_address.get())
            httpsText.isEnabled = httpsCheckBox.isChecked

            val builder = AlertDialog.Builder(activity)

            builder
                    .setView(view)
                    .setTitle(R.string.pref_proxy_settings)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val context = context
                        val enable = checkBox.isChecked
                        val proxyAddress = editText.text.toString()
                        val enableHttps = httpsCheckBox.isChecked
                        val httpsProxyAddress = httpsText.text.toString()

                        WebViewProxy.setProxy(context, enable, proxyAddress, enableHttps, httpsProxyAddress)

                        if (arguments!!.getBoolean(SAVE)) {
                            AppPrefs.proxy_set.set(enable)
                            AppPrefs.proxy_address.set(proxyAddress)
                            AppPrefs.proxy_https_set.set(enableHttps)
                            AppPrefs.proxy_https_address.set(httpsProxyAddress)
                            AppPrefs.commit(context, AppPrefs.proxy_set, AppPrefs.proxy_address,
                                AppPrefs.proxy_https_set, AppPrefs.proxy_https_address)
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)

            return builder.create()
        }

        companion object {
            private const val SAVE = "save"

            fun newInstance(save: Boolean): SettingDialog {
                val dialog = SettingDialog()
                val bundle = Bundle()
                bundle.putBoolean(SAVE, save)
                dialog.arguments = bundle
                return dialog
            }
        }
    }
}
