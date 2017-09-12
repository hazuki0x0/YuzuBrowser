/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.settings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.adblock.AdBlockActivity;
import jp.hazuki.yuzubrowser.settings.preference.common.StrToIntListPreference;

public class BrowserSettingsFragment extends YuzuPreferenceFragment {

    @Override
    public void onCreateYuzuPreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_browser_settings);

        StrToIntListPreference pref = (StrToIntListPreference) findPreference("search_suggest");
        final Preference suggest = findPreference("search_suggest_engine");

        suggest.setEnabled(pref.getValue() != 2);

        pref.setOnPreferenceChangeListener((preference, newValue) -> {
            suggest.setEnabled(((int) newValue) != 2);
            return true;
        });

        final Preference savePinned = findPreference("save_pinned_tabs");
        SwitchPreference saveLast = (SwitchPreference) findPreference("save_last_tabs");

        savePinned.setEnabled(!saveLast.isChecked());
        saveLast.setOnPreferenceChangeListener((preference, newValue) -> {
            savePinned.setEnabled(!(boolean) newValue);
            return true;
        });

        findPreference("ad_block_settings").setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), AdBlockActivity.class));
            return true;
        });
    }
}
