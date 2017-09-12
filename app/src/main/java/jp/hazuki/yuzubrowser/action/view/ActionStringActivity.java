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

package jp.hazuki.yuzubrowser.action.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Toast;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionList;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;
import jp.hazuki.yuzubrowser.utils.util.JsonConvertable;

public class ActionStringActivity extends ThemeActivity {
    public static final String EXTRA_ACTIVITY = "MakeActionStringActivity.extra.activity";
    public static final String EXTRA_ACTION = "MakeActionStringActivity.extra.action";
    public static final int ACTION_ACTIVITY = 1;
    public static final int ACTION_LIST_ACTIVITY = 2;
    private int mTarget;
    private EditText editText;
    private ActionNameArray mActionNameArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scroll_edittext);
        editText = findViewById(R.id.editText);

        Intent intent = getIntent();
        if (intent == null)
            throw new NullPointerException("Intent is null");

        mActionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA);

        Parcelable action = intent.getParcelableExtra(EXTRA_ACTION);
        if (action != null) {
            if (action instanceof Action) {
                mTarget = ACTION_ACTIVITY;
                editText.setText(((JsonConvertable) action).toJsonString());
                return;
            } else if (action instanceof ActionList) {
                mTarget = ACTION_LIST_ACTIVITY;
                editText.setText(((JsonConvertable) action).toJsonString());
                return;
            }
            throw new IllegalArgumentException("ARG_ACTION is not action or actionlist");
        } else {
            mTarget = getIntent().getIntExtra(EXTRA_ACTIVITY, ACTION_ACTIVITY);

            switch (mTarget) {
                case ACTION_ACTIVITY:
                    new ActionActivity.Builder(this)
                            .show(ACTION_ACTIVITY);
                    break;
                case ACTION_LIST_ACTIVITY:
                    new ActionListActivity.Builder(this)
                            .show(ACTION_LIST_ACTIVITY);
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.json_to_action).setOnMenuItemClickListener(item -> {
            String jsonstr = editText.getText().toString();

            if (getCallingPackage() == null) {
                switch (mTarget) {
                    case ACTION_ACTIVITY:
                        new ActionActivity.Builder(ActionStringActivity.this)
                                .setDefaultAction(new Action(jsonstr))
                                .setActionNameArray(mActionNameArray)
                                .show(ACTION_ACTIVITY);
                        break;
                    case ACTION_LIST_ACTIVITY:
                        new ActionListActivity.Builder(ActionStringActivity.this)
                                .setDefaultActionList(new ActionList(jsonstr))
                                .setActionNameArray(mActionNameArray)
                                .show(ACTION_LIST_ACTIVITY);
                        break;
                }
            } else {
                boolean result = false;
                Parcelable actionObj = null;
                switch (mTarget) {
                    case ACTION_ACTIVITY:
                        Action action = new Action();
                        result = action.fromJsonString(jsonstr);
                        actionObj = action;
                        break;
                    case ACTION_LIST_ACTIVITY:
                        ActionList actionList = new ActionList();
                        result = actionList.fromJsonString(jsonstr);
                        actionObj = actionList;
                        break;
                }

                if (result) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_ACTION, actionObj);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(this, R.string.invalid_json_format, Toast.LENGTH_SHORT).show();
                }

            }

            return false;
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_ACTIVITY: {
                if (resultCode != Activity.RESULT_OK || data == null)
                    return;
                Action action = data.getParcelableExtra(ActionActivity.EXTRA_ACTION);
                if (action == null)
                    return;
                editText.setText(action.toJsonString());
            }
            case ACTION_LIST_ACTIVITY: {
                if (resultCode != Activity.RESULT_OK || data == null)
                    return;
                ActionList action = data.getParcelableExtra(ActionListActivity.EXTRA_ACTION_LIST);
                if (action == null)
                    return;
                editText.setText(action.toJsonString());
            }
            break;
        }
    }
}
