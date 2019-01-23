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

package jp.hazuki.yuzubrowser.legacy.pattern;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.pattern.action.BlockPatternAction;
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity;

public abstract class PatternActivity<T extends PatternChecker> extends ThemeActivity implements View.OnClickListener, OnItemClickListener, OnItemLongClickListener {

    private PatternManager<T> mManager;

    private LinearLayout rootLayout;
    private ListView listView;
    private Button openOthersButton;
    private Button webSettingButton;
    private Button blockButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pattern_list_activity);

        rootLayout = findViewById(R.id.rootLayout);
        listView = findViewById(R.id.listView);
        openOthersButton = findViewById(R.id.openOthersButton);
        webSettingButton = findViewById(R.id.webSettingButton);
        blockButton = findViewById(R.id.blockButton);

        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        openOthersButton.setOnClickListener(this);
        webSettingButton.setOnClickListener(this);
        blockButton.setOnClickListener(this);
    }

    protected void setPatternManager(PatternManager<T> manager) {
        mManager = manager;

        listView.setAdapter(new ArrayAdapter<T>(this, 0, mManager.getList()) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.activity_pattern_item, parent, false);

                TextView title = convertView.findViewById(R.id.titleTextView);
                TextView action = convertView.findViewById(R.id.actionTitleTextView);
                CheckBox checkBox = convertView.findViewById(R.id.enableCheckBox);
                T item = getItem(position);
                if (item != null) {
                    title.setText(item.getTitle(getApplicationContext()));
                    action.setText(item.getActionTitle(getApplicationContext()));
                    checkBox.setOnCheckedChangeListener(null);
                    checkBox.setChecked(item.isEnable());
                    checkBox.setEnabled(true);
                    checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                        mManager.get(position).setEnable(b);
                        saveAndNotifyDataSetChanged();
                    });
                } else {
                    title.setText(R.string.unknown);
                    action.setText(R.string.unknown);
                    checkBox.setOnCheckedChangeListener(null);
                    checkBox.setChecked(false);
                    checkBox.setEnabled(false);
                }

                return convertView;
            }
        });
    }

    protected void addHeaderView(View view) {
        rootLayout.addView(view, 0);
    }

    @Override
    public void onClick(View v) {
        if (v == openOthersButton)
            onClick(PatternAction.OPEN_OTHERS, null);
        else if (v == webSettingButton)
            onClick(PatternAction.WEB_SETTING, null);
        else if (v == blockButton)
            onClick(PatternAction.BLOCK, null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        T checker = mManager.get(position);
        onClick(checker.getAction().getTypeId(), checker);
    }

    protected void saveAndNotifyDataSetChanged() {
        mManager.save(getApplicationContext());
        ((ArrayAdapter<?>) listView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm)
                .setMessage(R.string.confirm_delete_action)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    mManager.remove(position);
                    saveAndNotifyDataSetChanged();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        return true;
    }

    protected void onClick(int id, T checker) {
        switch (id) {
            case PatternAction.OPEN_OTHERS:
                getOpenOtherDialog(checker).show(getSupportFragmentManager(), "open");
                break;
            case PatternAction.WEB_SETTING:
                getWebSettingDialog(checker).show(getSupportFragmentManager(), "web");
                break;
            case PatternAction.BLOCK:
                settingBlockAction(checker, null);
                break;
        }
    }

    public abstract T makeActionChecker(PatternAction pattern_action, View header_view);

    protected int getPosition(T checker) {
        return mManager.getIndex(checker);
    }

    protected void add(int id, T newChecker) {
        if (id >= 0) {
            mManager.set(id, newChecker);
        } else {
            mManager.add(newChecker);
        }
        saveAndNotifyDataSetChanged();
    }

    protected abstract DialogFragment getWebSettingDialog(T checker);

    protected abstract DialogFragment getOpenOtherDialog(T checker);

    protected void settingBlockAction(final T checker, final View header_view) {
        if (checker != null)
            return;
        BlockPatternAction action = new BlockPatternAction();
        T new_checker = makeActionChecker(action, header_view);
        mManager.add(new_checker);
        saveAndNotifyDataSetChanged();
    }
}
