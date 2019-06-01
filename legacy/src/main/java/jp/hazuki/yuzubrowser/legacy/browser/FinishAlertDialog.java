/*
 * Copyright (C) 2017-2019 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.browser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.ui.preference.CustomDialogPreference;
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs;

public class FinishAlertDialog extends CustomDialogPreference {
    private final boolean mShowMessage;
    private int mPositiveLabel = android.R.string.ok;
    private int mNegativeLabel = android.R.string.cancel;
    private int mNeutralLabel = 0;
    private int clearTabNo = -1;

    public interface OnClickListener {
        void onClick(DialogInterface dialog, int which, int new_value);
    }

    public FinishAlertDialog(Context context) {
        this(context, null, true);
    }

    public FinishAlertDialog(Context context, AttributeSet attrs) {
        this(context, attrs, false);
    }

    public FinishAlertDialog(Context context, boolean showMessage) {
        this(context, null, showMessage);
    }

    private FinishAlertDialog(Context context, AttributeSet attrs, boolean showMessage) {
        super(context, attrs);
        mShowMessage = showMessage;
    }

    public FinishAlertDialog setPositiveButton(int id) {
        mPositiveLabel = id;
        return this;
    }

    public FinishAlertDialog setNegativeButton(int id) {
        mNegativeLabel = id;
        return this;
    }

    public FinishAlertDialog setNeutralButton(int id) {
        mNeutralLabel = id;
        return this;
    }

    public FinishAlertDialog setClearTabNo(int clearTabNo) {
        this.clearTabNo = clearTabNo;
        return this;
    }

    @NonNull
    @Override
    protected CustomDialogFragment crateCustomDialog() {
        return FinishDialog.newInstance(mShowMessage, mPositiveLabel, mNegativeLabel, mNeutralLabel, clearTabNo);
    }

    public static class FinishDialog extends CustomDialogFragment {
        private static final String SHOW_MESSAGE = "message";
        private static final String POSITIVE = "positive";
        private static final String NEGATIVE = "negative";
        private static final String NEUTRAL = "neutral";
        private static final String CLEAR_TAB = "tab";

        private OnFinishDialogCallBack callBack;

        public static FinishDialog newInstance(boolean showMessage, int positive, int negative, int neutral, int tabNo) {
            FinishDialog dialog = new FinishDialog();
            Bundle bundle = new Bundle();
            bundle.putBoolean(SHOW_MESSAGE, showMessage);
            bundle.putInt(POSITIVE, positive);
            bundle.putInt(NEGATIVE, negative);
            bundle.putInt(NEUTRAL, neutral);
            bundle.putInt(CLEAR_TAB, tabNo);
            dialog.setArguments(bundle);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            boolean showMessage = getArguments().getBoolean(SHOW_MESSAGE);
            int positive = getArguments().getInt(POSITIVE);
            int negative = getArguments().getInt(NEGATIVE);
            int neutral = getArguments().getInt(NEUTRAL);
            int clearTabNo = getArguments().getInt(CLEAR_TAB);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.finish_alert, null);
            TextView textView = view.findViewById(R.id.textView);
            final CheckBox cacheCheckBox = view.findViewById(R.id.cacheCheckBox);
            final CheckBox cookieCheckBox = view.findViewById(R.id.cookieCheckBox);
            final CheckBox databaseCheckBox = view.findViewById(R.id.databaseCheckBox);
            final CheckBox passwordCheckBox = view.findViewById(R.id.passwordCheckBox);
            final CheckBox formdataCheckBox = view.findViewById(R.id.formdataCheckBox);
            final CheckBox faviconCheckBox = view.findViewById(R.id.faviconCheckBox);
            final CheckBox closeallCheckBox = view.findViewById(R.id.closeallCheckBox);
            final CheckBox historyCheckBox = view.findViewById(R.id.deleteHistoryCheckBox);
            final CheckBox searchCheckBox = view.findViewById(R.id.deleteSearchQueryCheckBox);
            final CheckBox geoCheckBox = view.findViewById(R.id.removeAllGeoPermissions);

            if (!showMessage)
                textView.setVisibility(View.GONE);

            final int def = AppPrefs.finish_alert_default.get();
            cacheCheckBox.setChecked((def & 0x01) != 0);
            cookieCheckBox.setChecked((def & 0x02) != 0);
            databaseCheckBox.setChecked((def & 0x04) != 0);
            passwordCheckBox.setChecked((def & 0x08) != 0);
            formdataCheckBox.setChecked((def & 0x10) != 0);
            historyCheckBox.setChecked((def & 0x20) != 0);
            searchCheckBox.setChecked((def & 0x40) != 0);
            geoCheckBox.setChecked((def & 0x80) != 0);
            faviconCheckBox.setChecked((def & 0x100) != 0);

            if (!AppPrefs.save_last_tabs.get())
                closeallCheckBox.setVisibility(View.GONE);
            //else
            //	closeallCheckBox.setChecked((def & 0x1000) != 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                formdataCheckBox.setVisibility(View.GONE);
                formdataCheckBox.setChecked(false);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle((showMessage) ? R.string.confirm : R.string.pref_clear_data_at_finish)
                    .setView(view)
                    .setPositiveButton(positive, (dialog, which) -> {
                        int new_settings = 0;

                        if (cacheCheckBox.isChecked())
                            new_settings |= 0x01;

                        if (cookieCheckBox.isChecked())
                            new_settings |= 0x02;

                        if (databaseCheckBox.isChecked())
                            new_settings |= 0x04;

                        if (passwordCheckBox.isChecked())
                            new_settings |= 0x08;

                        if (formdataCheckBox.isChecked())
                            new_settings |= 0x10;

                        if (historyCheckBox.isChecked())
                            new_settings |= 0x20;

                        if (searchCheckBox.isChecked())
                            new_settings |= 0x40;

                        if (geoCheckBox.isChecked())
                            new_settings |= 0x80;

                        if (faviconCheckBox.isChecked())
                            new_settings |= 0x100;

                        if (closeallCheckBox.isChecked())
                            new_settings |= 0x1000;

                        AppPrefs.finish_alert_default.set(new_settings);
                        AppPrefs.commit(getContext(), AppPrefs.finish_alert_default);

                        if (callBack != null)
                            callBack.onFinishPositiveButtonClicked(clearTabNo, new_settings);
                    })
                    .setNegativeButton(negative, null)
            ;
            if (neutral != 0)
                builder.setNeutralButton(neutral, (dialog, which) -> {
                    if (callBack != null)
                        callBack.onFinishNeutralButtonClicked(clearTabNo, def);
                });

            return builder.create();
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);

            if (getActivity() instanceof OnFinishDialogCallBack)
                callBack = (OnFinishDialogCallBack) getActivity();
        }

        @Override
        public void onDetach() {
            super.onDetach();

            callBack = null;
        }
    }

    public interface OnFinishDialogCallBack {
        void onFinishPositiveButtonClicked(int clearTabNo, int newSetting);

        void onFinishNeutralButtonClicked(int clearTabNo, int newSetting);
    }
}
