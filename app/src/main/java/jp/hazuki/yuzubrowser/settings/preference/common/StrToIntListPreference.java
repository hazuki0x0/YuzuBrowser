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

package jp.hazuki.yuzubrowser.settings.preference.common;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.ArrayUtils;

public class StrToIntListPreference extends DialogPreference {
    private final int mEntriesId;
    private final int[] mEntryValues;
    private int mClickedItemIndex = -1;
    private int mValue;

    public StrToIntListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StrToIntListPreference);
        Resources resources = context.getResources();
        mEntriesId = a.getResourceId(R.styleable.StrToIntListPreference_android_entries, 0);
        mEntryValues = resources.getIntArray(a.getResourceId(R.styleable.StrToIntListPreference_android_entryValues, 0));
        a.recycle();
    }

    public void setValue(int value) {
        mValue = value;
        persistInt(value);
    }

    public int getValue() {
        return mValue;
    }

    private int findIndexOfValue(int value) {
        return ArrayUtils.findIndexOfValue(value, mEntryValues);
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, -1);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(mValue) : (Integer) defaultValue);
    }

    public static class PreferenceDialog extends YuzuPreferenceDialog {

        public static YuzuPreferenceDialog newInstance(Preference preference) {
            return newInstance(new PreferenceDialog(), preference);
        }

        @Override
        protected void onPrepareDialogBuilder(android.support.v7.app.AlertDialog.Builder builder) {
            StrToIntListPreference preference = (StrToIntListPreference) getPreference();
            preference.mClickedItemIndex = preference.getValueIndex();
            builder.setPositiveButton(null, null);
            builder.setSingleChoiceItems(preference.mEntriesId, preference.mClickedItemIndex, (dialog, which) -> {
                preference.mClickedItemIndex = which;
                onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss();
            });
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            StrToIntListPreference preference = (StrToIntListPreference) getPreference();
            if (positiveResult && preference.mClickedItemIndex >= 0) {
                int value = preference.mEntryValues[preference.mClickedItemIndex];
                if (preference.callChangeListener(value)) {
                    preference.setValue(value);
                }
            }
        }
    }
}
