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

package jp.hazuki.yuzubrowser.settings.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.annimon.stream.Stream;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.BrowserApplication;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.preference.common.YuzuPreferenceDialog;

public class FontListPreference extends DialogPreference {
    private String path = "";

    public FontListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setNegativeButtonText(android.R.string.cancel);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setPath(restorePersistedValue ? getPersistedString(path) : (String) defaultValue);
    }

    public void setPath(String path) {
        if (path != null && !path.equals(this.path)) {
            this.path = path;
            persistString(path);
            setSummary(path);
            notifyChanged();
        }
    }

    public String getPath() {
        return path;
    }

    public static class FontListDialog extends YuzuPreferenceDialog {

        public static FontListDialog newInstance(Preference preference) {
            return newInstance(new FontListDialog(), preference);
        }

        @Override
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            File fontDir = new File(BrowserApplication.getExternalUserDirectory(), "fonts");

            File[] fileList = null;
            if (fontDir.exists() && fontDir.isDirectory()) {
                fileList = fontDir.listFiles();
            }

            FontListPreference pref = getParentPreference();

            int checked = -1;
            String path = pref.getPath();
            if (TextUtils.isEmpty(path)) {
                checked = 0;
            }

            List<String> names = new ArrayList<>();
            List<String> values = new ArrayList<>();

            names.add(getString(R.string.default_text));
            values.add("");

            if (fileList != null) {
                Stream.of(fileList)
                        .filter(file -> {
                            String name = file.getAbsolutePath();
                            return name.endsWith(".ttf") || name.endsWith(".otf") || name.endsWith(".ttc");
                        })
                        .forEach(file -> {
                            names.add(file.getName());
                            values.add(file.getAbsolutePath());
                        });

                int index = values.indexOf(path);
                if (index > 0)
                    checked = index;
            }

            builder.setSingleChoiceItems(names.toArray(new String[names.size()]), checked, (dialogInterface, i) -> {
                pref.setPath(values.get(i));
                dialogInterface.dismiss();
            });
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
        }
    }
}
