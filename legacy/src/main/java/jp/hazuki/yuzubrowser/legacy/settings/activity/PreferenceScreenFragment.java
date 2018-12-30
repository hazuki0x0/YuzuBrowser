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

import android.os.Bundle;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import androidx.annotation.Nullable;

public class PreferenceScreenFragment extends YuzuPreferenceFragment {
    private static final String ARG_ID = "id";

    @Override
    public void onCreateYuzuPreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(getArguments().getInt(ARG_ID), rootKey);
    }

    public static PreferenceScreenFragment newInstance(int resId, String key) {
        PreferenceScreenFragment fragment = new PreferenceScreenFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_ID, resId);
        bundle.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, key);
        fragment.setArguments(bundle);
        return fragment;
    }
}
