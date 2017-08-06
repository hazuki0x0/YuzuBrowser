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

package jp.hazuki.yuzubrowser.settings.activity;

import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.PermissionUtils;

public class PrivacyFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(AppData.PREFERENCE_NAME);
        addPreferencesFromResource(R.xml.pref_privacy);

        findPreference("web_geolocation").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (PermissionUtils.checkLocation(getActivity())) {
                    return true;
                } else {
                    PermissionUtils.requestLocation(getActivity());
                    return false;
                }
            }
        });

        SwitchPreference privateMode = (SwitchPreference) findPreference("private_mode");

        final Preference formData = findPreference("save_formdata");
        final Preference webDB = findPreference("web_db");
        final Preference webDom = findPreference("web_dom_db");
        final Preference geo = findPreference("web_geolocation");
        final Preference appCache = findPreference("web_app_cache");

        boolean enableSettings = !privateMode.isChecked();

        formData.setEnabled(enableSettings);
        webDB.setEnabled(enableSettings);
        webDom.setEnabled(enableSettings);
        geo.setEnabled(enableSettings);
        appCache.setEnabled(enableSettings);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getPreferenceScreen().removePreference(formData);
        } else {
            Preference safeBrowsing = findPreference("safe_browsing");
            safeBrowsing.setEnabled(false);
            safeBrowsing.setSummary(R.string.pref_required_android_O);
        }

        privateMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enableSettings = !(boolean) newValue;

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    formData.setEnabled(enableSettings);

                webDB.setEnabled(enableSettings);
                webDom.setEnabled(enableSettings);
                geo.setEnabled(enableSettings);
                appCache.setEnabled(enableSettings);
                return true;
            }
        });
    }
}
