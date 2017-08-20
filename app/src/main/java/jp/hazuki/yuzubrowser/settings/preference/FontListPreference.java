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
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

import com.annimon.stream.Stream;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.BrowserApplication;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.FileUtils;

public class FontListPreference extends ListPreference {

    public FontListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setNegativeButtonText(android.R.string.cancel);
        init();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void init() {
        File fontDir = new File(BrowserApplication.getExternalUserDirectory(), "fonts");

        if (!fontDir.isDirectory()) {
            FileUtils.deleteFile(fontDir);
        }

        if (!fontDir.exists()) {
            fontDir.mkdirs();
        }

        File[] fileList = null;
        if (fontDir.exists() && fontDir.isDirectory()) {
            fileList = fontDir.listFiles();
        }

        List<String> names = new ArrayList<>();
        List<String> values = new ArrayList<>();

        names.add(getContext().getString(R.string.default_text));
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
        }

        setEntries(names.toArray(new String[names.size()]));
        setEntryValues(values.toArray(new String[values.size()]));
    }
}
