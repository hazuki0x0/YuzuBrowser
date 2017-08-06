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

package jp.hazuki.yuzubrowser.action.item;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;

public class CustomSingleActionActivity extends ThemeActivity {

    public static final String EXTRA_ACTION = "CustomSingleActionActivity.extra.action";
    public static final String EXTRA_NAME = "CustomSingleActionActivity.extra.name";
    public static final String EXTRA_ICON = "CustomSingleActionActivity.extra.icon";
    public static final String EXTRA_ICON_MODE = "CustomSingleActionActivity.extra.icon.mode";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);
        setTitle(R.string.action_custom_setting);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            Action action = intent.getParcelableExtra(EXTRA_ACTION);
            String name = intent.getStringExtra(EXTRA_NAME);
            ActionNameArray actionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, CustomSingleActionFragment.newInstance(action, name, actionNameArray))
                    .commit();
        }
    }

}
