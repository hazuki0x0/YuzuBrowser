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
import android.preference.ListPreference;
import android.util.AttributeSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.BrowserApplication;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.ThemeData;

public class ThemePreference extends ListPreference {
    public ThemePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        File dir = new File(BrowserApplication.getExternalUserDirectory(), "theme");

        if (!dir.isDirectory()) {
            dir.delete();
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File noMedia = new File(dir, ".nomedia");

        if (!noMedia.exists()) {
            try {
                noMedia.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File[] themes = dir.listFiles();

        List<String> themeList = new ArrayList<>();
        List<String> valueList = new ArrayList<>();

        //Add default
        themeList.add(getContext().getString(R.string.pref_dark_theme));
        valueList.add("");
        themeList.add(getContext().getString(R.string.pref_light_theme));
        valueList.add(ThemeData.THEME_LIGHT);

        if (themes != null) {
            for (File theme : themes) {
                if (theme.isDirectory()) {
                    themeList.add(theme.getName());
                    valueList.add(theme.getName());
                }
            }
        }


        setEntries(themeList.toArray(new String[themeList.size()]));
        setEntryValues(valueList.toArray(new String[valueList.size()]));
    }
}
