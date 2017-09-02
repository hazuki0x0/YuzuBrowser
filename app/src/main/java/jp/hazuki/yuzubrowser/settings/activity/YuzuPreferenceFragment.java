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

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;

import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.settings.preference.NightModePreference;
import jp.hazuki.yuzubrowser.settings.preference.SearchUrlPreference;
import jp.hazuki.yuzubrowser.settings.preference.SlowRenderingPreference;
import jp.hazuki.yuzubrowser.settings.preference.WebTextSizePreference;
import jp.hazuki.yuzubrowser.settings.preference.common.AlertDialogPreference;
import jp.hazuki.yuzubrowser.settings.preference.common.CustomDialogPreference;
import jp.hazuki.yuzubrowser.settings.preference.common.FloatSeekbarPreference;
import jp.hazuki.yuzubrowser.settings.preference.common.IntListPreference;
import jp.hazuki.yuzubrowser.settings.preference.common.MultiListIntPreference;
import jp.hazuki.yuzubrowser.settings.preference.common.SeekbarPreference;
import jp.hazuki.yuzubrowser.settings.preference.common.StrToIntListPreference;

public abstract class YuzuPreferenceFragment extends PreferenceFragmentCompatDividers {
    private static final String FRAGMENT_DIALOG_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";

    private int preferenceResId;

    public abstract void onCreateYuzuPreferences(@Nullable Bundle savedInstanceState, String rootKey);

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(AppData.PREFERENCE_NAME);
        onCreateYuzuPreferences(savedInstanceState, rootKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            return super.onCreateView(inflater, container, savedInstanceState);
        } finally {
            setDividerPreferences(DIVIDER_PADDING_CHILD | DIVIDER_CATEGORY_AFTER_LAST | DIVIDER_CATEGORY_BETWEEN);
            getPreferenceManager().setSharedPreferencesName(AppData.PREFERENCE_NAME);
            TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[]{android.R.attr.listDivider});
            Drawable divider = a.getDrawable(0);
            a.recycle();
            setDivider(divider);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void addPreferencesFromResource(int preferencesResId) {
        super.addPreferencesFromResource(preferencesResId);
        this.preferenceResId = preferencesResId;
    }

    @Override
    public void setPreferencesFromResource(int preferencesResId, @Nullable String key) {
        super.setPreferencesFromResource(preferencesResId, key);
        this.preferenceResId = preferencesResId;
    }

    public int getPreferenceResId() {
        return preferenceResId;
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (this.getFragmentManager().findFragmentByTag(FRAGMENT_DIALOG_TAG) == null) {
            DialogFragment dialog;

            if (preference instanceof StrToIntListPreference) {
                dialog = StrToIntListPreference.PreferenceDialog.newInstance(preference);
            } else if (preference instanceof AlertDialogPreference) {
                dialog = AlertDialogPreference.PreferenceDialog.newInstance(preference);
            } else if (preference instanceof FloatSeekbarPreference) {
                dialog = FloatSeekbarPreference.PreferenceDialog.newInstance(preference);
            } else if (preference instanceof IntListPreference) {
                dialog = IntListPreference.PreferenceDialog.newInstance(preference);
            } else if (preference instanceof MultiListIntPreference) {
                dialog = MultiListIntPreference.PrefernceDialog.newInstance(preference);
            } else if (preference instanceof SeekbarPreference) {
                dialog = SeekbarPreference.PreferenceDialog.newInstance(preference);
            } else if (preference instanceof SearchUrlPreference) {
                dialog = SearchUrlPreference.PreferenceDialog.newInstance(preference);
            } else if (preference instanceof NightModePreference) {
                dialog = NightModePreference.SettingDialog.newInstance(preference);
            } else if (preference instanceof WebTextSizePreference) {
                dialog = WebTextSizePreference.SizeDialog.newInstance(preference);
            } else if (preference instanceof SlowRenderingPreference) {
                dialog = SlowRenderingPreference.WarningDialog.newInstance(preference);
            } else if (preference instanceof CustomDialogPreference) {
                ((CustomDialogPreference) preference).show(getChildFragmentManager());
                return;
            } else {
                super.onDisplayPreferenceDialog(preference);
                return;
            }

            dialog.setTargetFragment(this, 0);
            dialog.show(getFragmentManager(), preference.getKey());
        }
    }
}
