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

package jp.hazuki.yuzubrowser.legacy.settings.activity

import androidx.preference.Preference
import jp.hazuki.yuzubrowser.legacy.settings.preference.NightModePreference
import jp.hazuki.yuzubrowser.legacy.settings.preference.SlowRenderingPreference
import jp.hazuki.yuzubrowser.legacy.settings.preference.WebTextSizePreference
import jp.hazuki.yuzubrowser.search.presentation.settings.SearchUrlPreference
import jp.hazuki.yuzubrowser.ui.preference.*
import jp.hazuki.yuzubrowser.ui.settings.fragment.YuzuBasePreferenceFragment

abstract class YuzuPreferenceFragment : YuzuBasePreferenceFragment() {

    override fun onDisplayPreferenceDialog(preference: Preference) {
        val fragmentManager = fragmentManager ?: return

        if (fragmentManager.findFragmentByTag(FRAGMENT_DIALOG_TAG) == null) {
            val dialog: androidx.fragment.app.DialogFragment

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

    companion object {
        private const val FRAGMENT_DIALOG_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG"
    }
}
