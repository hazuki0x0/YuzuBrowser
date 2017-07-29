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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.Logger;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;
import jp.hazuki.yuzubrowser.utils.view.SpinnerButton;

public class CustomPreferenceActivity extends ThemeActivity {
    private static final String TAG = "CustomPreferenceActivity";

    public static final String EXTRA_ACTION = "CustomPreferenceActivity.extra.action";
    public static final String EXTRA_NAME = "CustomPreferenceActivity.extra.name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Intent intent = getIntent();
        Action action = intent.getParcelableExtra(EXTRA_ACTION);
        String name = intent.getStringExtra(EXTRA_NAME);
        ActionNameArray actionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA);

        CustomDialog.newInstance(name, action, actionNameArray)
                .show(getSupportFragmentManager(), "custom");
    }

    @Override
    protected int lightThemeResource() {
        return R.style.BrowserMinThemeLight_Transparent;
    }

    public static class CustomDialog extends DialogFragment {
        private static final int RESULT_REQUEST_PREFERENCE = 1;
        private static final String NAME = "name";
        private static final String ACTION = "act";
        private static final String ARRAY = "array";

        private Action mAction;
        private ActionNameArray mActionNameArray;

        private SpinnerButton actionButton;
        private EditText nameEditText;

        public static DialogFragment newInstance(String name, Action action, ActionNameArray nameArray) {
            DialogFragment fragment = new CustomDialog();
            Bundle bundle = new Bundle();
            bundle.putString(NAME, name);
            bundle.putParcelable(ACTION, action);
            bundle.putParcelable(ARRAY, nameArray);
            fragment.setArguments(bundle);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.action_custom_setting, null);

            actionButton = (SpinnerButton) v.findViewById(R.id.actionButton);
            nameEditText = (EditText) v.findViewById(R.id.nameEditText);

            mAction = getArguments().getParcelable(ACTION);
            mActionNameArray = getArguments().getParcelable(ARRAY);

            if (mAction != null) {
                ActionNameArray actionNameArray = (mActionNameArray == null) ? new ActionNameArray(getActivity()) : mActionNameArray;
                actionButton.setText(mAction.toString(actionNameArray));
            }

            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new ActionActivity.Builder(getActivity())
                            .setTitle(R.string.action_custom_base)
                            .setDefaultAction(mAction)
                            .setActionNameArray(mActionNameArray)
                            .create();

                    startActivityForResult(intent, RESULT_REQUEST_PREFERENCE);
                }
            });

            nameEditText.setText(getArguments().getString(NAME));

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.action_custom_setting)
                    .setView(v)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent data = new Intent();
                            data.putExtra(EXTRA_ACTION, (Parcelable) mAction);
                            data.putExtra(EXTRA_NAME, nameEditText.getText().toString());
                            getActivity().setResult(RESULT_OK, data);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);
            return builder.create();

        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            Activity activity = getActivity();
            if (activity != null)
                activity.finish();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case RESULT_REQUEST_PREFERENCE: {
                    Action action = ActionActivity.getActionFromIntent(resultCode, data);
                    if (action == null) {
                        Logger.w(TAG, "Action is null");
                        return;
                    }
                    mAction = action;

                    ActionNameArray actionNameArray = (mActionNameArray == null) ? new ActionNameArray(getActivity()) : mActionNameArray;
                    actionButton.setText(mAction.toString(actionNameArray));
                }
                break;
            }
        }
    }


}
