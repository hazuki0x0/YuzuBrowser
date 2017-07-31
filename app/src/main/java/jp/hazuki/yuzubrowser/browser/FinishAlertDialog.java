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

package jp.hazuki.yuzubrowser.browser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.settings.preference.common.CustomDialogPreference;

public class FinishAlertDialog extends CustomDialogPreference {
    private final boolean mShowMessage;
    private int mPositiveLabel = android.R.string.ok;
    private OnClickListener mPositiveListener;
    private int mNegativeLabel = android.R.string.cancel;
    private OnClickListener mNegativeListener;
    private int mNeutralLabel = 0;
    private OnClickListener mNeutralListener;

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

    @Override
    public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.finish_alert, null);
        TextView textView = (TextView) view.findViewById(R.id.textView);
        final CheckBox cacheCheckBox = (CheckBox) view.findViewById(R.id.cacheCheckBox);
        final CheckBox cookieCheckBox = (CheckBox) view.findViewById(R.id.cookieCheckBox);
        final CheckBox databaseCheckBox = (CheckBox) view.findViewById(R.id.databaseCheckBox);
        final CheckBox passwordCheckBox = (CheckBox) view.findViewById(R.id.passwordCheckBox);
        final CheckBox formdataCheckBox = (CheckBox) view.findViewById(R.id.formdataCheckBox);
        final CheckBox faviconCheckBox = (CheckBox) view.findViewById(R.id.faviconCheckBox);
        final CheckBox closeallCheckBox = (CheckBox) view.findViewById(R.id.closeallCheckBox);
        final CheckBox historyCheckBox = (CheckBox) view.findViewById(R.id.deleteHistoryCheckBox);
        final CheckBox searchCheckBox = (CheckBox) view.findViewById(R.id.deleteSearchQueryCheckBox);
        final CheckBox geoCheckBox = (CheckBox) view.findViewById(R.id.removeAllGeoPermissions);

        if (!mShowMessage)
            textView.setVisibility(View.GONE);

        final int def = AppData.finish_alert_default.get();
        cacheCheckBox.setChecked((def & 0x01) != 0);
        cookieCheckBox.setChecked((def & 0x02) != 0);
        databaseCheckBox.setChecked((def & 0x04) != 0);
        passwordCheckBox.setChecked((def & 0x08) != 0);
        formdataCheckBox.setChecked((def & 0x10) != 0);
        historyCheckBox.setChecked((def & 0x20) != 0);
        searchCheckBox.setChecked((def & 0x40) != 0);
        geoCheckBox.setChecked((def & 0x80) != 0);
        faviconCheckBox.setChecked((def & 0x100) != 0);

        if (!AppData.save_last_tabs.get())
            closeallCheckBox.setVisibility(View.GONE);
        //else
        //	closeallCheckBox.setChecked((def & 0x1000) != 0);

        builder
                .setTitle((mShowMessage) ? R.string.confirm : R.string.pref_clear_data_at_finish)
                .setView(view)
                .setPositiveButton(mPositiveLabel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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

                        AppData.finish_alert_default.set(new_settings);
                        AppData.commit(getContext(), AppData.finish_alert_default);

                        if (mPositiveListener != null)
                            mPositiveListener.onClick(dialog, which, new_settings);
                    }
                })
                .setNegativeButton(mNegativeLabel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mNegativeListener != null)
                            mNegativeListener.onClick(dialog, which, def);
                    }
                })
        ;
        if (mNeutralLabel != 0 && mNeutralListener != null)
            builder.setNeutralButton(mNeutralLabel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mNeutralListener != null)
                        mNeutralListener.onClick(dialog, which, def);
                }
            });
    }

    public FinishAlertDialog setPositiveButton(int id, OnClickListener l) {
        mPositiveListener = l;
        mPositiveLabel = id;
        return this;
    }

    public FinishAlertDialog setNegativeButton(int id, OnClickListener l) {
        mNegativeListener = l;
        mNegativeLabel = id;
        return this;
    }

    public FinishAlertDialog setNeutralButton(int id, OnClickListener l) {
        mNeutralListener = l;
        mNeutralLabel = id;
        return this;
    }
}
