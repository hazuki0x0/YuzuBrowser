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

package jp.hazuki.yuzubrowser.legacy.settings.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.ui.preference.SeekBarPreferenceController;
import jp.hazuki.yuzubrowser.ui.preference.YuzuPreferenceDialog;

public class WebTextSizePreference extends DialogPreference {
    private int mValue;

    public WebTextSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        mValue = value;
        persistInt(value);
    }

    public static class SizeDialog extends YuzuPreferenceDialog {

        private SeekBarPreferenceController mSeekbarController;

        public static YuzuPreferenceDialog newInstance(Preference preference) {
            return newInstance(new SizeDialog(), preference);
        }

        @Override
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            WebTextSizePreference pref = getParentPreference();
            mSeekbarController = new SeekBarPreferenceController(getContext()) {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    super.onClick(dialog, which);

                }
            };
            mSeekbarController.setSeekMin(1);
            mSeekbarController.setSeekMax(300);
            mSeekbarController.setValue(pref.mValue);
            mSeekbarController.onPrepareDialogBuilder(builder);

            builder.setTitle(R.string.pref_text_size);
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            if (positiveResult) {
                WebTextSizePreference pref = getParentPreference();
                int value = getLastValue();
                if (value >= 0) {
                    if (pref.callChangeListener(value)) {
                        if (mSeekbarController != null)
                            mSeekbarController.setValue(value);
                        pref.setValue(value);
                    }
                }
            }
        }

        private int getLastValue() {
            return mSeekbarController.getCurrentValue();
        }
    }
}
