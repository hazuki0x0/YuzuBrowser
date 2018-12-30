/*
 * Copyright (C) 2017 Hazuki
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
import androidx.preference.DialogPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import jp.hazuki.yuzubrowser.legacy.R

class SlowRenderingPreference(context: Context, attrs: AttributeSet) : SwitchPreference(context, attrs) {

    override fun onClick() {
        if (isChecked) {
            super.onClick()
        } else {
            preferenceManager?.showDialog(this)
        }
    }

    class WarningDialog : androidx.fragment.app.DialogFragment() {

        companion object {
            private const val ARG_KEY = "key"

            @JvmStatic
            fun newInstance(preference: Preference): WarningDialog {
                return WarningDialog().apply {
                    arguments = Bundle().apply {
                        putString(ARG_KEY, preference.key)
                    }
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val arguments = arguments ?: throw IllegalArgumentException()
            val fragment = targetFragment as? DialogPreference.TargetFragment
                    ?: throw IllegalStateException("Target fragment must implement TargetFragment" + " interface")

            val key = arguments.getString(ARG_KEY)

            val preference = fragment.findPreference(key) as SlowRenderingPreference

            return AlertDialog.Builder(context).run {
                setTitle(preference.title)
                setMessage(R.string.pref_slow_rendering_alert)
                setPositiveButton(android.R.string.ok,
                        { _, _ -> if (preference.callChangeListener(true)) preference.isChecked = true })
                setNegativeButton(android.R.string.cancel, null)
                create()
            }
        }
    }
}
