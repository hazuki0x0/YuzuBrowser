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

package jp.hazuki.yuzubrowser.legacy.settings.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.ListPreference;
import jp.hazuki.yuzubrowser.legacy.webencode.WebTextEncodeList;

public class WebTextEncodeListPreference extends ListPreference {

    public WebTextEncodeListPreference(Context context) {
        super(context);
        init(context);
    }

    public WebTextEncodeListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onClick() {
        init(getContext());
        super.onClick();
    }

    private void init(Context context) {
        WebTextEncodeList encodes = new WebTextEncodeList();
        encodes.read(context);

        String[] entries = new String[encodes.size()];

        for (int i = 0; encodes.size() > i; i++) {
            entries[i] = encodes.get(i).encoding;
        }

        setEntries(entries);
        setEntryValues(entries);
    }
}
