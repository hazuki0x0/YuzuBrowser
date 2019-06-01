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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceScreen
import jp.hazuki.yuzubrowser.bookmark.overflow.MenuType
import jp.hazuki.yuzubrowser.bookmark.overflow.view.BookmarkOverflowMenuActivity
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.MultiFingerSettingsActivity

class ActionSettingsFragment : YuzuPreferenceFragment() {

    private var replaceFragment: ReplaceFragmentListener? = null

    override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_action_settings)

        findPreference("mf_gesture").setOnPreferenceClickListener {
            startActivity(Intent(activity, MultiFingerSettingsActivity::class.java))
            true
        }
    }

    override fun onPreferenceStartScreen(pref: PreferenceScreen): Boolean {
        if (pref.key == "ps_bookmark") {
            replaceFragment?.replaceFragment(BookmarkPreferenceScreen(), "ps_bookmark")
            return true
        }
        return super.onPreferenceStartScreen(pref)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        replaceFragment = activity as ReplaceFragmentListener
    }

    override fun onDetach() {
        super.onDetach()

        replaceFragment = null
    }

    class BookmarkPreferenceScreen : YuzuPreferenceFragment() {

        override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_action_settings, "ps_bookmark")

            findPreference("bookmark_option_site").setOnPreferenceClickListener {
                startActivity(BookmarkOverflowMenuActivity.createIntent(requireActivity(), MenuType.SITE))
                true
            }
            findPreference("bookmark_option_folder").setOnPreferenceClickListener {
                startActivity(BookmarkOverflowMenuActivity.createIntent(requireActivity(), MenuType.FOLDER))
                true
            }
        }
    }
}
