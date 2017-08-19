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

package jp.hazuki.yuzubrowser.utils.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatActivity;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.theme.ThemeData;

public class ThemeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (isLoadThemeData()) {
            ThemeData.createInstance(getApplicationContext(), AppData.theme_setting.get());
        }

        if (!useDarkTheme() && useLightTheme() || ThemeData.isEnabled() && ThemeData.getInstance().lightTheme) {
            setTheme(lightThemeResource());
        }

        super.onCreate(savedInstanceState);
    }

    protected boolean isLoadThemeData() {
        return false;
    }

    protected
    @StyleRes
    int lightThemeResource() {
        return R.style.CustomThemeLight;
    }

    protected boolean useLightTheme() {
        return false;
    }

    protected boolean useDarkTheme() {
        return false;
    }
}
