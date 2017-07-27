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
import android.view.View;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.useragent.UserAgent;
import jp.hazuki.yuzubrowser.useragent.UserAgentList;

public class UserAgentListPreference extends ListPreference {

    public UserAgentListPreference(Context context) {
        super(context);
        init(context);
    }

    public UserAgentListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected View onCreateDialogView() {
        init(getContext());
        return super.onCreateDialogView();
    }

    private void init(Context context) {
        UserAgentList mUserAgentList = new UserAgentList();
        mUserAgentList.read(context);

        String[] entries = new String[mUserAgentList.size() + 1];
        String[] entryValues = new String[mUserAgentList.size() + 1];

        entries[0] = getContext().getString(R.string.default_text);
        entryValues[0] = "";

        UserAgent userAgent;

        for (int i = 1; mUserAgentList.size() > i - 1; i++) {
            userAgent = mUserAgentList.get(i - 1);
            entries[i] = userAgent.name;
            entryValues[i] = userAgent.useragent;
        }

        setEntries(entries);
        setEntryValues(entryValues);
    }
}
