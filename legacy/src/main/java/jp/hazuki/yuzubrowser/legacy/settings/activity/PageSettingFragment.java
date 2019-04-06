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
import androidx.preference.Preference;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.webencode.WebTextEncodeSettingActivity;
import jp.hazuki.yuzubrowser.ui.preference.StrToIntListPreference;

public class PageSettingFragment extends YuzuPreferenceFragment {

    @Override
    public void onCreateYuzuPreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_page_settings);

        findPreference("web_encode_list").setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), WebTextEncodeSettingActivity.class);
            startActivity(intent);
            return true;
        });

        final Preference nightMode = findPreference("night_mode");
        StrToIntListPreference rendering = (StrToIntListPreference) findPreference("rendering");

        nightMode.setEnabled(rendering.getValue() == 4);
        rendering.setOnPreferenceChangeListener((preference, newValue) -> {
            nightMode.setEnabled((int) (newValue) == 4);
            return true;
        });
    }
}
