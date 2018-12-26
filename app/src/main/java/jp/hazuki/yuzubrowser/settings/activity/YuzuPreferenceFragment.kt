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

package jp.hazuki.yuzubrowser.settings.activity

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceScreen
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.settings.preference.NightModePreference
import jp.hazuki.yuzubrowser.settings.preference.SearchUrlPreference
import jp.hazuki.yuzubrowser.settings.preference.SlowRenderingPreference
import jp.hazuki.yuzubrowser.settings.preference.WebTextSizePreference
import jp.hazuki.yuzubrowser.settings.preference.common.*

abstract class YuzuPreferenceFragment : PreferenceFragmentCompat() {

    var preferenceResId: Int = 0
        private set

    abstract fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?)

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = AppData.PREFERENCE_NAME
        onCreateYuzuPreferences(savedInstanceState, rootKey)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return try {
            super.onCreateView(inflater, container, savedInstanceState)
        } finally {
            activity?.let {
                preferenceManager.sharedPreferencesName = AppData.PREFERENCE_NAME
                val a = it.theme.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
                val divider = a.getDrawable(0)
                a.recycle()
                setDivider(divider)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val activity = activity ?: return

        preferenceScreen?.run {
            val key: String? = arguments?.getString(ARG_PREFERENCE_ROOT)
            val title = if (!TextUtils.isEmpty(key)) findPreference(key).title else title
            activity.title = if (TextUtils.isEmpty(title)) getText(R.string.pref_settings) else title
        }
    }

    override fun addPreferencesFromResource(preferencesResId: Int) {
        super.addPreferencesFromResource(preferencesResId)
        this.preferenceResId = preferencesResId
    }

    override fun setPreferencesFromResource(preferencesResId: Int, key: String?) {
        super.setPreferencesFromResource(preferencesResId, key)
        this.preferenceResId = preferencesResId
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        val fragmentManager = fragmentManager ?: return

        if (fragmentManager.findFragmentByTag(FRAGMENT_DIALOG_TAG) == null) {
            val dialog: DialogFragment

            when (preference) {
                is StrToIntListPreference -> dialog = StrToIntListPreference.PreferenceDialog.newInstance(preference)
                is AlertDialogPreference -> dialog = AlertDialogPreference.PreferenceDialog.newInstance(preference)
                is FloatSeekbarPreference -> dialog = FloatSeekbarPreference.PreferenceDialog.newInstance(preference)
                is IntListPreference -> dialog = IntListPreference.PreferenceDialog.newInstance(preference)
                is MultiListIntPreference -> dialog = MultiListIntPreference.PrefernceDialog.newInstance(preference)
                is SeekbarPreference -> dialog = SeekbarPreference.PreferenceDialog.newInstance(preference)
                is SearchUrlPreference -> dialog = SearchUrlPreference.PreferenceDialog.newInstance(preference)
                is NightModePreference -> dialog = NightModePreference.SettingDialog.newInstance(preference)
                is WebTextSizePreference -> dialog = WebTextSizePreference.SizeDialog.newInstance(preference)
                is SlowRenderingPreference -> dialog = SlowRenderingPreference.WarningDialog.newInstance(preference)
                is CustomDialogPreference -> {
                    preference.show(childFragmentManager)
                    return
                }
                else -> {
                    super.onDisplayPreferenceDialog(preference)
                    return
                }
            }

            dialog.setTargetFragment(this, 0)
            dialog.show(fragmentManager, preference.key)
        }
    }

    open fun onPreferenceStartScreen(pref: PreferenceScreen): Boolean = false

    companion object {
        private const val FRAGMENT_DIALOG_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG"
    }
}
