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

package jp.hazuki.yuzubrowser.legacy.settings.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.help.HelpActivity;

public class MainSettingsFragment extends YuzuPreferenceFragment {

    @Override
    public void onCreateYuzuPreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_main);

        findPreference("fragment_browser").setOnPreferenceClickListener(preference -> {
            openFragment(new BrowserSettingsFragment());
            return true;
        });
        findPreference("fragment_page").setOnPreferenceClickListener(preference -> {
            openFragment(new PageSettingFragment());
            return true;
        });
        findPreference("fragment_privacy").setOnPreferenceClickListener(preference -> {
            openFragment(new PrivacyFragment());
            return true;
        });
        findPreference("fragment_ui").setOnPreferenceClickListener(preference -> {
            openFragment(new UiSettingFragment());
            return true;
        });
        findPreference("fragment_action").setOnPreferenceClickListener(preference -> {
            openFragment(new ActionSettingsFragment());
            return true;
        });
        findPreference("fragment_operation").setOnPreferenceClickListener(preference -> {
            openFragment(new OperationSettingsFragment());
            return true;
        });
        findPreference("fragment_speseddial").setOnPreferenceClickListener(preference -> {
            openFragment(new SpeeddialFragment());
            return true;
        });
        findPreference("fragment_application").setOnPreferenceClickListener(preference -> {
            openFragment(new ApplicationSettingsFragment());
            return true;
        });
        findPreference("fragment_import_export").setOnPreferenceClickListener(preference -> {
            openFragment(new ImportExportFragment());
            return true;
        });
        findPreference("activity_help").setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), HelpActivity.class));
            return true;
        });
        findPreference("fragment_about").setOnPreferenceClickListener(preference -> {
            openFragment(new AboutFragment());
            return true;
        });
    }

    private void openFragment(PreferenceFragmentCompat fragment) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

}
