/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.settings.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.preference.common.CustomDialogPreference;
import jp.hazuki.yuzubrowser.settings.preference.common.SeekBarPreferenceController;
import jp.hazuki.yuzubrowser.utils.ArrayUtils;

public class WebTextSizeDialog extends CustomDialogPreference {
    private SeekBarPreferenceController mSeekbarController;
    private int mClickedItemIndex = -1;
    private int[] mEntryValues;
    private int mValue;

    public WebTextSizeDialog(Context context, int textsize) {
        super(context);
        mValue = textsize;
    }

    public WebTextSizeDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private int findIndexOfValue(int value) {
        return ArrayUtils.findIndexOfValue(value, mEntryValues);
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    @Override
    public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        mSeekbarController = new SeekBarPreferenceController(getContext()) {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                super.onClick(dialog, which);
                WebTextSizeDialog.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            }
        };
        mSeekbarController.setSeekMin(1);
        mSeekbarController.setSeekMax(300);
        mSeekbarController.setValue(mValue);
        mSeekbarController.onPrepareDialogBuilder(builder);
        builder.setPositiveButton(getPositiveButtonText(), this);
        builder.setNegativeButton(getNegativeButtonText(), this);

        builder.setTitle(R.string.pref_text_size);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int value = getLastValue();
            if (value >= 0) {
                if (callChangeListener(value)) {
                    setValue(value);
                }
            }
        }
    }

    private int getLastValue() {
        return mSeekbarController.getCurrentValue();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, -1);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(mValue) : (Integer) defaultValue);
    }

    public void setValue(int value) {
        if (mSeekbarController != null)
            mSeekbarController.setValue(value);
        mValue = value;
        persistInt(value);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (getPreferenceManager() != null)
            super.onClick(dialog, which);
        else if (which == DialogInterface.BUTTON_POSITIVE) {
            int value = getLastValue();
            if (value >= 0)
                onClick(getLastValue());
        }
    }

    public void onClick(int value) {
    }
}
